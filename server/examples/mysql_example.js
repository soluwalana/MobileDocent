var mysql = require('db-mysql');
var mysqlConfig = {
	hostname : 'localhost',
	user : 'docent',
	password : 'Docent_2012',
	database : 'docent_db'
};

var db = new mysql.Database(mysqlConfig);

db.on('error', function(error){
	console.log(error);
});

db.on('ready', function(server){
	console.log('Connected to '+server.hostname+ ' ('+server.version+')');
	console.log(server);
});

db.connect(function(error){
	console.log("Connected");
	for ( prop in this.query()){
		console.log(prop);
	}
		
	this.query('create table newTable (field1 integer not null primary key, field2 integer not null)').execute();
	this.query('create table newTable1 (field3 integer not null primary key, field4 integer not null)').execute();
	this.query('show tables').execute(function(err, rows, cols){
		console.log(rows.length);
		console.log(cols);
	});
	this.query('insert into newTable values (1, 2), (3, 4), (5, 6)').execute();
	this.query('select * from newTable').execute(function(err, rows, cols){
		console.log(rows);
		console.log(cols);
	});
    this.query('select * from newTable; update newTables set field1=8;').execute(function(err, rows, cols){
        console.log(this.sql());
        console.log(err);
        console.log(rows);
		console.log(cols);
    });
	this.query('drop table newTable').execute();
	this.query('drop table newTable1').execute();
    
});