var fs = require('fs');
var uuid = require('node-uuid');
var _ = require('./lib/underscore.js')._;


var constants = require('./constants.js');
var queries = require('./sql.js').queries;
var multiQuery = require('./sql.js').multiQuery;
var logger = require('./customLogger.js').getLogger();
var getLineNum = require('./customLogger.js').getLineNumber;

var errorCallback = function (message, callback, lineInfo){
	logger.error(message, lineInfo);
    callback({'error' : message});
}

var errorWrap = function (retCallback, callback){
	var lineInfo = getLineNum();
	return function (err, res, extra){
		if (err){
			return errorCallback(err, retCallback, lineInfo);
		}
		callback(res, extra);
	};
};

var addWarning = function (result, err){
    result.status = constants.WARN_STATUS;
    if (!(result.message instanceof Array)){
        result.message = [];
    }
    result.message.push(err);
};

/* Convert The Content of Nodes into an easy to
   manipulate set of values.
   @param {array} nodeContent - Expected to be of the form
   [ [{}, {}], [{},{}], [{}, {}] ]
   where the top array describes the node the second
   level describes the page and the lowest level describes
   a section*/
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

/* Will take the result from convertContent and convert it
   into an easy to query mongo object of the form
   [{'pageId' : xxxx, 'page' : sections : [{'sectionId' : xxxx, 'section' : {}}}], ...]
   */
var convertToMongo = function (nodeContent){
    if (!nodeContent){
        return null;
    }
    var mongoObject = [];
    var pageLayout = nodeContent.pageLayout;
    var pages = nodeContent.pages;
    for (var i = 0; i < pageLayout.length; i++){
        var sections = [];
        var page = pages[pageLayout[i]];
        for (var j = 0; j < page.length; j ++){
            var section = nodeContent.sections[page[j]];
            section.sectionId = page[j];
            sections.push(section);
        }
        mongoObject.push({'pageId' : pageLayout[i],
                          'page' : sections});
    }
    return mongoObject;
}

var TourManager = function(store){
    var self = this;
    
    self.init = function (){
        self.store = store;
    }

    
    self.createTour = function (params, callback){
        if (!params.tourName || !params.description || !params.authUserId){
            return errorCallback('Required Fields Missing From Tour Creation', callback);
        }
        
        self.store.sqlConn(errorWrap(callback, function (conn){
            var sql = queries.insertTour;
            var sqlParams = [params.authUserId, params.tourName, params.description, 0];
            conn.query(sql, sqlParams).execute(errorWrap(callback, function (result){
                callback({'success' : 'Tour Created',
                          'tourId' : result.id});
            }));
        }));
    };

    self.getTour = function (params, callback){
        if (!params.tourId && !params.tourName){
            return errorCallback('Required Fields Missing From Get Tour', callback);
        }
		
        self.store.sqlConn(errorWrap(callback, function (conn){
            var sql = params.tourId ? queries.getTourById: queries.getTourByName;
            var sqlParams = [params.tourId ? params.tourId : params.tourName];
            conn.query(sql, sqlParams).execute(errorWrap(
				callback, function (rows, cols){
					if (rows.length === 0){
						return errorCallback('Tour Doesnt exist or has no nodes', callback);
					}
					var retObj = {};
					retObj.tourId = rows[0].tourId;
					retObj.userId = rows[0].userId;
					retObj.tourName = rows[0].tourName;
					retObj.description = rows[0].description;
					retObj.locId = rows[0].locId;
					retObj.nodes = [];
					
					var nodes = {};
					var curNode = null;
					for (var i = 0; i < rows.length; i ++){
						nodes[rows[i].nodeId] = {
							nodeId : rows[i].nodeId,
							latitude : rows[i].latitude,
							longitude: rows[i].longitude,
							prevNode : rows[i].prevNode,
							nextNode : rows[i].nextNode,
							pseudo: rows[i].pseudo,
							mongoId: rows[i].mongoId
						};
						if (rows[i].prevNode == null) curNode = rows[i].nodeId;
					}
					retObj.nodes.push(nodes[curNode]);
					while(nodes[curNode].nextNode){
						curNode = nodes[curNode].nextNode;
						retObj.nodes.push(nodes[curNode]);
					}
                    
					callback({'success' : 'Tour Retrieved',
							  'tour' : retObj});
				}
			));
		}));
	};


    self._modifyTour = function (params, conn, callback){

    };
    
    self.modifyTour = function (params, callback){
        if (!params.tourId || !params.authUserId){
            return errorCallback('Missing Required Parameters', callback);
        }
        self._verifyOwnership(
            [params.authUserId, params.tourId],
            queries.checkTourOwnership,
            errorWrap(callback, function(conn){
                self._modifyTour(params, conn, callback);
                
            })
        );
    };

    
    self._createNode = function (nodeData, files, conn, callback){
		var numberCommitted = 0;
        var numberSubmitted = 0;
        files = files || {};
        var sections = {};
                
        var result = {
            'status' : constants.GOOD_STATUS,
            'message' : ''
        };

        var fileCallback = function(err, sectionId, name){
            numberCommitted ++;
            if (err){
                addWarning(result, err);
				logger.warn(sections);
				logger.warn(sectionId);
                sections[sectionId].contentId = constants.INVALID_FILE;
                logger.warn('Commit Called but file was missing');
            } else {
                sections[sectionId].contentId = name;
            }
            
            if (numberCommitted == numberSubmitted){
                logger.debug('All Files Done');
                self._finishNodeAppend(nodeData, sections, result, conn, callback);
            }
        };
        
        nodeData.content = convertContent(nodeData.content);
        var nodeContent = nodeData.content;
        sections = nodeContent ? nodeContent.sections : {};
        
        if (!nodeContent && !nodeData.brief){
            // Pseudo Node
            return self._finishNodeAppend(nodeData, sections, result, conn, callback);
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
				sections.thumb = {};
                var fileMeta = {'fileId' : thumbId, 'sectionId' : 'thumb',
                                'type' : type}
                self._storeFile(files, fileMeta, fileCallback);
            }
        }

        // No Files uploaded all static text or html
        if (numberSubmitted === 0){
            return self._finishNodeAppend(nodeData, sections, null, conn, callback);
        }

        for (var i = 0; i < filesMetaData.length; i ++){
            var fileMeta = filesMetaData[i];
            var sectionId = fileMeta.sectionId;
            fileMeta.type = nodeContent.sections[sectionId].contentType;
            if (!files[fileMeta.fileId]){
				fileCallback({'error' : 'File Was Not Uploaded'}, sectionId, null);
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
                return errorCallback('Node Data is not an object', callback);
            }
        }
        
        if (!nodeData || !nodeData.latitude || !params.authUserId ||
            !nodeData.longitude || !nodeData.tourId){
            logger.warn(nodeData, nodeData.latitude, params.authUserId, nodeData.longitude, nodeData.tourId);
            return errorCallback('Missing Required Parameters', callback);
        }
        self._verifyOwnership(
            [params.authUserId, nodeData.tourId],
            queries.checkTourOwnership,
            errorWrap(callback, function(conn){
                if (nodeData.prevNode){
                    var sql = queries.selectNodeById;
                    var sqlParams = [nodeData.prevNode];
                    conn.query(sql, sqlParams).execute(errorWrap(
						callback, function (rows, cols){
							if (rows.length === 0){
								var msg = 'The previous node specified doesnt exist';
								return errorCallback(msg, callback);
							}
							self._createNode(nodeData, files, conn, callback);
						}
					));
                    return null;
                }
                self._createNode(nodeData, files, conn, callback);
			})
        );                    
    };

    self._finishNodeAppend = function (nodeData, sections, filesResult, conn, callback){
        var nodeContent = nodeData.content;
        var latitude = nodeData.latitude;
        var longitude = nodeData.longitude;
        var tourId = nodeData.tourId;
        var pseudo = (!nodeContent && !nodeData.brief) ? 1 : 0;
        var sql = queries.appendNode;
        var sqlParams = [latitude, longitude, pseudo, tourId, tourId, tourId, tourId];
        
        if (nodeData.prevNode !== undefined){
            var prevNode  = nodeData.prevNode;
            if (prevNode === null){
                sql = queries.prependNode;
            } else {
                sql = queries.insertNode;
                sqlParams = [latitude, longitude, prevNode, pseudo, tourId,
                              prevNode, prevNode, prevNode];
            }
        }
            
        if (nodeData.brief && nodeData.brief.thumbId){
            nodeData.brief.thumbId = sections.thumb ? sections.thumb.contentId : null;
            delete sections.thumb;
        }
        
        multiQuery(conn, sql, sqlParams, errorWrap(callback, function (result){
            if (result.length === 0){
                return errorCallback('SQL Query Failed', callback);
            }
            
            var result = result[0];
                        
            var mongoObject = {};
            if (nodeData.brief){
                mongoObject.brief = nodeData.brief;
            }

            if (nodeData.content){
                mongoObject.content = convertToMongo(nodeData.content);
            }
            
            if (_.isEmpty(mongoObject)){
                return callback({'success': 'committed pseudo node',
                          'result' : result});
            }
            store.mongoCollection(
                constants.NODE_COLLECTION,
                errorWrap(callback, function (collection){
                    collection.insert(
                        mongoObject, {safe : true },
                        errorWrap(callback, function(records){
                            if (records.length === 0){
                                var msg = 'Couldnt save to mongo, MySQL modified!';
                                return errorCallback(msg, callback);
                            }
                            var mongoId = records[0]._id.toHexString();
                            var nodeId = result.nodeId;
                            conn.query(
								queries.bindMongoToSql,
								[mongoId, nodeId]).execute(errorWrap(
									callback, function (rows, cols){
										result.mongoId = mongoId;
										var retObj = {'success' : 'Node Created',
													  'result': result};
										if (filesResult) retObj.filesResult = filesResult;
										callback(retObj);
									}
								)
							);
						})
					);
                })
			);
        }));
    };

    self.getNode = function (params, callback){
        if (!params.nodeId && !params.mongoId){
            return errorCallback('Missing Required Parameters', callback);
        }
        var getMongoNode = function (mongoId){
            store.mongoCollection(
                constants.NODE_COLLECTION,
                errorWrap(callback, function (collection){
                    var _id = self.store.getMongoIdFromHex(mongoId);
					if (!_id){
						return errorCallback('Invalid Node Id', callback);
					}
                    var query = { _id : _id };
					collection.findOne(query, errorWrap(callback, function (record){
						record._id = record._id.toHexString();
						callback(record);
					}));
                })
            );
        };
        
        if (params.nodeId){
            self.store.sqlConn(errorWrap(callback, function (conn){
                var sql = queries.selectNodeById;
                var sqlParams = [params.nodeId];
                conn.query(sql, sqlParams).execute(errorWrap(
					callback, function (rows, cols){
						if (rows.length === 0){
							return errorCallback('Node ID is invalid', callback);
						}
						getMongoNode(rows[0].mongoId);
					}
				));
            }));
        } else {
            getMongoNode(params.mongoId);
        }
    };
    
    self._modifyNode = function (params, conn, callback){
        
    };
    
    self.modifyNode = function (params, callback){
        if (!params.tourId || !params.authUserId || !params.nodeId){
            return errorCallback('Missing Required Parameters', callback);
        }

        self._verifyOwnership(
            [params.authUserId, params.tourId, params.nodeId],
            queries.checkNodeOwnership,
            errorWrap(callback, function(conn){
			    self._modifyTour(params, conn, callback);
            })
        );
    };

	self._storeFile = function (files, data, callback){
        var fileId = data.fileId;
        var sectionId = data.sectionId;
        var type = data.type;
        
        self.store.newMongoGrid(type, errorWrap(callback, function(gs, name){
			//var outStream = fs.createWriteStream('/tmp/'+name);
			//logger.error(name);
			var fileStream = fs.createReadStream(files[fileId].path, {
                'flags' : 'r',
                'bufferSize' : constants.FILE_BUF_SIZE
            });
            
            fileStream.on('data', function (chunk){
                fileStream.pause();
				gs.write(chunk, function(err, result){
                    if (err){
                        fs.unlinkSync(files[fileId].path);
                        gs.close();
						logger.error('FAILURE');
                        return callback({'error' : fileId+' didnt write to GridFS'});
                    }
					//outStream.write(chunk);
                    fileStream.resume();
                });
            });
            
            fileStream.on('close', function(){
                fs.unlinkSync(files[fileId].path);
                gs.close(function(){
					//outStream.end();
                    callback(null, sectionId, name);
                });
            });
        }));
    };

    self._verifyOwnership = function (args, sql, callback){
        self.store.sqlConn(function(err, conn){
            conn.query(sql, args).execute(errorWrap(callback, function (rows, cols){
                if (rows.length === 0){
                    return errorCallback('User Doesnt own this tour', callback);
                }
                callback(null, conn);
            }));
        });
    };

    self.init();
    return self;
};

exports.TourManager = TourManager;
    