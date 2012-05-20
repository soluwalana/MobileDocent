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
	/*for ( prop in this.query()){
		console.log(prop);
	}*/
		
	this.query('create table newTable (field1 integer not null primary key, field2 integer not null)').execute();
	this.query('create table newTable1 (field3 integer not null primary key, field4 integer not null)').execute();
	this.query('show tables').execute(function(err, rows, cols){
		console.log(rows.length);
		console.log(cols);
	});
	this.query('insert into newTable values (1, 2), (3, 4), (5, 6)').execute();
	/*this.query('select * from newTable').execute(function(err, rows, cols){
		console.log(rows);
		console.log(cols);
	});
    this.query('select * from newTable;').execute(function(err, rows, cols){
        console.log(this.sql());
        console.log(err);
        console.log(rows);
		console.log(cols);
    });
*/
    var num = 1000000;
    var start = new Date().getTime();
    var tempQuery = this.query('insert into newTable (field1, field2) values ');
    for (var i = 10; i < num; i ++){
        var add = null;
        if (i > 10){
            add =  ',('+i+', 100)'
        } else {
            add =  '('+i+', 100)'
        }
        tempQuery.add(add);
    }
    
    var db = this;
    tempQuery.execute(function(err, rows, cols){
        //console.log(rows);

        console.log('Time to insert '+num+': '+(new Date().getTime() - start))
        var start1 = new Date().getTime();
        var create = 'create temporary table tempJoin (joinId integer primary key)';
        db.query(create).execute(function(err, result){

            var insertQuery = 'insert into tempJoin values ';
            var tQuery= db.query(insertQuery);
            for (var i = 10; i < num; i ++){
                var add = '('+i+')';
                //if (i > 10) insertQuery += ', ';
                //insertQuery += '('+i+')';
                if (i > 10) add = ', '+add;
                tQuery.add(add);
            }
            
            tQuery.execute(function(err, rows, cols){
                var start2 = new Date().getTime();
                console.log('Time to insert '+num+' into tempJoin: '+(new Date().getTime() - start1));
                var join  = 'select field1, field2 from newTable where field1 in (select joinId from tempJoin)';
                var join2 = 'select field1, field2 from newTable, tempJoin where field1 = joinId';
                var join3 = 'select * from newTable';
                var tQuery2 = db.query(join);
                tQuery2.limit(10000);
                tQuery2.execute(
                    function (err, rows, cols){
                        console.log('Time to join '+(new Date().getTime() - start2));
                        var end = new Date().getTime();
                        console.log(rows.length);
		                console.log('Return Times : '+(end-start))
                        db.query('drop table newTable').execute();
	                    db.query('drop table newTable1').execute();
                    }
                );
            });
            
        });

    });
});