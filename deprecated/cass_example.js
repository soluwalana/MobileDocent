var helenus = require('helenus');
var pool = new helenus.ConnectionPool({
	hosts     : ['localhost:9160'],
	keyspace  : 'TestKeyspace',
	timeout   : 3000
});

var column_family_options =  {
    "key_validation_class"     : "UTF8Type",
    "comparator_type"          : "UTF8Type",
    "default_validation_class" : "UTF8Type",
    "columns" : [
		{ "name" : "bytes-test", "validation_class" : "BytesType" },
		{ "name" : "long-test", "validation_class" : "LongType" },
		{ "name" : "integer-test", "validation_class" : "IntegerType" },
		{ "name" : "utf8-test", "validation_class" : "UTF8Type" },
		{ "name" : "ascii-test", "validation_class" : "AsciiType" },
		{ "name" : "lexicaluuid-test", "validation_class" : "LexicalUUIDType" },
		{ "name" : "timeuuid-test", "validation_class" : "TimeUUIDType" },
		{ "name" : "float-test", "validation_class" : "FloatType" },
		{ "name" : "double-test", "validation_class" : "DoubleType" },
		{ "name" : "date-test", "validation_class" : "DateType" },
		{ "name" : "boolean-test", "validation_class" : "BooleanType" },
		{ "name" : "uuid-test", "validation_class" : "UUIDType" },
		{ "name" : "index-test", "validation_class" : "UTF8Type", "index_type":0 }
    ]
};

var simple_family =  {
    "key_validation_class"     : "UTF8Type",
    "comparator_type"          : "UTF8Type",
    "default_validation_class" : "UTF8Type",
    "columns" : [
		{ "name" : "long-col", "validation_class" : "LongType" },
		{ "name" : "utf8-col", "validation_class" : "UTF8Type" },
	]
};



pool.on('error', function(err){
	console.log(err);
});

pool.connect(function(err, keyspace){
	if (err){
		console.log(err);
		return null;
	} else {
		keyspace.createColumnFamily('test', simple_family);
	}

	keyspace.get('test', function(err, colFamily){
		if (err){
			console.log(err);
			return null;
		}

		colFamily.insert('key', {'long-col' : 19838529392143, 'utf8-col' : 'Stuff Is Fun'}, function(err){
			if(err){
				console.log(err);
				return null;
			}

			colFamily.get('key', function (err, row){
				if (err){
					console.log(err);
					return null;
				}
				console.log(row.get('long-col'));
				console.log(row.get('utf8-col'));
			});
		});
	});
});

