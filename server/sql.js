exports.queries= {

    /* Authentication SQL */
    selectUserAuth : 'select * from users as U, userDevices as UD where userName = ? and deviceId = ? and U.userId=UD.userId',

    insertUser : 'insert into users (userName, password, salt, about, email, fbId, twitterId) VALUES (?, ?, ?, ?, ?, ?, ?)',
    
    insertDevice : 'insert into userDevices (userId, deviceId) VALUES (?, ?)',


    /* REST SQL */

    /* User queries*/
    selectUserById : 'select * from users as U, userDevices as UD where U.userId = ? and U.userId=UD.userId',
    selectUserByName : 'select * from users as U, userDevices as UD where userName = ? and U.userId=UD.userId',
    selectUsersByDevice : 'select * from users as U, userDevices as UD where deviceId = ? and U.userId=UD.userId'
    
};

