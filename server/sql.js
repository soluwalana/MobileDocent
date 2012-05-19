var logger = require('./customLogger.js').getLogger();

exports.queries= {

    /* USER SQL */
    selectUserById : 'select * from users as U where userId = ?',

    selectUserByName : 'select * from users as U where userName = ?',

    insertUser : 'insert into users (userName, password, salt, about, email, fbId, twitterId) VALUES (?, ?, ?, ?, ?, ?, ?)',
    
    updateUserAbout : 'update users set about = ? where userId = ?', 
    
    /* Tour Queries */
    insertTour : 'insert into tours (userId, tourName, description, active) values (?, ?, ?, ?)',

    updateTourDesc : 'update tours set description = ? where tourId = ? and userId = ?', 
    updateTourLocation : 'update tours set locId = ? where tourId = ? and userId = ?',
    updateTourDist : 'update tours set walkingDistance = ? where tourId = ? and userId = ?',
    activateTour : 'update tours set active = 1 where tourId = ? and userId = ?',
    makeTourOfficial : 'update tours set official = 1 where tourId = ? and userId = ?',

    getTourById : 'select * from tours as T inner join nodes as N where T.tourId = ? and T.tourId = N.tourId',
    getTourByName: 'select * from tours as T inner join nodes as N where tourName = ? and T.tourId = N.tourId',
    
    /* Node Queries */
    checkTourOwnership : 'select 1 from tours where userId = ? and tourId = ?',
    checkNodeOwnership : 'select 1 from tours, nodes where tours.tourId = nodes.tourId and tours.userId = ? and tours.tourId = ?',

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
    bindMongoToSql: 'update nodes set mongoId = ? where nodeId = ?',

    selectNodeById : 'select * from nodes where nodeId = ?',
    /* Geo Queries */
    selectLocIdByIP : 'select * from ipBlocks where endIpNum >= ? order by endIpNum asc limit 1',
    selectLocByLocId: 'select * from locations where locId = ?',
    selectLocByKeys: "select * from locations where city like '%?%' and region = ?"
        
};

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
                             'messsage' : err});
        } else {
            recursiveQuery(conn, queries, argsArray, callback, res);
        }
    });
}

exports.multiQuery = function (conn, query, args, callback){
    var queries = query.split(';');
    var argsArray = [];
    for (var i = 0; i < queries.length; i ++){
        var placeHolders = queries[i].match(/\?/g) || [];
        argsArray.push(args.splice(0, placeHolders.length));
    }
    recursiveQuery(conn, queries, argsArray, callback);
};