use docent_db;
drop table if exists nodeData;
drop table if exists nodes;
drop table if exists tourTags;
drop table if exists tags;
drop table if exists tourHistory;
drop table if exists tours;
drop table if exists userDevices;
drop table if exists users;
/*drop table if exists ipBlocks;
drop table if exists locations;
    
create table locations (
  locId integer unsigned not null,
  country varchar(2)  character set utf8 collate utf8_general_ci,
  region varchar(2)  character set utf8 collate utf8_general_ci,
  city varchar(255)  character set utf8 collate utf8_general_ci,
  postalCode varchar(8),
  latitude double,
  longitude double,
  metroCode varchar(8),
  areaCode varchar(8),
  primary key (locId),
  unique(country, region, city, postalCode)
) ENGINE InnoDB character set utf8 collate utf8_general_ci;

create table ipBlocks (
    locId integer unsigned,
    startipnum integer unsigned,
    endipnum integer unsigned,
    index (startipnum),
    index (endipnum),
    foreign key (locId) references locations(locId) on delete cascade
) ENGINE InnoDB character set utf8 collate utf8_general_ci;
*/  
create table users (
    userId integer unsigned not null auto_increment,
	userName varchar(255) not null,
	password varchar(256) not null,
	salt varchar(16) not null,
	about text default null,
	email varchar(256) default null,
	fbId integer default null,
	twitterId integer default null, 
	primary key (userId),
	unique(userName)
) ENGINE InnoDB character set utf8 collate utf8_general_ci;

create table tours (
	tourId integer unsigned not null auto_increment,
    userId integer unsigned not null, 
	tourName varchar(255) not null,
 	description text,
  	locId integer unsigned,
  	walkingDistance double default null,
    official tinyint(1) default 0,
    active tinyint(1) default 0,
	primary key(tourId),
    unique (tourName, official),
    foreign key (userId) references users(userId) on delete cascade,
    foreign key (locId) references locations(locId) on delete cascade
    
) ENGINE InnoDB character set utf8 collate utf8_general_ci;

create table tourHistory (
    userId integer unsigned not null,
    tourId integer unsigned not null,
    timeStarted timestamp not null default CURRENT_TIMESTAMP,
    finished tinyint(1) default 0,
    timeFinished datetime default null,
    rating integer default null,
    foreign key (userId) references users(userId) on delete cascade,
    foreign key (tourId) references tours(tourId) on delete cascade
) ENGINE InnoDB character set utf8 collate utf8_general_ci;

create table tags (
    tagId integer unsigned not null auto_increment,
	tagName varchar(256) not null,
	description text not null,
    userId integer unsigned not null,
  	primary key(tagId),
    foreign key (userId) references users(userId) on delete cascade
) ENGINE InnoDB character set utf8 collate utf8_general_ci;

create table tourTags (
	tagId integer unsigned not null,
	tourId integer unsigned not null,
    userId integer unsigned not null, 
    primary key(tagId, tourId),
  	foreign key(tagId) references tags(tagId) on delete cascade,
  	foreign key(tourId) references tours(tourId) on delete cascade,
    foreign key(userId) references users(userId) on delete cascade
) ENGINE InnoDB character set utf8 collate utf8_general_ci;

create table nodes(
	nodeId integer unsigned not null auto_increment,
    latitude double not null,
    longitude double not null,
    prevNode integer unsigned default null,
	nextNode integer unsigned default null,
    pseudo tinyint(1) default 1,
    tourId integer unsigned not null,
    mongoId varchar(256) default null,
	primary key (nodeId),
    foreign key (nextNode) references nodes(nodeId) on delete cascade,
    foreign key (prevNode) references nodes(nodeId) on delete cascade
) ENGINE InnoDB character set utf8 collate utf8_general_ci;

