var logger = require('./customLogger.js').getLogger();
var queries = require('./sql.js').queries;

var errorCallback = function (message, callback){
    logger.error(message);
    callback({'error' : message});
}

var rest = {}
/* Get API's*/
rest.getUser = function (store, params, callback){
    if (!params.userName && !params.userId && !params.deviceId){
        errorCallback('Required Field Missing From User Request', callback);
        return null;
    }
    store.sqlQuery(function(err, query){
        if (err){
            callback(err);
            return null;
        }
        var sql = null;
        var sql_params = [];
        
        if (params.userId){
            sql = queries.selectUserById;
            sql_params = [params.userId];
            logger.debug('Select by ID');
        } else if (params.userName){
            sql = queries.selectUserByName;
            sql_params = [params.userName];
            logger.debug('Select by Name');
        } else if (params.deviceId){
            sql = queries.selectUsersByDevice;
            sql_params = [params.deviceId];
            logger.debug('Select by Device');
        }
        
        query.execute(sql, sql_params, function (err, rows, cols){
            if (err){
                errorCallback(err, callback);
                return null;
            }
            if (rows.length === 0){
                logger.debug('No Users Found For query');
                logger.debug(sql, params);
                callback([]);
                return null;
            }
            if (rows.length === 1){
                logger.debug('User found');
                logger.debug(rows[0]);
                callback(rows[0]);
                return null;
            }
            logger.debug('Many Users found');
            logger.debug(rows);
            callback(rows);
        });
    });
};

rest.getTour = function (store, params, callback){
    logger.info('Tour Request Received');
	callback({'success' : 'got tour request',
			  'data' : params});
};

rest.getNode = function (store, params, callback){
    logger.info('Node Request Received');
	callback({ 'success' : 'get node by id',
			   'data' : params});
};

rest.getLocation = function (store, params, callback){
    logger.info('Request for location');
    callback({'success' : 'get location',
              'data' : params});
};

rest.getTags = function (store, params, callback){
    logger.info('List Tags Request');
    callback({'success' : 'get tags',
              'data' : params});
};

/* Create API's */

rest.createTour = function (store, params, callback){
    logger.info('Tour Creation Request Received');
	callback({'success' : 'got tour create request',
			  'data' : params});
};

rest.createNode = function (store, params, callback){
    logger.info('Node Modification Request Received');
	callback({'success' : 'add a node to the tour',
			  'params' : params});
};

rest.createTags = function (store, params, callback){
};

/* Modification API's */
rest.modifyUser = function (store, params, callback){
};
 
rest.modifyTour = function (store, params, callback){
};

rest.modifyNode = function (store, params, callback){
};


exports.rest = rest;