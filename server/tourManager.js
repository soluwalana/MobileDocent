var fs = require('fs');
var uuid = require('node-uuid');
var _ = require('./lib/underscore.js')._;

var constants = require('./constants.js');
var queries = require('./sql.js').queries;
var multiQuery = require('./sql.js').multiQuery;
var logger = require('./customLogger.js').getLogger();

var errorCallback = function (message, callback){
    logger.error(message, 1);
    callback({'error' : message});
}

var addWarning = function (result, err){
    result.status = constants.WARN_STATUS;
    if (!result.message instanceof Array){
        result.message = [];
    }
    result.message.push(err);
};

var convertContent = function (nodeContent){
    if (!nodeContent){
        return null;
    }
    var node = {};
    node.pageLayout = [];
    node.pages = {};
    node.sections = {};
    for (var i = 0; i < nodeContent.length; i ++){
        var page = [];
        var pageId = uuid.v4();
        for (var j = 0; j < nodeContent[i].length; j++){
            var sectionId = uuid.v1();
            var section = nodeContent[i][j];
            node.sections[sectionId] = section;
            page.push(sectionId);
        }
        node.pages[pageId] = page;
        node.pageLayout.push(pageId);
    }
    return node;
};


var TourManager = function(store){
    var self = this;
    
    self.init = function (){
        self.store = store;
    }

    
    self.createTour = function (params, callback){
        if (!params.tourName || !params.description || !params.authUserId){
            errorCallback('Required Fields Missing From Tour Creation', callback);
            return null;
        }
        
        self.store.sqlConn(function (err, conn){
            if (err){
                callback(err);
                return null;
            }
            var sql = queries.insertTour;
            var sqlParams = [params.authUserId, params.tourName, params.description, 0];
            conn.query(sql, sqlParams).execute(
                function (err, result){
                    if (err){
                        errorCallback('Tour Name Already Created', callback);
                        return null;
                    }
                    callback({'success' : 'Tour Created',
                              'tourId' : result.id});
                }
            );
        });
    };

    self._createNode = function (nodeData, files, conn, callback){
        var numberCommitted = 0;
        var numberSubmitted = 0;
        var result = {
            'status' : constants.GOOD_STATUS,
            'message' : ''
        };

        var fileCallback = function(err, sectionId, name){
            numberCommitted ++;
            if (err){
                addWarning(result, err);
                sections[sectionId].contentId = constants.INVALID_FILE;
                logger.warn('Commit Called but file was missing');
            } else {
                sections[sectionId].contentId = name;
            }
            
            if (numberCommitted == numberSubmitted){
                logger.debug('All Files Done');
                self._finishNodeAppend(nodeData, result, conn, callback);
            }
        };
        
        nodeData.content = convertContent(nodeData.content);
        var nodeContent = nodeData.content;
        var sections = nodeContent.sections || {};
        
        if (!nodeContent && !nodeData.brief){
            // Pseudo Node 
            self._finishNodeAppend(nodeData, result, conn, callback);
            return null;
        }

        var filesMetaData = [];
        for (var key in sections){
            if (sections[key].contentId){
                var fileId = sections[key].contentId;
                filesMetaData.push({'sectionId' : key , 'fileId' : fileId});
                numberSubmitted ++;
            }
        }
        
        if (nodeData.brief && nodeData.brief.thumbId){
            var thumbId = nodeData.brief.thumbId;
            sections.thumb = {};
            if (!files[thumbId]){
                addWarning(result, 'ThumbNail Image Not uploaded');
                sections.thumb.contentId = constants.INVALID_FILE;
            } else {
                numberSubmitted ++;
                var type = files[thumbId].contentType;
                var fileMeta = {'fileId' : thumbId, 'sectionId' : 'thumb',
                                'type' : type}
                self._storeFile(files, fileMeta, fileCallback);
            }
        }

        // No Files uploaded all static text or html
        if (numberSubmitted === 0){
            self._finishNodeAppend(nodeData, result, conn, callback);
            return null;
        }

        for (var i = 0; i < filesMetaData.length; i ++){
            var fileMeta = filesMetaData[i];
            var sectionId = fileMeta.sectionId;
            fileMeta.type = nodeContent.sections[sectionId].contentType;
            if (!files[fileMeta.fileId]){
                commit({'error' : 'File Was Not Uploaded'});
                continue;
            }
            self._storeFile(files, fileMeta, fileCallback);
        }
    };
    
    
    self.createNode = function (params, files, callback){
        logger.info('File Upload Recieved');
        var nodeData = params.nodeData;
        if (nodeData && typeof(nodeData) === 'string'){
            try{
                nodeData = JSON.parse(nodeData);
            } catch (err){
                errorCallback('Node Data is not an object', callback);
                return null;
            }
        }
        
        if (!nodeData || !nodeData.latitude || !params.authUserId ||
            !nodeData.longitude || !nodeData.tourId){
            logger.warn(nodeData, nodeData.latitude, params.authUserId, nodeData.longitude, nodeData.tourId);
            errorCallback('Missing Required Parameters', callback);
            return null;
        }
        self._verifyOwnership(
            [params.authUserId, nodeData.tourId],
            queries.checkTourOwnership,
            function(err, conn){
                if(!err && conn){
                    self._createNode(nodeData, files, conn, callback);
                    return null;
                }
                errorCallback(err, callback);
            }
        );                    
    };

    self._finishNodeAppend = function (nodeData, result, conn, callback){
        var commitToDB = function(){
            
        };
        var nodeContent = nodeData.content;
        var latitude = nodeData.latitude;
        var longitude = nodeData.longitude;
        var tourId = nodeData.tourId;
        var pseudo = (!nodeContent && !nodeData.brief) ? 1 : 0;

        var sql = queries.appendNode;
        var sqlParams = sqlParams = [latitude, longitude, pseudo, tourId];
        
        if (nodeData.brief && nodeData.brief.thumbId){
            nodeData.brief.thumbId = nodeData.content.sections.thumb.contentId;
            delete nodeData.content.sections.thumb;
        }
        
        multiQuery(conn, sql, sqlParams, function (err, result){
            if (err){
                errorCallback(err, callback);
                return null;
            }

            if (result.length === 0){
                errorCallback('SQL Query Failed', callback);
                return null;
            }
            var result = result[0];
                        
            var mongoObject = {};
            if (nodeData.brief){
                mongoObject.brief = nodeData.brief;
            }

            if (nodeData.content){
                mongoObject.content = nodeData.content;
            }

            if (_.isEmpty(mongoObject)){
                callback({'success': 'committed pseudo node',
                          'result' : result});
                return null;
            }
            store.mongoCollection(
                constants.NODE_COLLECTION,
                function (err, collection){
                    if (err){
                        var msg = 'Couldnt open Mongo Collection, MySQL modified!';
                        errorCallback(msg, callback);
                        return null;
                    }
                    collection.insert(
                        mongoObject, {'safe' : true },
                        function(err, records){
                            if (err || records.length === 0){
                                var msg = 'Couldnt save to mongo, MySQL modified!';
                                errorCallback(msg, callback);
                                return null;
                            }
                            var mongoId = JSON.stringify(records[0]._id);
                            mongoId = mongoId.replace(/"/g, '')
                            var nodeId = result.nodeId;
                            conn.query(queries.bindMongoToSql, [mongoId, nodeId]).
                                execute(function (err, rows, cols){
                                    if (err){
                                        errorCallback(err, callback);
                                        return null;
                                    }
                                    result.mongoId = mongoId;
                                    callback({'success' : 'Node Created',
                                              'result': result});
                                }
                            );
                        }
                    );
                }
            );
        });
    };

    self._modifyTour = function (params, conn, callback){

    };
    
    self.modifyTour = function (params, callback){
        if (!params.tourId || !params.authUserId){
            errorCallback('Missing Required Parameters', callback);
            return null;
        }
        self._verifyOwnership(
            [params.authUserId, params.tourId],
            queries.checkTourOwnership,
            function(err, conn){
                if(!err && conn){
                    self._modifyTour(params, conn, callback);
                }
                errorCallback(err, callback);
            }
        );
    };

    self._modifyNode = function (params, conn, callback){
        
    };
    
    self.modifyNode = function (params, callback){
        if (!params.tourId || !params.authUserId || !params.nodeId){
            errorCallback('Missing Required Parameters', callback);
            return null;
        }

        self._verifyOwnership(
            [params.authUserId, params.tourId, params.nodeId],
            queries.checkNodeOwnership,
            function(err, conn){
                if(!err && conn){
                    self._modifyTour(params, conn, callback);
                }
                errorCallback(err, callback);
            }
        );
    };
    
    self._storeFile = function (files, data, callback){
        var fileId = data.fileId;
        var sectionId = data.sectionId;
        var type = data.type;
        
        self.store.mongoGrid(type, function(err, gs, name){
            if (err){
                callback({'error' : fileId+' couldnt be made'});
                return null;
            }
            var fileStream = fs.createReadStream(files[fileId].path, {
                'flags' : 'r',
                'encoding' : 'utf8',
                'bufferSize' : constants.FILE_BUF_SIZE
            });
            
            fileStream.addListener('data', function (chunk){
                fileStream.pause();
                gs.write(chunk, function(err, result){
                    if (err){
                        fs.unlinkSync(files[fileId].path);
                        gs.close();
                        callback({'error' : fileId+' didnt write to GridFS'});
                        return null;
                    }
                    fileStream.resume();
                });
            });
            
            fileStream.addListener('close', function(){
                fs.unlinkSync(files[fileId].path);
                gs.close(function(){
                    callback(null, sectionId, name);
                });
            });
        });
    };

    self._verifyOwnership = function (args, sql, callback){
        self.store.sqlConn(function(err, conn){
            conn.query(sql, args).execute(function (err, rows, cols){
                if (err){
                    errorCallback(err, callback);
                    return null;
                }
                if (rows.length === 0){
                    errorCallback('User Doesnt own this tour', callback);
                    return null;
                }
                callback(null, conn);
            });
        });
    };

    self.init();
    return self;
};

exports.TourManager = TourManager;



    