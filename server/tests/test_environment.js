var should = require('should');

var mysqlConfig = {
	hostname : 'localhost',
	user : 'docent',
	password : 'Docent_2012',
	database : 'docent_db'
};


describe('Mysql Setup Test', function(){
	it('Access/Connect', function(done){
		var mysql = require('db-mysql');
		var db = new mysql.Database(mysqlConfig);
		
		db.should.be.ok;
		db.on('error', function(error){
			error.should.not.be.ok;
		});
		db.on('ready', function(server){
			console.log('Connected to '+server.hostname+ ' ('+server.version+')');
			console.log(server);
		});
		db.connect.should.be.ok;
		
		console.log()
		db.connect(function(error){
			should.not.exist(error);
			console.log("Connected");
			done();
		});
	});

	it('Create Table/Drop Table', function(done){
		var mysql = require('db-mysql');
		var db = new mysql.Database(mysqlConfig);
		db.should.be.ok;
		db.connect.should.be.ok;
		db.on('error', function (error){
			console.log(error);
			error.should.not.be.ok;
		});
		
		
		db.connect(function(error){
			should.not.exist(error);
			var numTables = 0;
			var expectedNumTables = 0;
			var numberCallback = function(error, rows, cols){
				should.not.exist(error);
				numTables = rows.length;
			};

			var numberAssert = function(error, rows, cols){
				should.not.exist(error);
				rows.length.should.be.equal(expectedNumTables);
			};

			this.query('show tables').execute(numberCallback, { async : false});
			expectedNumTables = numTables;
			this.query('show tables').execute(numberAssert, { async : false});

			var q1 = 'create table newTestTable0 (field1 integer primary key, field2 integer)';
			var q2 = 'create table newTestTable1 (field1 integer primary key, field2 integer)';
			this.query(q1).execute({ async : false });
			this.query(q2).execute({ async : false });

			expectedNumTables = numTables + 2;
			this.query('show tables').execute(numberAssert, { async : false});

			var q3 = 'drop table newTestTable0';
			var q4 = 'drop table newTestTable1';

			this.query(q3).execute({ async : false});
			this.query(q4).execute({ async : false});

			expectedNumTables = numTables;
			
			this.query('show tables').execute(numberAssert, { async : false});
			done();
			
		});
		
	});

	it('CRUD Test', function(done){
		var mysql = require('db-mysql');
		var db = new mysql.Database(mysqlConfig);
		db.should.be.ok;
		db.connect.should.be.ok;

		db.on('error', function (error){
			should.not.exist(error);
		});
				
		db.connect(function(error){
			should.not.exist(error);
			var q1 = 'create table CRUDTestTable (f1 integer primary key, f2 boolean, f3 varchar(255))';
			this.query(q1).execute(function(){}, { async : false });

			var numRows;
			var errCheck = function (err, rows, cols){
				should.not.exist(err);
			};
			
			var getNumRows = function(err, rows, cols){
				should.not.exist(err);
				numRows = rows.length ? rows.length : 0;
			};

			this.query().delete().from('CRUDTestTable').where("f1 != -1").execute(getNumRows, { async : false});

			this.query().select('*').from('CRUDTestTable').execute(getNumRows, { async : false});
			numRows.should.be.equal(0);
			
			this.query().insert('CRUDTestTable',
								['f1', 'f2', 'f3'],
								[[1, true, 'Test 1'],
								 [2, false, 'Test 2'],
								 [3, true, 'To Be changed']
								]).execute(getNumRows, { async : false});

			this.query().select('*').from('CRUDTestTable').execute(getNumRows, { async : false});
			numRows.should.be.equal(3);

			this.query().update('CRUDTestTable').
				set({'f3' : 'Test 3'}).
				where('f1 = 3').
				execute(errCheck, { async : false});

			this.query().select('*').from('CRUDTestTable').where('f1 = 3').execute(function(err, rows, cols){
				should.not.exist(err);
				rows.length.should.be.equal(1);
				var res = rows[0];
				res.f3.should.be.equal('Test 3');
			}, {async : false});
		
			
			this.query().delete().from('CRUDTestTable').execute(getNumRows, { async : false});
			numRows.should.be.equal(0);

			this.query('drop table CRUDTestTable').execute({async : false});

			this.query('describe CRUDTestTable').execute(function (err, rows, cols){
				err.should.be.ok;
			}, {async : false});
			
			done();
		});
	});
});

