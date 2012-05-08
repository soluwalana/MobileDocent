var mysqlConfig = {
	hostname : 'localhost',
	user : 'docent',
	password : 'Docent_2012',
	database : 'docent_db'
};

var MONGO_HOST = 'localhost';


var mysql = require('db-mysql');

var mongodb = require('mongodb');
var mongoserver = new mongodb.Server(MONGO_HOST, mongodb.Connection.DEFAULT_PORT)

function DataStore (init_callback){
	var self = 
	self.db = new mysql.Database(mysqlConfig);

	self.db.on('ready', function (server){
		console.log('MySQL ready');
		init_callback();
	});
	
	return self;
}
/* Going to set up the necessary MySQL and MongoDB services */
var buildDataStore = function (req, res, callback){
	
	
};

exports.buildDataStore = buildDataStore;