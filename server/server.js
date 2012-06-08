var express = require('express');
var fs = require('fs');

var authentication = require('./lib/authentication.js').authentication;
var buildDataStore = require('./lib/datastore.js').buildDataStore;
var logger = require('./lib/customLogger.js').getLogger();
var queries = require('./lib/sql.js').queries;

var TourManager = require('./managers/tourManager.js').TourManager;
var UserManager = require('./managers/userManager.js').UserManager;
var SearchManager = require('./managers/searchManager.js').SearchManager;

var port = 8787;

process.argv.forEach(function(arg, index, array){
    if (arg.toLowerCase() == '--port' || arg.toLowerCase() == '-p'){
        port = parseInt(array[index+1], 10);
    }
});

app = express.createServer();

var store  = new express.session.MemoryStore();

app.configure(function(){
    app.use(express.errorHandler({dumpExceptions : true, showStack : true}));
    app.use(express.bodyParser());
    app.use(express.cookieParser());
    app.use(express.session({ secret: 'MobilDocent Cookie Secret',
                              cookie: { maxAge: 30*24*60*60*1000 },
                              store : store
                            }));
    app.use(buildDataStore);
    app.use(authentication);
    app.use(function(req, res, callback){
        logger.info('Request for '+req.url);
        callback();
    });
    app.use(app.router);
});

app.get('/mongoFile', function(req, res){
    var mongoFile = req.query.mongoFile;
    if (!mongoFile){
        logger.error('Missing Required parameter for node file');
        return res.send({'error' : 'Missing Required Parameters'});
    }
    
    req.ds.mongoGrid(mongoFile, function(err, gs){
        if (err) return res.send(err);
        res.contentType(gs.contentType);
        /*gs.read(function(err, data){
            err ? res.send(err): res.send(data);
        });*/

        var stream = gs.stream(true);
        stream.on('data', function (data){
            res.write(data);
        });
        stream.on('error', function (err){
            gs.close(function(){
                res.end(err);
            });
        });
        stream.on('end', function(){
            gs.close(function(){
                res.end();
            });
        });
    });
});


/* Get the user*/
app.get('/user', function(req, res){
    var userManager = new UserManager(req.ds);
    userManager.getUser(req.query, res.send);
});

/* Get a single tour item with all of the nodes and tags associated with it
   returns a single element array*/
app.get('/tour', function(req, res){
    var tourManager = new TourManager(req.ds);
    tourManager.getTour(req.query, res.send);
});

/* Gets an array of tours:
   q for full text query search,
   tourName for fuzzy name search 
   tagName for tag Only Search for tours
   user=true for tours created by authenticated user
   Will not return the nodes or the tags for these
   tours, use /tour and /tags to get them if you need
   them
*/
app.get('/tours', function(req, res){
    var searchManager = new SearchManager(req.ds);
    searchManager.search(req.query, res.send);
});


/* Get the Node Details */
app.get('/nodeContent', function(req, res){
    var tourManager = new TourManager(req.ds);
    tourManager.getNode(req.query, res.send);
});


app.get('/location', function(req, res){
    logger.info('Request for location');
    callback({'success' : 'get location',
              'data' : req.query});
});

app.get('/ipLocation', function(req, res){
    rest.getLocation(req.ds, null, function(data){ res.send(data);});
});

app.get('/tags', function(req, res){
    var searchManager = new SearchManager(req.ds);
    searchManager.getTags(req.query, res.send); 
});

/* Create a new user  Handled in Authentication step*/

/* Create a new tour */
app.post('/tour', function(req, res){
    var tourManager = new TourManager(req.ds);
    tourManager.createTour(req.body, res.send);
});

/* Add a node to the tour given by the tourId and userId */
app.post('/node', function(req, res){
    var tourManager = new TourManager(req.ds);
    logger.warn(req.body);
    tourManager.createNode(req.body, req.files, res.send);
});

app.post('/tags', function(req, res){
    var searchManager = new SearchManager(req.ds);
    searchManager.addTag(req.body, res.send);
});

app.post('/tagTour', function(req, res){
    var searchManager = new SearchManager(req.ds);
    searchManager.addTagToTour(req.body, res.send);
});

/* Modification APIs */
app.post('/modifyTour', function(req, res){
    var tourManager = new TourManager(req.ds);
    tourManager.modifyTour(req.body, res.send);
});

app.post('/modifyNode', function(req, res){
    logger.info('Modify Tour Request');
    tourManager.modifyNode(req.body, req.files, res.send);
});

/* Destructive API */

app.post('/deleteTour', function(req, res){
    var tourManager = new TourManager(req.ds);
    tourManager.deleteTour(req.body, res.send);
});

app.post('/deleteNode', function(req, res){
    var tourManager = new TourManager(req.ds);
    tourManager.deleteNode(req.body, res.send);
});

app.post('/deleteTag', function(req, res){
    var searchManager = new SearchManager(req.ds);
    searchManager.deleteTourTag(req.body, res.send);
});


/* TEST functions */
app.post('/echo', function (req, res){
    logger.info('Data Received for echo');
    logger.info(JSON.stringify(req.body));
    req.body.authUserId = undefined;
    //logger.info(req.socket.remoteAddress);
    res.send({'success' : req.body});
});


app.all(/.*/, function (req, res){
    var msg = {'error' : 'This request fell through', 'url' : req.url} ;
    logger.info(msg);
    res.send(msg, 404);
});

app.listen(port, function(){
    logger.info('Server is now running on PORT:'+port);
});
