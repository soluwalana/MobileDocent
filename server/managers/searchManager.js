var _ = require('../lib/underscore.js')._;

var SQL = require('../lib/sql.js').queries;
var selectMany = require('../lib/sql.js').selectMany;
var multiQuery = require('../lib/sql.js').multiQuery;

var constants = require('../lib/constants.js');
var logger = require('../lib/customLogger.js').getLogger();
var getLineNum = require('../lib/customLogger.js').getLineNumber;
var reEscape = require('../lib/helpers.js').reEscape;
var errorHelper = require('../lib/helpers.js').errorHelper;
var geo = require('../lib/geo.js');

var errorWrap = function (retCallback, callback){
    return errorHelper(logger, getLineNum(), callback, retCallback);
};

var errorCallback = function (msg, callback){
    logger.error(msg, getLineNum());
    return callback({ error : msg });
};

var likeWrap = function(string){
    if (typeof(string) === 'string'){
        return '%'+string+'%';
    }
    return string;
};
exports.likeWrap = likeWrap;


var SearchManager = function(store){
    var self = this;

    self.init = function(){
        self.store = store;
    };

    self.addTag = function (params, callback){
        if (!params.authUserId || !params.tagName || !params.description){
            return errorCallback('Missing Required Parameters', callback);
        }
        var sqlParams = [params.tagName, params.description, params.authUserId];
        self._simpleQuery(SQL.addTag, sqlParams, callback, 'Tag Inserted');
    };
        
    self.addTagToTour = function (params, callback){
        if (!params.authUserId || !params.tagId || !params.tourId){
            return errorCallback('Missing Required Parameters', callback);
        }
        var sqlParams = [params.tagId, params.tourId, params.authUserId];
        return self._simpleQuery(SQL.tagTour, sqlParams, callback, 'Tour Tagged');
    };

     
    self.getToursByTag = function (params, sortFn, callback){
        if (!params.tagName){
            return errorCallback('Missing Required Parameters', callback);
        }
        var sql = SQL.getToursByTagName;
        var sqlParams = [likeWrap(params.tagName)];
        return self._simpleQuery(sql, sqlParams, callback, sortFn);
    };

    self.getToursByName = function(params, sortFn, callback){
        if (!params.tourName){
            return errorCallback('Missing Required Parameters', callback);
        }
        var sql = SQL.getToursByName;
        var sqlParams = [likeWrap(params.tourName)];
        return self._simpleQuery(sql, sqlParams, callback, sortFn);
    };

    self.getToursByUser = function(params, sortFn, callback){
        if ((!params.userId && !params.userName) || !params.authUserId){
            return errorCallback('Missing Required Parameters', callback);
        }
        var sql = SQL.getToursByUserId;
        var sqlParams = [params.authUserId];

        if (params.userId !== 'true'){
            sqlParams = [params.userId];
        } else if (params.userName){
            sql = SQL.getToursByUserName;
            sqlParams = [params.userName];
        }
        return self._simpleQuery(sql, sqlParams, callback, sortFn);
    };
    
    
    self.getTourTags = function (params, callback){
        if (!params.tourId){
            return errorCallback('Missing Required Parameters', callback);
        }
        return self._simpleQuery(SQL.getTagsByTourId, [params.tourId], callback);
    };
    
    self.getTags = function (params, callback){
        if (!params.tagId && !params.tagName && !params.description){
            return errorCallback('Missing Required Parameters', callback);
        }
        var sql = SQL.getTagById;
        var sqlParams = [params.tagId];
        if ( params.tagName && params.description ){
            sql = SQL.getTagByData;
            sqlParams = [likeWrap(params.tagName), likeWrap(params.description)];
        } else if (params.tagName){
            sql = SQL.getTagByName;
            sqlParams = [likeWrap(params.tagName)];
        } else if (params.description){
            sql = SQL.getTagByDescription;
            sqlParams = [likeWrap(params.description)];
        }

        return self._simpleQuery(sql, sqlParams, callback);
    };
    
    self.deleteTourTag = function (params, callback){
        if (!params.authUserId || !params.tagId || !params.tourId){
            return errorCallback('Missing Required Parameters', callback);
        }
        var sql = SQL.checkTourOwnership;
        var sqlParams = [params.authUserId, params.tourId];
        self._verifyOwnership(sql, sqlParams, errorWrap(callback, function(conn){
            sql = SQL.deleteTourTag;
            sqlParams = [params.tagId];
            conn.query(sql, sqlParams).execute(errorWrap(callback, function(result){
                return callback({'success' : 'Tag Deleted',
                                 'result' : result});
            }));
        }));
    };


    self.search = function(params, callback){
        var sortFn = null;
        if (_.isNumber(params.latitude) && _.isNumber(params.longitude)){
            sortFn = geo.sortGenerator(params.latitude, params.longitude);
        }
        if (params.tagName) {
            return self.getToursByTag(params, sortFn, callback);
        }
        if (params.tourName) {
            return self.getToursByName(params, sortFn, callback);
        }
        if (params.userName || params.userId){
            return self.getToursByUser(params, sortFn, callback);
        }
        if (!params.q){
            return errorCallback('Missing Required Parameters', callback);
        }
                        
        var queryStr = params.q;
        if (typeof (queryStr) !== 'string'){
            queryStr = queryStr.toString();
        }
        var queryRe = new RegExp('.*?'+reEscape(queryStr), 'i');

        var sql = SQL.getToursByAny;
        var sqlParams = [likeWrap(queryStr), likeWrap(queryStr), likeWrap(queryStr)];

        self.store.sqlConn(errorWrap(callback, function(conn){
            conn.query(sql, sqlParams).execute(errorWrap(callback, function(basicRows){
                
                store.mongoCollection(
                    constants.NODE_COLLECTION,
                    errorWrap(callback, function (collection){
                        var query = {
                            $or : [
                                { 'brief.description' : queryRe },
                                { 'brief.title' : queryRe },
                                { 'content.page.content' : queryRe }
                            ]
                        };
                        var fields = {
                            tourId: true
                            /*'nodeId' : true,
                            'brief.description' : true,
                            'brief.title' : true,
                            'content.page.content' : true*/
                        };

                        var opts = {
                            limits: constants.MAX_RESULT
                        };
                        
                        var cursor = collection.find(query, fields, opts);
                        cursor.toArray(errorWrap(callback, function(records){
                            var values = _.uniq(_.pluck(records, 'tourId'));
                            selectMany(
                                conn, 'tour', values,
                                errorWrap(callback, function(rows){
                                    var allRows = _.uniq(basicRows.concat(rows));
                                    if (sortFn) allRows.sort(sortFn);
                                    callback(allRows.slice(0, constants.MAX_RETURN));
                                })
                            );
                        }));
                    })
                );
            }));
        }));
    };

        
    self._simpleQuery = function (sql, sqlParams, callback, extra){
        self.store.sqlConn(errorWrap(callback, function(conn){
            conn.query(sql, sqlParams).execute(errorWrap(callback, function(rows){
                if (_.isString(extra)){
                    return callback({'success' : extra, 'result' : rows});
                } else if (_.isFunction(extra)){
                    return callback(rows.sort(extra));
                } else {
                    return callback(rows);
                }
            }));
        }));
    };
        
    self._verifyOwnership = function (sql, args, callback){
        self.store.sqlConn(function(err, conn){
            conn.query(sql, args).execute(errorWrap(callback, function (rows, cols){
                if (rows.length === 0){
                    return callback('User Doesnt own this tour');
                }
                callback(null, conn);
            }));
        });
    };

    self.init(store);

    return self;
};

exports.SearchManager = SearchManager;