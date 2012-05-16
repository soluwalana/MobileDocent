exports.queries= {

    /* Authentication SQL */
    selectUserAuth : 'select * from users as U, user_devices as UD where user_name = ? and device_id = ? and U.user_id=UD.user_id',

    insertUser : 'insert into users (user_name, password, salt, about, email, fb_id, twitter_id) VALUES (?, ?, ?, ?, ?, ?, ?)',
    
    insertDevice : 'insert into user_devices (user_id, device_id) VALUES (?, ?)',


    /* REST SQL */

    /* User queries*/
    selectUserById : 'select * from users as U, user_devices as UD where U.user_id = ? and U.user_id=UD.user_id',
    selectUserByName : 'select * from users as U, user_devices as UD where user_name = ? and U.user_id=UD.user_id',
    selectUsersByDevice : 'select * from users as U, user_devices as UD where device_id = ? and U.user_id=UD.user_id'
    
};

