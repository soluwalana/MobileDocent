var log4js = require('log4js');

var getLineNumber = function (up){
	var idx = up ? up : 0;
	try {(0)()} catch (e){
		// Remove the error message
		var stack = e.stack.replace(/^(?:.*?\n){4}/, '');
		// Remove garbage
		stack = stack.replace(/(?:\n@:0)?\s+$/m, '');
		// Give anonymous functions more info
		stack = stack.replace(/^\(/gm, '{anon}(');
		// Make it an array
		stack = stack.split('\n');
		file = stack[idx].replace(/^.*\//, '').replace(/(\(|\))/g, '');
		file = file.split(':');
		return file;
	}
	return null;
};

var getFileLineColumn = function (message, lineInfo){
	var print_message = message;
	if (typeof(message) === 'object'){
		try {print_message = JSON.stringify(message, null, 1);}
		catch (e) {print_message = '[Circular Object] (use console.log)';}
	}
	return "("+lineInfo[1]+":"+lineInfo[2]+") - "+print_message;
};

exports.getLineNumber = function (up){
	up = up ? up : 1;
	return getLineNumber(up)
};

exports.getLogger = function(){
	var fileInfo = getLineNumber();
	var logger = log4js.getLogger(fileInfo[0]);
	var wrappedLogger = {};

	wrappedLogger.debug = function(message, lineInfo){
		lineInfo = lineInfo ? lineInfo : getLineNumber();
		logger.debug(getFileLineColumn(message, lineInfo));
	};

	wrappedLogger.info = function(message, lineInfo){
		lineInfo = lineInfo ? lineInfo : getLineNumber();
		logger.info(getFileLineColumn(message, lineInfo));
	};

	wrappedLogger.warn = function(message, lineInfo){
		lineInfo = lineInfo ? lineInfo : getLineNumber();
		logger.warn(getFileLineColumn(message, lineInfo));
	};

	wrappedLogger.error = function(message, lineInfo){
		lineInfo = lineInfo ? lineInfo : getLineNumber();
		logger.error(getFileLineColumn(message, lineInfo));
	};

	wrappedLogger.fatal = function(message, lineInfo){
		lineInfo = lineInfo ? lineInfo : getLineNumber();
		logger.fatal(getFileLineColumn(message, lineInfo));
	};

	return wrappedLogger;
};