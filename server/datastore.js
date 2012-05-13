var mysql = require('db-mysql');
var mongodb = require('mongodb');
var crypto = require('crypto');

var mysqlConfig = {
	hostname : 'localhost',
	user : 'docent',
	password : 'Docent_2012',
	database : 'docent_db'
};

var MONGO_HOST = 'localhost';
var MONGO_DB_NAME = 'docent';
var MONGO_COLLECTION = 'docent_docs';

var SHA1_KEY = 'Docent_2012_SHA1_Private';
var SALT_POSSIBLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
var SALT_LENGTH = 8;

var generateSalt = function(){
	var text = "";
	for (var i = 0; i < SALT_LENGTH; i++){
		text += SALT_POSSIBLE.charAt(Math.floor(Math.random() * SALT_POSSIBLE.length));
	}
	return text;
};

var generatePassword = function(password, salt){
	var hmac = crypto.createHmac('sha1', SHA1_KEY);
	var hash = hmac.update(password + salt);
	return hmac.digest(encoding = 'base64');
};


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
			console.log('MySQL connected')
			self.db = this;
			mongo_db.open(function(err, mongo){
				console.log('mongo connected');
				self.mongo = mongo;
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