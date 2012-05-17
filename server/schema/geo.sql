drop table if exists locations;
create table locations (
  locId integer unsigned not null,
  country varchar(2)  character set utf8 collate utf8_general_ci not null,
  region varchar(2)  character set utf8 collate utf8_general_ci not null,
  city varchar(255)  character set utf8 collate utf8_general_ci not null,
  postalCode varchar(8),
  latitude double,
  longitude double,
  metroCode integer,
  areaCode integer,
  primary key (locId),
  unique(country, region, city, postalCode)
) ENGINE MyISAM character set utf8 collate utf8_general_ci;
