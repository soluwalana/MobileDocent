var mysqlConfig = {
	hostname : 'localhost',
	user : 'docent',
	password : 'Docent_2012',
	database : 'docent_db'
};

var MONGO_HOST = 'localhost';
var MONGO_DB_NAME = 'docent';
var MONGO_COLLECTION = 'docent_docs';
var mysql = require('db-mysql');
var mongodb = require('mongodb');


function DataStore (init_callback){
	
	var self = this;
	var mysql_db = new mysql.Database(mysqlConfig);
	var mongoserver = new mongodb.Server(MONGO_HOST, mongodb.Connection.DEFAULT_PORT)
	var mongo_db = new mongodb.Db(MONGO_DB_NAME, mongoserver);
	
	self.db = null
	self.mongo = null;

	self.init = function(){
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
	
	
	/* Define data store services here */
	self.init()
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