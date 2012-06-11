var logger = require('./customLogger.js').getLogger();

var LOGIN_URL = '/login';
var LOGOUT_URL = '/logout';
var CREATE_URL = '/user';
var FILE_ACCESS = '/mongoFile';

var authentication = function(req, res, callback){
	if (req.method === 'POST' && req.url === LOGIN_URL){
		/* login */
        req.ds.authenticate(req.body, function(err, userId){
			if (err){
				res.send(err);
				return null;
			}
			req.session.authenticated = true;
			req.session.userId = userId;
			res.send({'success' : 'Successfully authenticated'});
			
		});
	} else if (req.method === 'POST' && req.url === CREATE_URL){
        req.ds.addUser(req.body, function(err, userId){
			if (err){
				res.send(err);
				return null;
			} 
			req.session.authenticated = true;
			req.session.userId = userId;
			res.send({'success' : 'User Created Successfully'});
		});
	} else if (req.url === LOGOUT_URL){
		/* logout */
		req.session.destroy();
		res.send({'success' : 'Logged Out'});
	} else if ((/^\/mongoFile\?.*$/).test(req.url)){
        /* Hack for now until we get the stuff working on client */
        req.ds.allowFileAccess();
        callback();
    } else {
		/* Make sure authenticated and then continue */
		req.ds.sessionAuthenticate(req.session, function(err, userId){
			if (err){
				res.send(err);
				return null;
			}
            if (req.params) req.params.authUserId = userId;
            if (req.body) req.body.authUserId = userId;
            if (req.query) req.query.authUserId = userId;
			callback();
		});
	}
};

exports.authentication = authentication;