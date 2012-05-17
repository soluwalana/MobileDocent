var mysql = require('db-mysql');
var mongodb = require('mongodb');
var helpers = require('./helpers.js');
var uuid = require('node-uuid');
var logger = require('./customLogger.js').getLogger();
var queries = require('./sql.js').queries;
var constants = require('./constants.js');

var errorCallback = function (message, callback){
    logger.error(message);
    callback({'error' : message}, null);
}

function DataStore (initCallback){
	
	var self = this;
	var mysqlDb = new mysql.Database(constants.MYSQL_CONF);
	var mongoserver = new mongodb.Server(constants.MONGO_HOST,
                                         mongodb.Connection.DEFAULT_PORT)
    
	var mongoDb = new mongodb.Db(constants.MONGO_DB_NAME, mongoserver);
	
	var mysqlConn = null
	var mongoConn = null;
	var authenticated = false;

    /* Initialize mysql and mongo db, call the initCallback when finished*/
	self.init = function(initCallback){
		mysqlDb.connect(function(err){
            if (err){
                logger.error(err);
                initCallback(false);
                return null;
            }
            mysqlConn = this;
            mongoDb.open(function(err, openMongo){
				mongoConn = openMongo;
                logger.info('DataStore Initialized')
				initCallback(true);
			});
		});
	};

    /* A function that will print that there is unauthenticated access
       to the data store and return an error to the caller */
	self.authenticatedAccess = function(callback, msg){
		if (!authenticated){
			errorCallback('Unauthenticated access to '+msg, callback);
			return false;
		}
		return true;
	};

    self.test = function (callback){
		var testData = {};
		mysqlConn.query('show tables').execute(function(err, rows, cols){
			testData['rows'] = rows;
			mongoConn.collection(constants.MONGO_COLLECTION, function(err, collection){
				testData['collection'] = collection.collectionName;
				callback(null, testData);
			});
		});
	};

    /* Creates a new user and authenticates that user's access to the data store
       @param {object} userData - Expected fields: userName, deviceId, pass, passConf
           optional fields: about, email, fbId, twitterId
       @param {function} callback - A function that should expect two parameters
           err and the resulting user id from the call */
	self.addUser = function(userData, callback){
        if (!userData.userName || !userData.deviceId ||
            !userData.pass || !userData.passConf){
            errorCallback('Request missing basic parameters', callback);
            return null;
        }
        
        if (userData.pass !== userData.passConf){
            errorCallback("Passwords don't match", callback);
            return null;
        }
         
        /* Set Default values */
        userData.about = userData.about || null;
        userData.email = userData.email || null;
        userData.fbId = userData.fbId || null;
        userData.twitterId = userData.twitterId || null;
        
        var salt = helpers.generateSalt();
        var password = helpers.generatePassword(userData.pass, salt);
        
        mysqlConn.query().execute(
            queries.insertUser,
            [userData.userName, password, salt, userData.about, userData.email,
             userData.fbId, userData.twitterId],
            function (err, result){
                if (err){
                    errorCallback('Insert User Failed, check duplicate', callback);
                    return null;
                }
                mysqlConn.query().execute(
                    queries.insertDevice,
                    [result.id, userData.deviceId],
                    function  (err, result){
                        if (err){
                            errorCallback('Insert Device ID failed', callback);
                            return null;
                        }
                        authenticated = true;
                        callback(null, result.id);
                    }
                );
            }
        );
    };

    /* Verifies that the user given by the userData is indeed a valid user
       if so it will allow access to authenticated only access to this service
       @param {object} userData - An object expecting fields: user, pass, device
       @param {function} callback - A function that should expect two parameters
           err and the resulting user id from the call
       */
	self.authenticate = function(userData, callback){
        if (!userData.userName || !userData.pass || !userData.deviceId){
            errorCallback('Request to authenticate had wrong pramaters', callback);
            return null;
        }
        mysqlConn.query().execute(
            queries.selectUserAuth,
            [userData.userName, userData.deviceId],
            function (err, rows, cols){
                if (err || rows.length === 0){
                    errorCallback('User lookup failed for authentication', callback);
                    return null;
                }

                if (rows.length !== 1){
                    errorCallback('More than one user matches the userName/device');
                    return null;
                }
                var user = rows[0];
                var password = helpers.generatePassword(userData.pass, user.salt);
                if (password !== user.password){
                    errorCallback('Authentication failure', callback);
                    return null;
                }
                authenticated = true;
		        callback(null, user.userId);    
                
            }
        );
	};

    /* Verified that the session is authenticated and then sets authenticated
       access to the data store to true
       @param {object} session - the server session object
       @param {function} callback - A function that should expect two parameters
           err and the resulting user id from the call
       */
    self.sessionAuthenticate = function(session, callback){
        if (session.authenticated && session.userId){
            authenticated = true;
            callback(null, session.userId);
        } else {
            errorCallback('Session is not authenticated', callback);
        }
    };

    /* Returns a query object to be executed on to the callback,
       will return an error on unauthenticated access
       @param {function} callback - A function that should be expecting
           two parameters err, and query
    */
    self.sqlQuery = function(callback){
        if (self.authenticatedAccess(callback, 'MySQL')){
            callback(null, mysqlConn.query());
        }
    };

    /* Returns a mongo collection to be operated on if authenticated
       access to database has been granted. should expect the err and
       collection */
    self.mongoCollection = function(collectionName, callback){
        if (self.authenticatedAccess(callback, 'MongoDB')){
            mongoConn.collection(collectionName, callback);
        }        
    };

    
    self.mongoGrid = function(type, callback){
        if (self.authenticatedAccess(callback)){
            var fileName = uuid.v1();
            var opts = {
                'content_type' : type,
                'chunk_size' : 1024*4
            };
            var gs = new mongodb.GridStore(mongoDb, fileName, 'w', opts);
            gs.open(function(err, gs){
                if (err){
                    errorCallback('Error Making grid store', callback);
                    return null;
                }
                callback(null, gs, fileName);
            });
        }
    };
    
    /* Do clean up here for data store*/
    self.close = function(){
        mongoConn.close();
        mysqlConn.disconnect();
    };
    
	/* Define data store services here */
	self.init(initCallback)
	return self;
}
/* Going to set up the necessary MySQL and MongoDB services */
var buildDataStore = function (req, res, callback){
	var ds = new DataStore(function(inited){
        if (!inited){
            res.send('There was an error initializing the DB', 500);
            return null;
        }
        
		var send = res.send;
		var end = res.end;
        
	    res.send = function(message){
            ds.close();
            res.send = send;
            res.send(message);
			
		};

		res.end = function(message){
            ds.close();
            res.end = end;
        	res.end(message);
		};
		
		req.ds = ds;
		res.ds = ds;
		callback();
	});
};

exports.buildDataStore = buildDataStore;