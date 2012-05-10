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
	app.use(express.bodyParser());
	app.use(express.cookieParser());
	app.use(express.session({ secret: 'MobilDocent Cookie Secret',
							  cookie: { maxAge: 30*60*60*1000 } }));
	app.use(buildDataStore);
	app.use(app.router);
});


/* Get the user*/
app.get('/user/:id', function(req, res){
	res.send({'success' : 'Ran',
			  'data' : req.params.id});
});


/* Get the tour item*/
app.get('/tour/:id', function(req, res){
	res.send({'success' : 'got tour request',
			  'data' : req.params.id});
});

/* Get the Node Details */
app.get('/node/:id', function(req, res){
	res.send({ 'success' : 'get node by id',
			   'data' : req.params.id});
});

/* Create a new user */
app.post(/user/, function(req, res){
	res.send({'success' : 'got user create request',
			  'data' : req.body});
});

/* Create a new tour */
app.post(/tour/, function(req, res){
	res.send({'success' : 'got tour create request',
			  'data' : req.body});
});

/* Add a node to the tour given by the tourId and userId */
app.post('/node/:tourId/:userId', function(req, res){
	res.send({'success' : 'add a node to the tour',
			  'data' : req.body,
			  'params' : req.params});
});

app.all(/.*/, function(req, res){
	console.log('Got Request for root');
	req.ds.test(function(err, data){
		res.send({'success' : 'Ran',
				  'data' : data});
	});
});


app.listen(port, function(){
	console.log('We are now receiving request');
});
