var mysql = require('db-mysql');
var mongodb = require('mongodb');
var uuid = require('node-uuid');

var helpers = require('./helpers.js');
var logger = require('./customLogger.js').getLogger();
var queries = require('./sql.js').queries;
var constants = require('./constants.js');

var UserManager = require('../managers/userManager.js').UserManager;

var errorCallback = function (message, callback){
    logger.error(message);
    callback({'error' : message}, null);
};

function DataStore (initCallback){
    
    var self = this;
    var mysqlDb = new mysql.Database(constants.MYSQL_CONF);
    var mongoserver = new mongodb.Server(constants.MONGO_HOST,
                                         mongodb.Connection.DEFAULT_PORT);
    
    var mongoDb = new mongodb.Db(constants.MONGO_DB_NAME, mongoserver);
    
    var mysqlConn = null;
    var mongoConn = null;
    var authenticated = false;

    /* Initialize mysql and mongo db, call the initCallback when finished*/
    self.init = function(initCallback){
        mysqlDb.connect(function(err){
            if (err){
                logger.error(err);
                return initCallback(false);
            }
            mysqlConn = this;
            mongoDb.open(function(err, openMongo){
                mongoConn = openMongo;
                logger.info('DataStore Initialized');
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

    self.unauthenticatedStore = function(){
        var store = {};
        store.sqlConn = function(callback){
            callback(null, mysqlConn);
        };
        store.mongoCollection = function(IGNORED, callback){
            errorCallback('Unauthenticated access to '+msg, callback);
        };
        store.mongoGrid = function(IGNORED, callback){
            errorCallback('Unauthenticated access to '+msg, callback);
        };
        return store;
    };

    /* Functions that do not need authentication */

    self.test = function (callback){
        var testData = {};
        mysqlConn.query('show tables').execute(function(err, rows, cols){
            testData.rows = rows;
            mongoConn.collection(constants.MONGO_COLLECTION, function(err, collection){
                testData.collection = collection.collectionName;
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
        var userManager = new UserManager(self.unauthenticatedStore());
        userManager.addUser(userData, function(err, userId){
            if (err){
                return callback(err);
            }
            authenticated = true;
            callback(null, userId);
        });
        
    };

    /* Verifies that the user given by the userData is indeed a valid user
       if so it will allow access to authenticated only access to this service
       @param {object} userData - An object expecting fields: user, pass, device
       @param {function} callback - A function that should expect two parameters
           err and the resulting user id from the call
       */
    self.authenticate = function(userData, callback){
        var userManager = new UserManager(self.unauthenticatedStore());        
        userManager.authenticate(userData, function(err, userId){
            if (err){
                return callback(err);
            }
            authenticated = true;
            callback(null, userId);
        });
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

    /* Functions that must be authenticated */
    
    /* Returns the conn object to be executed as a parameter to callback,
       will return an error on unauthenticated access
       @param {function} callback - A function that should be expecting
           two parameters err, and query
    */
    self.sqlConn = function(callback){
        if (self.authenticatedAccess(callback, 'MySQL')){
            callback(null, mysqlConn);
        }
    };

    /* Gets the 12 byte mongo db _id type from the given hex
       string */
    self.getMongoIdFromHex = function(hexStr){
        try{
            return mongodb.ObjectID.createFromHexString(hexStr);
        } catch (e){
            return null;
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


    /* Returns a GridStore object that can be used for storing
       files up to 2GB in size and streamed in and out of the
       mongo database
       @param {string} type: mimetype of file to be opened
       @param {function} callback: should expect err and the gridstore
           as parameters*/
    self.newMongoGrid = function(type, callback){
        if (self.authenticatedAccess(callback)){
            var fileName = uuid.v4();
            var opts = {
                content_type : type,
                chunk_size : constants.FILE_BUF_SIZE
            };
            var gs = new mongodb.GridStore(mongoDb, fileName, 'w', opts);
            gs.open(function(err, gs){
                if (err){
                    return errorCallback('Error Making grid store', callback);
                }
                callback(null, gs, fileName);
            });
        }
    };

    /* Return a stored GridStore object for reading
       @param {string} mongoId: the mongo Id for the stored file
       @param {function} callback: should expect err and the gs as parameters*/
    self.mongoGrid = function(mongoId, callback){
        if (self.authenticatedAccess(callback)){
            var gs = new mongodb.GridStore(
                mongoDb, mongoId, 'r', {chunk_size : constants.FILE_BUF_SIZE});
            gs.open(function(err, gs){
                if (err){
                    return errorCallback('Error Opening Grid Store', callack);
                }
                if (gs.length === 0){
                    return errorCallback('This File Does Not Have Data', callback);
                }
                callback(null, gs);
            });
        }
    };
    
    /* Do clean up here for data store*/
    self.close = function(){
        mongoConn.close();
        mysqlConn.disconnect();
    };
    
    /* Define data store services here */
    self.init(initCallback);
    return self;
}
/* Going to set up the necessary MySQL and MongoDB services */
var buildDataStore = function (req, res, callback){
    var ds = new DataStore(function(inited){
        if (!inited){
            return res.send('There was an error initializing the DB', 500);
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