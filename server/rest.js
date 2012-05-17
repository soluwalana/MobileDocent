var logger = require('./customLogger.js').getLogger();
var queries = require('./sql.js').queries;
var constants = require('./constants.js');
var fs = require('fs');

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
        
        query.execute(sql, sqlParams, function (err, rows, cols){
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

var storeFile = function (store, files, data, callback){
    var id = data.id;
    var idx = data.idx;
    var type = data.type;
    
    store.mongoGrid(type, function(err, gs, name){
        if (err){
            commit({'error' : fileName+' couldnt be made'});
            return null;
        }
    });
};

rest.createNode = function (store, params, files, callback){
    logger.info('File Upload Recieved');

    // Verify the parameters (This allows you to make empty pages)
    if (!params.nodeData || !params.nodeData.latitude ||
        !params.nodeData.longitude || !nodeData.content ||
        nodeData.content.length === 0){
        
        errorCallback('Missing Node Data', callback);
        return null;
    }
    var nodeData = params.nodeData;
    var numberCommitted = 0;
    var numberSubmitted = 0;
    var status = constants.GOOD_STATUS;
    var message = '';
    var fileMap = {};
    
    var finish = function (){
        callback({'success': 'committed node'});
    };
    
    var commit = function (err, oldName, newName){
        numberCommitted ++;
        if (err){
            status = constants.WARN_STATUS;
            if (message === ''){
                message = [];
            }
            message.push(err);
        }
        if (numberCommitted == numberSubmitted){
            logger.debug('All Files Done');
            finish();
        }
    };
    
    var fileContent = [];
    for (var i = 0; i < nodeData.length; i ++){
        for (var j = 0; j < nodeData[i].length; j++){
            if (nodeData[i][j].contentId){
                fileContent.push({'idx' : [i, j], 'id' : nodeData[i][j].contentId});
            }
        }
    }
    numberSubmitted = fileContent.length;
    
    if (numberSubmitted == numberCommitted){
        finish();
        return null;
    }
    
    for (i = 0; i < fileContent; i ++){
        fileContent[i].type = nodeData[idx[0]][idx[1]];
        if (!files[fileContent[i].id]){
            commit({'error' : 'File Was Not Uploaded'});
            continue;
        }
        storeFile(store, files, fileContent, commit);
    }
    
    
    logger.debug(req.body);
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