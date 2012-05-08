var express = require('express');
var authentication = require('./authentication.js').authentication;
var buildDataStore = require('./datastore.js').buildDataStore;
var port = 8787;

process.argv.forEach(function(arg, index, array){
	if (arg.toLowerCase() == '--port' || arg.toLowerCase() == '-p'){
		port = parseInt(array[index+1], 10);
	}
});

app = express.createServer();

app.configure(function(){
	app.use(express.errorHandler({dumpExceptions : true, showStack : true}));
	app.use(authentication);
	app.use(app.router);
	app.use(express.bodyParser());
	app.use(express.cookieParser());
	app.use(express.session({ secret: 'MobilDocent Cookie Secret',
							  cookie: { maxAge: 30*60*60*1000 } }));
	app.use(buildDataStore);
});





