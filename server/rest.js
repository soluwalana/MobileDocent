var logger = require('./customLogger.js').getLogger();
var TourManager = require('./tourManager.js').TourManager;
var UserManager = require('./userManager.js').UserManager;
var queries = require('./sql.js').queries;

var rest = {}


/* Get API's*/
rest.getUser = function (store, params, callback){
    var userManager = new UserManager(store);
    userManager.getUser(params, callback);
};

rest.getTour = function (store, params, callback){
    var tourManager = new TourManager(store);
    tourManager.getTour(params, callback);
};

rest.getNode = function (store, params, callback){
    var tourManager = new TourManager(store);
    tourManager.getNode(params, callback);
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
    var tourManager = new TourManager(store);
    tourManager.createTour(params, callback);
};

rest.createNode = function (store, params, files, callback){
    var tourManager = new TourManager(store);
    tourManager.createNode(params, files, callback);
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