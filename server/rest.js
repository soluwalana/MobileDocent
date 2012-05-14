var logger = require('./customLogger.js').getLogger();


var rest = {}
/* Get API's*/
rest.getUser = function (dataStore, params, callback){
    logger.info('User Request Received');
	callback({'success' : 'Ran',
			  'data' : params});
};

rest.getTour = function (dataStore, params, callback){
    logger.info('Tour Request Received');
	callback({'success' : 'got tour request',
			  'data' : params});
};

rest.getNode = function (dataStore, params, callback){
    logger.info('Node Request Received');
	callback({ 'success' : 'get node by id',
			   'data' : params});
};

rest.getLocation = function (dataStore, params, callback){
    logger.info('Request for location');
    callback({'success' : 'get location',
              'data' : params});
};

rest.getTags = function (dataStore, params, callback){
    logger.info('List Tags Request');
    callback({'success' : 'get tags',
              'data' : params});
};

/* Create API's */
rest.createUser = function (dataStore, params, callback){
    logger.info('User Creation Request Received');
	callback({'success' : 'got user create request',
			  'data' : params});
};

rest.createTour = function (dataStore, params, callback){
    logger.info('Tour Creation Request Received');
	callback({'success' : 'got tour create request',
			  'data' : params});
};

rest.createNode = function (dataStore, params, callback){
    logger.info('Node Modification Request Received');
	callback({'success' : 'add a node to the tour',
			  'params' : params});
};

rest.createTags = function (dataStore, params, callback){
};

/* Modification API's */
rest.modifyUser = function (dataStore, params, callback){
};
 
rest.modifyTour = function (dataStore, params, callback){
};

rest.modifyNode = function (dataStore, params, callback){
};


exports.rest = rest;