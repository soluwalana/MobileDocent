use docent_db;
	
drop table if exists users;
create table users (
	user_id integer not null auto_increment,
	device_id varchar(256) not null,
	user_name varchar(256) not null,
	password varchar(256) not null,
	salt varchar(256) not null,
	primary key (user_id),
	unique(device_id),
	unique(user_name)
) ENGINE=MyISAM character set utf8 collate utf8_general_ci;

drop table if exists region;
create table region (
	region_id integer not null auto_increment,
	region_name varchar(256) not null,
	center_lat double not null,
	center_long double not null,
	radius double not null, 
	primary key (region_id)
) ENGINE=MyISAM character set utf8 collate utf8_general_ci;

drop table if exists tour;
create table tour (
	tour_id integer not null auto_increment,
	tour_name varchar(256) not null,
	description text,
	region_id integer not null,
	primary key(tour_id),
	foreign key (region_id) references region(region_id)
);

