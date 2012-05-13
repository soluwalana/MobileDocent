use docent_db;
drop table if exists users;
create table users (
	user_id integer unsigned not null auto_increment,
	device_id varchar(256) not null,
	user_name varchar(256) not null,
	password varchar(256) not null,
	salt varchar(16) not null,
	about text default null,
	email varchar(256) default null,
	fb_id integer default null,
	twitter_id integer default null, 
	primary key (user_id),
	unique(device_id),
	unique(user_name)
) ENGINE MyISAM character set utf8 collate utf8_general_ci;

drop table if exists locations;
create table locations (
  loc_id integer unsigned not null,
  country varchar(2)  character set utf8 collate utf8_general_ci not null,
  region varchar(2)  character set utf8 collate utf8_general_ci not null,
  city varchar(255)  character set utf8 collate utf8_general_ci not null,
  postalCode varchar(8),
  latitude double,
  longitude double,
  metroCode integer,
  areaCode integer,
  primary key (loc_id),
  unique(country, region, city, postalCode)
) ENGINE MyISAM character set utf8 collate utf8_general_ci;
	
drop table if exists tours;
create table tours (
	tour_id integer not null auto_increment,
    user_id integer not null, 
	tour_name varchar(256) not null,
 	description text,
	loc_id integer,
	walking_distance double default null,
	primary key(tour_id),
    foreign key (user_id) references users(user_id),
	foreign key (loc_id) references locations(loc_id)
) ENGINE MyISAM character set utf8 collate utf8_general_ci;

drop table if exists tags;
create table tags (
    tag_id integer not null auto_increment,
	tag_name varchar(256) not null,
	description text not null,
	primary key(tag_id)
) ENGINE MyISAM character set utf8 collate utf8_general_ci;

drop table if exists tour_tags;
create table tour_tags (
	tag_id integer not null,
	tour_id integer not null,
	foreign key(tag_id) references tags(tag_id),
	foreign key(tour_id) references tags(tour_id)
) ENGINE MyISAM character set utf8 collate utf8_general_ci;

drop table if exists ipBlocks;
create table ipBlocks (
    startipnum bigint unsigned,
    index (startipnum),
    endipnum bigint unsigned,
    index (endipnum),
    loc_id bigint unsigned,
    foreign key (loc_id) references locations(loc_id)
) ENGINE MyISAM character set utf8 collate utf8_general_ci;

drop table if exists nodes;
create table nodes(
	node_id integer not null auto_increment,
	next_node integer default null,
	tour_id integer not null,
	mongo_id integer not null,
    primary key (node_id)
) ENGINE MyISAM character set utf8 collate utf8_general_ci;
	