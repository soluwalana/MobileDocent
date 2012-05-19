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
        if ((!params.userName && !params.userId) || !params.pass ){
            return errorCallback('Request to authenticate had wrong pramaters', callback);
        }
        
        self.store.sqlConn(function(err, conn){
            var sql = params.userName ? queries.selectUserByName : queries.selectUserById;
            var sqlParams = [params.userName ? params.userName : params.userId];
            conn.query(sql, sqlParams).execute(
                function (err, rows, cols){
                    if (err || rows.length === 0){
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
                    
                }
            );
        })
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
        
        self.store.sqlConn(function(err, conn){
            if (err){
                return callback(err);
            }
            var sql = queries.insertUser;
            var sqlParams = [params.userName, password, salt, params.about,
                             params.email, params.fbId, params.twitterId];
            conn.query(sql, sqlParams).execute(function (err, result){
                if (err){
                    return errorCallback('Insert User Failed, check duplicate', callback);
                }
                callback(null, result.id);
            });
        });
    };

    self.getUser = function(params, callback){
        if (!params.userName && !params.userId && !params.deviceId){
            return errorCallback('Required Field Missing From User Request', callback);
        }
        self.store.sqlConn(function(err, conn){
            if (err){
                return callback(err);
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
                    return errorCallback(err, callback);
                    
                }
                if (rows.length === 0){
                    logger.warn('No Users Found For query');
                    logger.warn([sql, params]);
                    return callback([]);
                }
                if (rows.length === 1){
                    return callback(rows[0]);
                }
                callback(rows);
            });
        });
    };

    self.init();
    return self;
}

exports.UserManager = UserManager;