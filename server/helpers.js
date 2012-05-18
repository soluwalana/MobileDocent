var crypto = require('crypto');
var logger = require('./customLogger.js').getLogger(1);

var SHA1_KEY = 'Docent_2012_SHA1_Private';
var SALT_POSSIBLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
var SALT_LENGTH = 8;

exports.ipToLong = function(addr){
    addr = addr.split('.');
    
    var ipNumber = 16777216 * parseInt(addr[0]) + 65536 * parseInt(addr[1]) +
        256 * parseInt(addr[2]) + parseInt(addr[3]);
    console.log(ipNumber);
    return ipNumber;
};

exports.generateSalt = function(){
	var text = "";
	for (var i = 0; i < SALT_LENGTH; i++){
		text += SALT_POSSIBLE.charAt(Math.floor(Math.random() * SALT_POSSIBLE.length));
	}
	return text;
};

exports.generatePassword = function(password, salt){
	var hmac = crypto.createHmac('sha1', SHA1_KEY);
	var hash = hmac.update(password + salt);
	return hmac.digest(encoding = 'base64');
};
