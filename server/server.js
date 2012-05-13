var express = require('express');
var authentication = require('./authentication.js').authentication;
var buildDataStore = require('./datastore.js').buildDataStore;
var logger = require('./customLogger.js').getLogger();
var port = 8787;

process.argv.forEach(function(arg, index, array){
	if (arg.toLowerCase() == '--port' || arg.toLowerCase() == '-p'){
		port = parseInt(array[index+1], 10);
	}
});

app = express.createServer();

app.configure(function(){
	app.use(express.errorHandler({dumpExceptions : true, showStack : true}));
	app.use(buildDataStore);
	app.use(authentication);
	app.use(express.bodyParser());
	app.use(express.cookieParser());
	app.use(express.session({ secret: 'MobilDocent Cookie Secret',
							  cookie: { maxAge: 30*60*60*1000 } }));
	app.use(app.router);
});

/* Get the user*/
app.get('/user/:id', function(req, res){
	logger.info('User Request Received');
	res.send({'success' : 'Ran',
			  'data' : req.params.id});
});

/* Get the tour item*/
app.get('/tour/:id', function(req, res){
	logger.info('Tour Request Received');
	res.send({'success' : 'got tour request',
			  'data' : req.params.id});
});

/* Get the Node Details */
app.get('/node/:id', function(req, res){
	logger.info('Node Request Received');
	res.send({ 'success' : 'get node by id',
			   'data' : req.params.id});
});

app.get('/location', function(req, res){
    logger.info('Request for location');
    res.send({'success' : 'get location',
              'data' : req.params});
});

app.get('/ipLocation', function(req, res){
    logger.info('Request for location by IP');
    res.send({'success' : 'get location by ip',
              'data' : req.params});
});

/* Create a new user */
app.post(/user/, function(req, res){
	logger.info('User Creation Request Received');
	res.send({'success' : 'got user create request',
			  'data' : req.body});
});

/* Create a new tour */
app.post('/tour/:userId', function(req, res){
	logger.info('Tour Creation Request Received');
	res.send({'success' : 'got tour create request',
			  'data' : req.body});
});

/* Add a node to the tour given by the tourId and userId */
app.post('/node/:tourId/:userId', function(req, res){
	logger.info('Node Modification Request Received');
	res.send({'success' : 'add a node to the tour',
			  'data' : req.body,
			  'params' : req.params});
});


app.all(/.*/, function(req, res){
	logger.info('Got Request for root');
	req.ds.test(function(err, data){
		res.send({'success' : 'Ran',
				  'data' : data});
	});
});


app.listen(port, function(){
	logger.info('Server is now running on PORT:'+port);
	
});
