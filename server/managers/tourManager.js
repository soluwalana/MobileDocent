var fs = require('fs');
var uuid = require('node-uuid');

var likeWrap = require('./searchManager.js').likeWrap;

var _ = require('../lib/underscore.js')._;
var constants = require('../lib/constants.js');
var SQL = require('../lib/sql.js').queries;
var KEYS = require('../lib/sql.js').keys;
var multiQuery = require('../lib/sql.js').multiQuery;
var logger = require('../lib/customLogger.js').getLogger();
var getLineNum = require('../lib/customLogger.js').getLineNumber;
var errorHelper = require('../lib/helpers.js').errorHelper;

var errorWrap = function (retCallback, callback){
    return errorHelper(logger, getLineNum(), callback, retCallback);
};

var errorCallback = function (msg, callback){
    logger.error(msg, getLineNum());
    return callback({ error : msg });
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
};

var formatTourRows = function (rows, single){
    var retObjs = [];
    for (var i = 0; i < rows.length; i ++){
        var row = rows[i];
        var retObj = _.pick(row, KEYS.tourKeys);
        retObj.latitude = row.tourLatitude;
        retObj.latitude = row.tourLongitude;
        retObj.nodes = [];
        
        var nodes = {};
        var curNode = null;
        for (var j = 0; j < rows.length; j ++){
            if (rows[j].nodeId){
                nodes[rows[j].nodeId] = _.pick(rows[j], KEYS.nodeKeys);
                if (rows[j].prevNode === null) curNode = rows[j].nodeId;
            }
        }
        if (curNode){
            retObj.nodes.push(nodes[curNode]);
            while(nodes[curNode].nextNode){
                curNode = nodes[curNode].nextNode;
                retObj.nodes.push(nodes[curNode]);
            }
        }
        retObjs.push(retObj);
    }

    if (single){
        return retObjs[0];
    } 
    return retObjs;
};

var TourManager = function(store){
    var self = this;
    
    self.init = function (){
        self.store = store;
    };

    
    self.createTour = function (params, callback){
        if (!params.tourName || !params.tourDesc || !params.authUserId){
            return errorCallback('Required Fields Missing From Tour Creation', callback);
        }
        
        self.store.sqlConn(errorWrap(callback, function (conn){
            var sql = SQL.addTour;
            var sqlParams = [params.authUserId, params.tourName, params.tourDesc, 0];
            conn.query(sql, sqlParams).execute(errorWrap(callback, function (result){
                callback({'success' : 'Tour Created',
                          'tourId' : result.id,
                          'userId' : params.authUserId});
            }));
        }));
    };

    self.getTour = function (params, callback){
        if (!params.tourId && !params.tourName){
            return errorCallback('Required Fields Missing From Get Tour', callback);
        }
        
        self.store.sqlConn(errorWrap(callback, function (conn){
            var sql = params.tourId ? SQL.getTourById: SQL.getTourByName;
            var sqlParams = params.tourId ? [params.tourId] :
                [params.tourName];

            conn.query(sql, sqlParams).execute(errorWrap(
                callback, function (rows, cols){
                    if (rows.length === 0){
                        return errorCallback('Tour Doesnt Exist', callback);
                    }
                    var retObj = formatTourRows(rows, true);
                    
                    conn.query(SQL.getTourTags, [retObj.tourId])
                        .execute(errorWrap(callback, function(rows){
                            if (rows.length > 0){
                                retObj.tags = rows;
                            }
                            callback(retObj);
                        }));
                }
            ));
        }));
    };


    self._modifyTour = function (p, tour, conn, callback){
        var tourId = p.tourId;
        var userId = p.authUserId;
        
        var sql = '';
        var sqlParams  = [];
        
        if (p.tourDesc !== undefined && 
            !_.isEqual(p.tourDesc, tour.tourDesc)){

            sql += SQL.updateTourDesc;
            sqlParams.push.apply(sqlParams, [p.tourDesc, tourId, userId]);
        }
        
        if (p.tourDist  !== undefined &&
            !_.isEqual(p.tourDist, tour.tourDist)){

            sql += SQL.updateTourDist;
            sqlParams.push.apply(sqlParams, [p.tourDist, tourId, userId]);
        }

        if (p.active !== undefined &&
            !_.isEqual(p.active, tour.active)){

            sql += SQL.activeTour;
            sqlParams.push.apply(sqlParams, [p.active, tourId, userId]);
        }

        if (p.locId !== undefined &&
            !_.isEqual(p.locId, tour.locId)){

            sql += SQL.updateTourLocation;
            sqlParams.push.apply(sqlParams, [p.locId, tourId, userId]);
        }

        if (p.latitude !== undefined && p.longitude !== undefined &&
            !_.isEqual(p.latitude, tour.latitude) &&
            !_.isEqual(p.longitude, tour.longitude)){

            sql += SQL.updateTourLocByCoords;
            sqlParams.push.apply(sqlParams, [p.latitude, p.longitude,
                                             tourId, userId]);
        }
        
        multiQuery(conn, sql, sqlParams, errorWrap(callback, function (result){
            return callback({'success' : 'Updates Successful'});
        }));
        
    };
    
    self.modifyTour = function (params, callback){
        if (!params.tourId || !params.authUserId){
            return errorCallback('Missing Required Parameters', callback);
        }
        self._verifyOwnership(
            SQL.checkTourOwnership,
            [params.authUserId, params.tourId],
            errorWrap(callback, function(conn, rows){
                
                self._modifyTour(params, rows[0], conn, callback);
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
                self._finishNodeAdd(nodeData, sections, result, conn, callback);
            }
        };
        
        nodeData.content = convertContent(nodeData.content);
        var nodeContent = nodeData.content;
        sections = nodeContent ? nodeContent.sections : {};
        
        if (!nodeContent && !nodeData.brief){
            // Pseudo Node
            return self._finishNodeAdd(nodeData, sections, result, conn, callback);
        }
        
        var filesMetaData = [];
        var fileMeta = {};
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
                fileMeta = {'fileId' : thumbId, 'sectionId' : 'thumb',
                            'type' : type};
                self._storeFile(files, fileMeta, fileCallback);
            }
        }

        // No Files uploaded all static text or html
        if (numberSubmitted === 0){
            return self._finishNodeAdd(nodeData, sections, null, conn, callback);
        }

        for (var i = 0; i < filesMetaData.length; i ++){
            fileMeta = filesMetaData[i];
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
            SQL.checkTourOwnership,
            [params.authUserId, nodeData.tourId],
            errorWrap(callback, function(conn, tour){
                nodeData._tour = tour[0];
                if (nodeData.prevNode){
                    var sql = SQL.getNodeById;
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

    self._finishNodeAdd = function (nodeData, sections, filesResult, conn, callback){
        var nodeContent = nodeData.content;
        var latitude = nodeData.latitude;
        var longitude = nodeData.longitude;
        var tourId = nodeData.tourId;
        var tour = nodeData._tour;
        var pseudo = (!nodeContent && !nodeData.brief) ? 1 : 0;

        var sql = SQL.appendNode;
        var sqlParams = [latitude, longitude, pseudo, tourId, tourId, tourId, tourId];

        if (nodeData.prevNode !== undefined){
            var prevNode  = nodeData.prevNode;
            if (prevNode === null){
                sql = SQL.prependNode;
            } else {
                sql = SQL.insertNode;
                sqlParams = [latitude, longitude, prevNode, pseudo, tourId,
                              prevNode, prevNode, prevNode];
            }
        }
            
        if (nodeData.brief && nodeData.brief.thumbId){
            nodeData.brief.thumbId = sections.thumb ? sections.thumb.contentId : null;
            delete sections.thumb;
        }

        if (tour.locId === null){
            /* Set up the tour location on first node append */
            var updateQuery = SQL.updateTourLocByCoords;
            var updateParams = [latitude, longitude, tourId, tour.userId];
            conn.query(updateQuery, updateParams).execute(function(err, rows, cols){
                if (err || rows.length === 0){
                    var msg = 'Automatic Location Update failed tourId: '+tourId;
                    logger.error(msg);
                }
            });
        }
                
        multiQuery(conn, sql, sqlParams, errorWrap(callback, function (result){
            if (result.length === 0){
                return errorCallback('SQL Query Failed', callback);
            }
            result = result[0];
            var nodeId = result.nodeId;
            
            var mongoObject = {};
            mongoObject.tourId = tourId;
            mongoObject.nodeId = nodeId;
            
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
                            conn.query(
                                SQL.bindMongoToSql,
                                [mongoId, nodeId]).execute(errorWrap(
                                    callback, function (rows, cols){
                                        result.mongoId = mongoId;
                                        var retObj = {'success' : 'Node Created',
                                                      'result': result,
                                                      'mongoObj' : mongoObject};
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
                        callback([record]);
                    }));
                })
            );
        };
        
        if (params.nodeId){
            self.store.sqlConn(errorWrap(callback, function (conn){
                var sql = SQL.getNodeById;
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
    
    self.modifyNode = function (params, files, callback){
        //if (!params.nodeData || !params.nodeData.nodeId){
            return errorCallback('Missing Required Parameters', callback);
        //}
        //return self.createNode(params, files, callback);
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


    self.deleteTour = function (params, callback){
        callback({'error' : 'Unimplemented'});
    };

    self.deleteNode = function(params, callback){
        callback({'error' : 'Unimplemented'});
    };

    self._verifyOwnership = function (sql, args, callback){
        self.store.sqlConn(errorWrap(callback, function(conn){
            conn.query(sql, args).execute(errorWrap(callback, function (rows, cols){
                if (rows.length === 0){
                    return callback('User Doesnt own this tour');
                }
                callback(null, conn, rows);
            }));
        }));
    };

    self.init();
    return self;
};

exports.TourManager = TourManager;
    