var logger = require('./customLogger.js').getLogger();
var constants = require('./constants.js');

var getLineNum = require('../lib/customLogger.js').getLineNumber;
var errorHelper = require('../lib/helpers.js').errorHelper;
var errorWrap = function (retCallback, callback){
    return errorHelper(logger, getLineNum(), callback, retCallback);
};


exports.keys = {
    locationKeys : ['locId', 'country', 'region', 'city', 'postalCode', 'latitude',
                    'longitude', 'metroCode', 'areaCode'],
    ipBlockKeys : ['locId', 'startipnum', 'endipnum'],
    userKeys : ['userId', 'userName', 'password', 'salt', 'about', 'email', 'fbId', 'twitterId'],
    tourKeys : ['tourId', 'userId', 'tourName', 'description', 'locId',
                'walkingDistance', 'official', 'active'],
    tourHistoryKeys : ['userId', 'tourId', 'timeStarted', 'finished', 'timeFinished', 'rating'],
    tagKeys : ['tagId', 'tagName', 'description', 'userId'],
    tourTagKeys : ['tagId', 'tourId', 'userId'],
    nodeKeys : ['nodeId', 'latitude', 'longitude', 'prevNode', 'nextNode', 'pseudo', 'tourId', 'mongoId']
};

exports.queries= {

    /* USER SQL */
    getUserById : 'select * from users as U where userId = ?;',

    getUserByName : 'select * from users as U where userName = ?;',

    addUser : 'insert into users (userName, password, salt, about, email, fbId, twitterId) VALUES (?, ?, ?, ?, ?, ?, ?);',
    
    updateUserAbout : 'update users set about = ? where userId = ?;', 
    
    /* Tour Queries */
    addTour : 'insert into tours (userId, tourName, description, active) values (?, ?, ?, ?);',

    updateTourDesc : 'update tours set description = ? where tourId = ? and userId = ?;', 
    updateTourLocation : 'update tours set locId = ? where tourId = ? and userId = ?;',
    updateTourDist : 'update tours set walkingDistance = ? where tourId = ? and userId = ?;',
    updateTourLocByCoords: 'update tours set locId = '+
        '(select locId from locations '+
        'where latitude is not null and longitude is not null '+
        'order by ((abs(? - latitude) + abs(? - longitude))/2) asc limit 1 '+
        ') where tourId = ? and userId = ?',
    activeTour : 'update tours set active = ? where tourId = ? and userId = ?;',
    makeTourOfficial : 'update tours set official = 1 where tourId = ? and userId = ?;',
    
    getTourById : 'select * from tours as T inner join nodes as N where N.tourId = T.tourId and T.tourId = ? '+
        'union '+
        'select * from tours T left join (select * from nodes N1 ) as X on T.tourId = X.tourId where T.tourId = ?;',

    getTourByName: 'select * from tours as T inner join nodes as N where N.tourId = T.tourId and T.tourName = ? '+
        'union '+
        'select * from tours T left join (select * from nodes N1 ) as X on T.tourId = X.tourId where T.tourName = ?;',
    
              
    /* Node Queries */
    checkTourOwnership : 'select T.* from tours as T where userId = ? and tourId = ?;',
    checkNodeOwnership : 'select T.* from tours as T, nodes as N where T.tourId = N.tourId and T.userId = ? and T.tourId = ?;',

    //Use MQ: Expects lat, long, pseudo, tourId, tourId, tourId, tourId
    prependNode: 'insert into nodes (latitude, longitude, prevNode, nextNode, pseudo, tourId) '+
        'select ?, ?, null, X.nodeId, ?, ? from ('+
        'select nodeId from nodes as N1 where N1.prevNode is null and N1.tourId = ? '+
        'union select null from dual '+
        'where not exists (select * from nodes N2 where N2.prevNode is null and N2.tourId = ? )) as X;'+
        'update nodes set prevNode = LAST_INSERT_ID() '+
        'where nodeId != LAST_INSERT_ID() and prevNode is null and tourId = ?;'+
        'select * from nodes where nodeId=LAST_INSERT_ID();',

    //Use MQ: Expects lat, long, pseudo, tourId, tourId, tourId, tourId
    appendNode: 'insert into nodes (latitude, longitude, prevNode, nextNode, pseudo, tourId) '+
        'select ?, ?, X.nodeId, null, ?, ? from ('+
        'select nodeId from nodes as N1 where N1.nextNode is null and N1.tourId = ? '+
        'union select null from dual '+
        'where not exists (select * from nodes as N2 where N2.nextNode is null and N2.tourId = ?)) as X; '+
        'update nodes set nextNode=LAST_INSERT_ID() '+
        'where nodeId != LAST_INSERT_ID() and nextNode is null and tourId = ?;'+
        'select * from nodes where nodeId=LAST_INSERT_ID();',

    //Use MQ: Needs latitude, longitude, prevNode, pseudo, tourId, prevNode, prevNode, prevNode
    insertNode: 'insert into nodes (latitude, longitude, prevNode, nextNode, pseudo, tourId) '+
        'select ?, ?, ?, X.nextNode, ?, ? from (select nextNode from nodes as N1 where N1.nodeId = ?) as X;'+
        'update nodes as N1, nodes as N2 set N2.prevNode = LAST_INSERT_ID() '+
        'where N1.nodeId != N2.nodeId and N2.nodeId = N1.nextNode and N1.nodeId = ?;'+
        'update nodes set nextNode = LAST_INSERT_ID() where nodeId = ?;'+
        'select * from nodes where nodeId=LAST_INSERT_ID();',

    //Use MQ: Needs nodeId, nodeId, nodeId
    removeNode : 'update nodes as N1, (select nodeId, nextNode from nodes where nodeId = ?) as N2 '+
        'set N1.nextNode=N2.nextNode where N1.nextNode = N2.nodeId;'+
        'update nodes as N1, (select nodeId, prevNode from nodes where nodeId = ?) as N2 '+
        'set N1.prevNode=N2.prevNode where N1.prevNode = N2.nodeId;'+
        'delete from nodes where nodeId = ?;',
    
    // Needs mongoId, nodeId
    bindMongoToSql: 'update nodes set mongoId = ? where nodeId = ?;',

    getNodeById : 'select * from nodes where nodeId = ?;',

    /* Tag Related Queries */

    addTag : 'insert into tags (tagName, description, userId) values (?, ?, ?);',
    tagTour: 'insert into tourTags (tagId, tourId, userId) values (?, ?, ?);',

    getTagById : 'select * from tags where tagId = ?;',
    getTagByName : "select * from tags where tagName like ?",
    getTagByDescription : "select * from tags where  description like ?;",
    getTagByData : "select * from tags where tagName like ? or description like ?;",
    
    getTourTags: 'select * from tags T1, tourTags T2 where T1.tagId = T2.tagId and tourId = ?',

    
    deleteTourTag: 'delete from tourTags where tagId = ?',

    
    /* Search Queries */
    getToursByTagName: 'select T.*, latitude, longitude from tours T left join locations L on T.locId = L.locId '+
        'where tourId in (select T0.tourId from tours T0, tags T1, tourTags T2 where '+
        'T1.tagId = T2.tagId and T0.tourId = T2.tourId and T1.tagName like ? )',

    getToursByUserId: 'select T.*, latitude, longitude from tours T left join locations L on T.locId = L.locId '+
        'where userId = ?',

    getToursByUserName: 'select T.*, latitude, longitude from tours T left join locations L on T.locId = L.locId '+
        'inner join users U on U.userId = T.userId and  userName = ?',
            
    getToursByName: 'select T.*, latitude, longitude from tours T left join locations L on T.locId = L.locId '+
        'where (T.locId = L.locId or T.locId is null) and  T.tourName like ?',

    getToursByAny: 'select T.*, latitude, longitude from tours T left join locations L on T.locId = L.locId '+
        'where tourId in (select T0.tourId from tours T0, tags T1, tourTags T2 where '+
        'T1.tagId = T2.tagId and T0.tourId = T2.tourId and T1.tagName like ? ) '+
        'or T.tourName like ? or T.description like ?',

    getAllTours: 'select T.*, latitude, longitude from tours T left join locations L on T.locId = L.locId',
    
    /* Geo Queries */
    getLocIdByIP : 'select * from ipBlocks where endIpNum >= ? order by endIpNum asc limit 1;',
    getLocByLocId: 'select * from locations where locId = ?;',
    getLocByKeys: "select * from locations where city like ? and region = ?;",
    getLocByLatLong: "select * from locations where latitude is not null and longitude is not null order by ((abs(? - latitude) + abs(? - longitude))/2) asc  limit 1;"
    
};

var joinQueries = {
    'tour' : {
        'create' : 'create temporary table tourJoin (tourId integer unsigned primary key)',
        'insert' : 'insert into tourJoin values ',
        'select' : 'select T.*, latitude, longitude from tours T left join locations L on T.locId = L.locId '+
            'where tourId in (select tourId from tourJoin)'
    },
    'node' : {
        'create' : 'create temporary table nodeJoin (nodeId integer unsigned primary key)',
        'insert' : 'insert into nodeJoin values ',
        'select' : 'select * from nodes where nodeId in (select nodeId from nodeJoin)'
    }
};
    

var selectMany = function(conn, table, values, callback){
    if (values.length === 0){
        return callback(null, []);
    }
    var queries = joinQueries[table];
    conn.query(queries.create).execute(errorWrap(callback, function(res){
        var insert = queries.insert;
        var tQuery = conn.query(insert);
        for (var i = 0; i < values.length; i ++){
            var add = '('+conn.escape(values[i].toString())+')';
            if (i > 0) add = ', '+add;
            tQuery.add(add);
        }
        tQuery.execute(errorWrap(callback, function(rows){
            var select = queries.select;
            var tQuery1 = conn.query(select);
            tQuery1.limit(constants.MAX_RESULT);
            tQuery1.execute(errorWrap(callback, function(rows){
                return callback(null, rows);
            }));
        }));
    }));    
};

exports.selectMany = selectMany;

var recursiveQuery = function (conn, queries, argsArray, callback, result){
    if (queries.length === 0){
        return callback(null, result);
    }
    var sql = queries.shift();
    var sqlParams = argsArray.shift();
    
    if (!sql){
        return recursiveQuery(conn, queries, argsArray, callback, result);
    }
    
    conn.query(sql, sqlParams).execute(function (err, res){
        if (err){
            logger.error('Sql Failed');
            logger.error(sql);
            logger.error(sqlParams);
            return callback({'error' : 'One of the queries failed',
                             'messsage' : err });
        } else {
            recursiveQuery(conn, queries, argsArray, callback, res);
        }
    });
};

exports.multiQuery = function (conn, query, args, callback){
    var queries = query.split(';');
    var argsArray = [];
    for (var i = 0; i < queries.length; i ++){
        var placeHolders = queries[i].match(/\?/g) || [];
        argsArray.push(args.splice(0, placeHolders.length));
    }
    recursiveQuery(conn, queries, argsArray, callback);
};