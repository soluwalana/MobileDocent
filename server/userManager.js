var queries = require('./sql.js').queries;
var logger = require('./customLogger.js').getLogger();
var helpers = require('./helpers.js');

var errorCallback = function (message, callback){
    logger.error(message, 1);
    callback({'error' : message});
}

var UserManager = function(store){
    var self = this;

    self.init = function (){
        self.store = store;
    };

    self.authenticate = function(params, callback){
        if (!params.userName || !params.pass || !params.deviceId){
            errorCallback('Request to authenticate had wrong pramaters', callback);
            return null;
        }
        self.store.sqlConn(function(err, conn){
            var sql = queries.selectUserAuth;
            var sqlParams = [params.userName, params.deviceId];
            conn.query(sql, sqlParams).execute(
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
                    var password = helpers.generatePassword(params.pass, user.salt);
                    if (password !== user.password){
                        errorCallback('Authentication failure', callback);
                        return null;
                    }
                    
		            callback(null, user.userId);    
                    
                }
            );
        })
    };
    
    self.addUser = function(params, callback){
        if (!params.userName || !params.deviceId ||
            !params.pass || !params.passConf){
            errorCallback('Request missing basic parameters', callback);
            return null;
        }
        
        if (params.pass !== params.passConf){
            errorCallback("Passwords don't match", callback);
            return null;
        }
        
        /* Set Default values */
        params.about = params.about || null;
        params.email = params.email || null;
        params.fbId = params.fbId || null;
        params.twitterId = params.twitterId || null;
        
        var salt = helpers.generateSalt();
        var password = helpers.generatePassword(params.pass, salt);
        
        self.store.sqlConn(function(err, conn){
            if (err){
                callback(err);
                return null;
            }
            var sql = queries.insertUser;
            var sqlParams = [params.userName, password, salt, params.about,
                              params.email, params.fbId, params.twitterId];

            conn.query(sql, sqlParams).execute(function (err, result){
                if (err){
                    errorCallback('Insert User Failed, check duplicate', callback);
                    return null;
                }
                sql = queries.insertDevice;
                sqlParams = [result.id, params.deviceId];
                conn.query(sql, sqlParams).execute(
                    function  (err, result){
                        if (err){
                            errorCallback('Insert Device ID failed', callback);
                            return null;
                        }
                        callback(null, result.id);
                    }
                );
            });
        });
    };

    self.getUser = function(params, callback){
        if (!params.userName && !params.userId && !params.deviceId){
            errorCallback('Required Field Missing From User Request', callback);
            return null;
        }
        self.store.sqlConn(function(err, conn){
            if (err){
                callback(err);
                return null;
            }
            var sql = null;
            var sqlParams = [];
            
            if (params.userId){
                sql = queries.selectUserById;
                sqlParams = [params.userId];
                logger.debug('Select by ID');
            } else if (params.userName){
                sql = queries.selectUserByName;
                sqlParams = [params.userName];
                logger.debug('Select by Name');
            } else if (params.deviceId){
                sql = queries.selectUsersByDevice;
                sqlParams = [params.deviceId];
                logger.debug('Select by Device');
            }
            
            conn.query(sql, sqlParams).execute(function (err, rows, cols){
                if (err){
                    errorCallback(err, callback);
                    return null;
                }
                if (rows.length === 0){
                    logger.warn('No Users Found For query');
                    logger.warn([sql, params]);
                    callback([]);
                    return null;
                }
                if (rows.length === 1){
                    callback(rows[0]);
                    return null;
                }
                callback(rows);
            });
        });
    };

    self.init();
    return self;
}

exports.UserManager = UserManager;