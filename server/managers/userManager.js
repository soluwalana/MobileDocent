var SQL = require('../lib/sql.js').queries;
var helpers = require('../lib/helpers.js');
var logger = require('../lib/customLogger.js').getLogger();
var getLineNum = require('../lib/customLogger.js').getLineNumber;

var errorHelper = require('../lib/helpers.js').errorHelper;
var errorWrap = function (retCallback, callback){
    return errorHelper(logger, getLineNum(), callback, retCallback);
};
var errorCallback = function (msg, callback){
    logger.error(msg);
    return callback({ error : msg });
}

var UserManager = function(store){
    var self = this;

    self.init = function (){
        self.store = store;
    };

    self.authenticate = function(params, callback){
        if ((!params.userName && !params.userId) || !params.pass ){
            return errorCallback('Request Missing Paramaters', callback);
        }
        
        self.store.sqlConn(errorWrap(callback, function(conn){
            var sql = params.userName ? SQL.getUserByName : SQL.getUserById;
            var sqlParams = [params.userName ? params.userName : params.userId];
            conn.query(sql, sqlParams).execute(
                errorWrap(callback, function (rows, cols){
                    if (rows.length === 0){
                        return errorCallback('User lookup failed for authentication', callback);
                    }
                    
                    if (rows.length !== 1){
                        return errorCallback('More than one user matches the userName/device');
                    }
                    
                    var user = rows[0];
                    var password = helpers.generatePassword(params.pass, user.salt);
                    if (password !== user.password){
                        return errorCallback('Authentication failure', callback);
                    }
                    
                    callback(null, user.userId);    
                    
                })
            );
        }));
    };
    
    self.addUser = function(params, callback){
        if (!params.userName ||!params.pass || !params.passConf){
            return errorCallback('Request missing basic parameters', callback);
        }
        
        if (params.pass !== params.passConf){
            return errorCallback("Passwords don't match", callback);
        }
        
        /* Set Default values */
        params.about = params.about || null;
        params.email = params.email || null;
        params.fbId = params.fbId || null;
        params.twitterId = params.twitterId || null;
        
        var salt = helpers.generateSalt();
        var password = helpers.generatePassword(params.pass, salt);
        
        self.store.sqlConn(errorWrap(callback, function(conn){
            var sql = SQL.addUser;
            var sqlParams = [params.userName, password, salt, params.about,
                             params.email, params.fbId, params.twitterId];

            conn.query(sql, sqlParams).execute(errorWrap(
                callback, function (result){
                    callback(null, result.id);
                }
            ));
        }));
    };

    self.getUser = function(params, callback){
        if (!params.userName && !params.userId && !params.deviceId){
            return errorCallback('Required Field Missing From User Request', callback);
        }
        self.store.sqlConn(errorWrap(callback, function(conn){
            var sql = null;
            var sqlParams = [];
            
            if (params.userId){
                sql = SQL.getUserById;
                sqlParams = [params.userId];
                logger.debug('Get by ID');
            } else if (params.userName){
                sql = SQL.getUserByName;
                sqlParams = [params.userName];
                logger.debug('Get by Name');
            } else if (params.deviceId){
                sql = SQL.getUsersByDevice;
                sqlParams = [params.deviceId];
                logger.debug('Get by Device');
            }
            
            conn.query(sql, sqlParams).execute(errorWrap(
                callback, function (rows, cols){
                    if (rows.length === 0){
                        logger.warn('No Users Found For query');
                        logger.warn([sql, params]);
                        return callback([]);
                    }
                    if (rows.length === 1){
                        return callback(rows[0]);
                    }
                    callback(rows);
                }
            ));
        }));
    };

    self.init();
    return self;
};

exports.UserManager = UserManager;