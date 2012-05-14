var express = require('express');
var authentication = require('./authentication.js').authentication;
var buildDataStore = require('./datastore.js').buildDataStore;
var logger = require('./customLogger.js').getLogger();
var rest = require('./rest.js').rest;
var port = 8787;

process.argv.forEach(function(arg, index, array){
	if (arg.toLowerCase() == '--port' || arg.toLowerCase() == '-p'){
		port = parseInt(array[index+1], 10);
	}
});

app = express.createServer();

app.configure(function(){
	app.use(express.errorHandler({dumpExceptions : true, showStack : true}));
    app.use(express.bodyParser());
    app.use(express.cookieParser());
    app.use(buildDataStore);
    app.use(authentication);
	app.use(express.session({ secret: 'MobilDocent Cookie Secret',
							  cookie: { maxAge: 30*60*60*1000 } }));
	app.use(app.router);
});

/* Get the user*/
app.get('/user/:id', function(req, res){
    rest.getUser(req.ds, req.params, function(data){ res.send(data);});
});

/* Get the tour item*/
app.get('/tour/:id', function(req, res){
    rest.getTour(req.ds, req.params, function(data){ res.send(data);});
});

/* Get the Node Details */
app.get('/node/:id', function(req, res){
    rest.getNode(req.ds, req.params, function(data){ res.send(data);});
});

app.post('/location', function(req, res){
    rest.getLocation(req.ds, req.params, function(data){ res.send(data);});
});

app.get('/ipLocation', function(req, res){
    rest.getLocation(req.ds, null, function(data){ res.send(data);});
});

/* Create a new user */
app.post('/user', function(req, res){
    rest.createUser(req.ds, req.body, function(data){ res.send(data);});
});

/* Create a new tour */
app.post('/tour/:userId', function(req, res){
	rest.createTour(req.ds, req.body, function(data){ res.send(data);});
});

/* Add a node to the tour given by the tourId and userId */
app.post('/node/:tourId/:userId', function(req, res){
    rest.createNode(req.ds, req.body, function(data){ res.send(data);});
});


/* TEST functions */
app.post('/echo', function (req, res){
    logger.info('Data Received for echo');
    logger.info(req.socket.remoteAddress);
    res.send({'success' : 'got data'});
});

app.all('/test', function(req, res){
	logger.info('Got Request for test');
    req.ds.test(function(err, data){
		res.send({'success' : 'Ran',
				  'data' : data});
	});
});

app.all(/.*/, function (req, res){
    var msg = {'error' : 'This request fell through',
               'url' : req.url} 
    console.log(msg);
    res.send(msg);
});

app.listen(port, function(){
	logger.info('Server is now running on PORT:'+port);
	
});
