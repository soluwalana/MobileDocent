var log4js = require('log4js');

var getFileLineColumn = function (basic, message, up){
	try {(0)()} catch (e){
		// Remove the error message
		var stack = e.stack.replace(/^(?:.*?\n){4}/, '');
		// Remove garbage
		stack = stack.replace(/(?:\n@:0)?\s+$/m, '');
		// Give anonymous functions more info
		stack = stack.replace(/^\(/gm, '{anon}(');
		// Make it an array
		stack = stack.split('\n');
        var idx = 0;
        if (up && up > 0){
            idx = up;
        }
        var file = stack[idx].replace(/^.*\//, '').replace(/(\(|\))/g, '');
		file = file.split(':');
		if (!basic){
            var print_message = message;
            if (typeof(message) === 'object'){
                try{
                    print_message = JSON.stringify(message, null, 1);
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
	var fileInfo = getFileLineColumn(true, null);
	var logger = log4js.getLogger(fileInfo[0]);
	var wrappedLogger = {};

	wrappedLogger.debug = function(message, up){
		logger.debug(getFileLineColumn(false, message, up));
	};

	wrappedLogger.info = function(message, up){
		logger.info(getFileLineColumn(false, message, up));
	};

	wrappedLogger.warn = function(message, up){
		logger.warn(getFileLineColumn(false, message, up));
	};

	wrappedLogger.error = function(message, up){
		logger.error(getFileLineColumn(false, message, up));
	};

	wrappedLogger.fatal = function(message, up){
		logger.fatal(getFileLineColumn(false, message, up));
	};

	return wrappedLogger;
};