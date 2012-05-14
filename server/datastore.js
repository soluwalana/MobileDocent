var mysql = require('db-mysql');
var mongodb = require('mongodb');
var helpers = require('./helpers.js')
var logger = require('./customLogger.js').getLogger();

var mysqlConfig = {
	hostname : 'localhost',
	user : 'docent',
	password : 'Docent_2012',
	database : 'docent_db'
};

var MONGO_HOST = 'localhost';
var MONGO_DB_NAME = 'docent';
var MONGO_COLLECTION = 'docent_docs';

function DataStore (init_callback){
	
	var self = this;
	var mysql_db = new mysql.Database(mysqlConfig);
	var mongoserver = new mongodb.Server(MONGO_HOST, mongodb.Connection.DEFAULT_PORT)
	var mongo_db = new mongodb.Db(MONGO_DB_NAME, mongoserver);
	
	self.db = null
	self.mongo = null;
	self.authenticated = false;
	
	self.init = function(init_callback){
		mysql_db.connect(function(err){
			self.db = this;
			mongo_db.open(function(err, mongo){
				self.mongo = mongo;
                logger.info('DataStore Initialized')
				init_callback();
			});
		});
	};

	self.authenticatedAccess = function(callback){
		if (! self.authenticated){
			callback({'error' : 'This request was not authenticated'});
			return false;
		}
		return true;
	};

	self.test = function (callback){
		var testData = {};
		self.db.query('show tables').execute(function(err, rows, cols){
			testData['rows'] = rows;
			self.mongo.collection(MONGO_COLLECTION, function(err, collection){
				testData['collection'] = collection.collectionName;
				callback(null, testData);
			});
		});
	};

	self.addUser = function(userData, callback){
		callback({'error' : 'Not Implemented'});
	};
	
	self.authenticate = function(userParams, callback){
		self.authenticated = true;
	};

	self.addTour = function (tourParams, callback){
		if (!authenticatedAccess) return null;
		callback({'error' : 'Not Implemented'});
	};

	/* Get the region for the user based on their IP address
	   when they don't have location services enabled this is
	   our best guess to guide their tour setups */
	self.getRegion = function(ipAddr){
		
	};
	
	/* Define data store services here */
	self.init(init_callback)
	return self;
}
/* Going to set up the necessary MySQL and MongoDB services */
var buildDataStore = function (req, res, callback){
	var ds = new DataStore(function(){
		var send = req.send;
		var end = req.end;

		req.send = function(message){
			ds.close();
			send(message);
		};

		req.end = function(message){
			ds.close();
			end(message);
		};
		
		req.ds = ds;
		res.ds = ds;
		callback();
	});
};

exports.buildDataStore = buildDataStore;