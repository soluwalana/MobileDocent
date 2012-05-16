var log4js = require('log4js');

var getFileLineColumn = function (message, basic){
	try {(0)()} catch (e){
		// Remove the error message
		var stack = e.stack.replace(/^(?:.*?\n){4}/, '');
		// Remove garbage
		stack = stack.replace(/(?:\n@:0)?\s+$/m, '');
		// Give anonymous functions more info
		stack = stack.replace(/^\(/gm, '{anon}(');
		// Make it an array
		stack = stack.split('\n');

		var file = stack[0].replace(/^.*\//, '').replace(/(\(|\))/g, '');
		file = file.split(':');
		if (!basic){
            var print_message = message;
            if (typeof(message) === 'object'){
                try{
                    print_message = JSON.stringify(message);
                } catch (e){
                    print_message = '[Circular Object] (use console.log)';
                }
            }
			return "("+file[1]+":"+file[2]+") - "+print_message;
		} else {
			return file;
		}
	}
};

exports.getLogger = function(){
	var fileInfo = getFileLineColumn(null, true);
	var logger = log4js.getLogger(fileInfo[0]);
	var wrappedLogger = {};

	wrappedLogger.debug = function(message){
		logger.debug(getFileLineColumn(message));
	};

	wrappedLogger.info = function(message){
		logger.info(getFileLineColumn(message));
	};

	wrappedLogger.warn = function(message){
		logger.warn(getFileLineColumn(message));
	};

	wrappedLogger.error = function(message){
		logger.error(getFileLineColumn(message));
	};

	wrappedLogger.fatal = function(message){
		logger.fatal(getFileLineColumn(message));
	};

	return wrappedLogger;
};