var logger = require('./customLogger.js').getLogger();

exports.queries= {

    /* Authentication SQL */
    selectUserAuth : 'select * from users as U, userDevices as UD where userName = ? and deviceId = ? and U.userId=UD.userId',

    insertUser : 'insert into users (userName, password, salt, about, email, fbId, twitterId) VALUES (?, ?, ?, ?, ?, ?, ?)',
    
    insertDevice : 'insert into userDevices (userId, deviceId) VALUES (?, ?)',

    updateUserAbout : 'update users set about = ? where userId = ?', 
    
    /* REST SQL */

    /* User Queries*/
    selectUserById : 'select * from users as U, userDevices as UD where U.userId = ? and U.userId=UD.userId',
    selectUserByName : 'select * from users as U, userDevices as UD where userName = ? and U.userId=UD.userId',
    selectUsersByDevice : 'select * from users as U, userDevices as UD where deviceId = ? and U.userId=UD.userId',

    /* Tour Queries */
    insertTour : 'insert into tours (userId, tourName, description, active) values (?, ?, ?, ?)',

    updateTourDesc : 'update tours set description = ? where tourId = ? and userId = ?', 
    updateTourLocation : 'update tours set locId = ? where tourId = ? and userId = ?',
    updateTourDist : 'update tours set walkingDistance = ? where tourId = ? and userId = ?',

    activateTour : 'update tours set active = 1 where tourId = ? and userId = ?',

    /* Node Queries */
    checkTourOwnership : 'select 1 from tours where userId = ? and tourId = ?',
    checkNodeOwnership : 'select 1 from tours, nodes where tours.tourId = nodes.tourId and tours.userId = ? and tours.tourId = ?',

    // Needs lat, long, pseudo, tourId
    appendNode: 'insert into nodes (latitude, longitude, prevNode, nextNode, pseudo, tourId) '+
        'select ?, ?, X.nodeId, null, ?, ? from ('+
        'select nodeId from nodes as N1 where N1.nextNode is null and N1.tourId = 1 '+
        'union '+
        'select null from dual where not exists ('+
        'select * from nodes as N2 where N2.nextNode is null and N2.tourId = 1'+
        ')) as X; '+
        'update nodes set nextNode=LAST_INSERT_ID() where nodeId != LAST_INSERT_ID() and nextNode is null;'+
        'select * from nodes where nodeId=LAST_INSERT_ID();',

    //Needs latitude, longitude, prevNode, nextNode, pseudo, tourId, prevNode, nextNode
    insertNode: 'insert into nodes (latitude, longitude, prevNode, nextNode, pseudo, tourId) values'+
        '(?, ?, ?, ?, ?, ?);'+
        'update nodes set nextNode=LAST_INSERT_ID() where nodeId = ?;'+
        'update nodes set prevNode=LAST_INSERT_ID() where nodeId = ?;',

    // Needs nodeId, nodeId, nodeId
    removeNode : 'update nodes as N1, (select nodeId, nextNode from nodes where nodeId = ?) as N2 '+
        'set N1.nextNode=N2.nextNode where N1.nextNode = N2.nodeId;'+
        'update nodes as N1, (select nodeId, prevNode from nodes where nodeId = ?) as N2 '+
        'set N1.prevNode=N2.prevNode where N1.prevNode = N2.nodeId;'+
        'delete from nodes where nodeId = ?;',

    // mongoId, nodeId
    bindMongoToSql: 'update nodes set mongoId = ? where nodeId = ?',
    
    /* Geo Queries */
    selectLocIdByIP : 'select * from ipBlocks where endIpNum >= ? order by endIpNum asc limit 1',
    selectLocByLocId: 'select * from locations where locId = ?',
    selectLocByKeys: "select * from locations where city like '%?%' and region = ?"
        
};

var recursiveQuery = function (conn, queries, argsArray, callback, result){
    if (queries.length === 0){
        callback(null, result);
        return null;
    }
    var sql = queries.shift();
    var sqlParams = argsArray.shift();

    if (!sql){
        recursiveQuery(conn, queries, argsArray, callback, result);
        return null;
    }
    
    conn.query(sql, sqlParams).execute(function (err, res){
        if (err){
            callback({'error' : 'One of the queries failed'});
            return null;
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
        argsArray.push(args.slice(0, placeHolders.length));
    }
    recursiveQuery(conn, queries, argsArray, callback);
};