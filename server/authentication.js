var LOGIN_URL = '/login';
var LOGOUT_URL = '/logout';

var authentication = function(req, res, callback){
	if (req.method === 'POST' && req.url === LOGIN_URL){
		/* login */
	} else if (req.url === LOGOUT_URL){
		/* logout */
	} else {
		/* Make sure authenticated and then continue */
	}
};

exports.authentication = authentication;