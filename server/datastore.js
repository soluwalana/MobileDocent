var mysql = require('db-mysql');
var mongodb = require('mongodb');
var helpers = require('./helpers.js')
var logger = require('./customLogger.js').getLogger();
var queries = require('./sql.js').queries;
var constants = require('./constants.js');

var errorCallback = function (message, callback){
    logger.error(message);
    callback({'error' : message}, null);
}

function DataStore (init_callback){
	
	var self = this;
	var mysql_db = new mysql.Database(constants.MYSQL_CONF);
	var mongoserver = new mongodb.Server(constants.MONGO_HOST,
                                         mongodb.Connection.DEFAULT_PORT)
    
	var mongo_db = new mongodb.Db(constants.MONGO_DB_NAME, mongoserver);
	
	var mysql_conn = null
	var mongo_conn = null;
	var authenticated = false;

    /* Initialize mysql and mongo db, call the initCallback when finished*/
	self.init = function(initCallback){
		mysql_db.connect(function(err){
            if (err){
                logger.error(err);
                initCallback(false);
                return null;
            }
            mysql_conn = this;
            mongo_db.open(function(err, openMongo){
				mongo_conn = openMongo;
                logger.info('DataStore Initialized')
				initCallback(true);
			});
		});
	};

    /* A function that will print that there is unauthenticated access
       to the data store and return an error to the caller */
	self.authenticatedAccess = function(callback){
		if (!authenticated){
			errorCallback('This request was not authenticated', callback);
			return false;
		}
		return true;
	};

    self.test = function (callback){
		var testData = {};
		mysql_conn.query('show tables').execute(function(err, rows, cols){
			testData['rows'] = rows;
			mongo_conn.collection(constants.MONGO_COLLECTION, function(err, collection){
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
        
        mysql_conn.query().execute(
            queries.insertUser,
            [userData.userName, password, salt, userData.about, userData.email,
             userData.fbId, userData.twitterId],
            function (err, result){
                if (err){
                    errorCallback('Insert User Failed, check duplicate', callback);
                    return null;
                }
                mysql_conn.query().execute(
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
        mysql_conn.query().execute(
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
		        callback(null, user.user_id);    
                
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
        if (!self.authenticatedAccess()){
            errorCallback('Unauthenticated access to MySQL', callback);
            return null;
        }
        callback(null, mysql_conn.query());
    };

    /* Returns a mongo collection to be operated on if authenticated
       access to database has been granted. should expect the err and
       collection */
    self.mongoCollection = function(collectionName, callback){
        if (!self.authenticatedAccess()){
            errorCallback('Unauthenticated access to Mongo DB', callback);
            return null;
        }
        mongo_conn.collection(collectionName, callback);
    };
    
    /* Do clean up here for data store*/
    self.close = function(){
        mongo_conn.close();
        mysql_conn.disconnect();
    };
    
	/* Define data store services here */
	self.init(init_callback)
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