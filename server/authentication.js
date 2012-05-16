var logger = require('./customLogger.js').getLogger();

var LOGIN_URL = '/login';
var LOGOUT_URL = '/logout';
var CREATE_URL = '/user';

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
	} else {
		/* Make sure authenticated and then continue */
		req.ds.sessionAuthenticate(req.session, function(err, userId){
			if (err){
				res.send(err);
				return null;
			}
			callback();
		});
	}
};

exports.authentication = authentication;