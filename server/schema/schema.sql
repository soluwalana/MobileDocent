use docent_db;
drop table if exists users;
create table users (
    userId integer unsigned not null auto_increment,
	userName varchar(256) not null,
	password varchar(256) not null,
	salt varchar(16) not null,
	about text default null,
	email varchar(256) default null,
	fbId integer default null,
	twitterId integer default null, 
	primary key (userId),
	unique(userName)
) ENGINE MyISAM character set utf8 collate utf8_general_ci;

drop table if exists userDevices;
create table userDevices(
    userId integer unsigned not null,
    deviceId varchar(256) not null,
    primary key (userId, deviceId),
    foreign key (userId) references users(userId)
) ENGINE MyISAM character set utf8 collate utf8_general_ci;
    
drop table if exists tours;
create table tours (
	tourId integer not null auto_increment,
    userId integer not null, 
	tourName varchar(256) not null,
 	description text,
	locId integer,
  	walkingDistance double default null,
    active tinyint(1) default 0,
	primary key(tourId),
    foreign key (userId) references users(userId),
	foreign key (locId) references locations(locId)
) ENGINE MyISAM character set utf8 collate utf8_general_ci;

drop table if exists tourHistory;
create table tourHistory (
    userId integer not null,
    tourId integer not null,
    timeStarted timestamp not null default CURRENT_TIMESTAMP,
    finished tinyint(1) default 0,
    timeFinished datetime default null,
    rating integer default null,
    foreign key (userId) references users(userId),
    foreign key (tourId) references tours(tourId)
) ENGINE MyISAM character set utf8 collate utf8_general_ci;
    
drop table if exists tags;
create table tags (
    tagId integer not null auto_increment,
	tagName varchar(256) not null,
	description text not null,
	primary key(tagId)
) ENGINE MyISAM character set utf8 collate utf8_general_ci;

drop table if exists tourTags;
create table tourTags (
	tagId integer not null,
	tourId integer not null,
	foreign key(tagId) references tags(tagId),
	foreign key(tourId) references tags(tourId)
) ENGINE MyISAM character set utf8 collate utf8_general_ci;

drop table if exists ipBlocks;
create table ipBlocks (
    startipnum bigint unsigned,
    index (startipnum),
    endipnum bigint unsigned,
    index (endipnum),
    locId bigint unsigned,
    foreign key (locId) references locations(locId)
) ENGINE MyISAM character set utf8 collate utf8_general_ci;

drop table if exists nodes;
create table nodes(
	nodeId integer not null auto_increment,
    latitude double not null,
    longitude double not null,
    prevNode integer default null,
	nextNode integer default null,
    pseudo tinyint(1) default 1,
    tourId integer not null,
	primary key (nodeId),
    foreign key (nextNode) references nodes(nodeId)
) ENGINE MyISAM character set utf8 collate utf8_general_ci;


drop table if exists nodeData;
create table nodeData(
    nodeId integer not null,
    mongoId integer not null,
    brief tinyint(1) default 0,
    primary key(nodeId, mongoId),
    foreign key (nodeId) references nodes(nodeId)
) ENGINE MyISAM character set utf8 collate utf8_general_ci;