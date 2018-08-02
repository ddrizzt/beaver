/*
 Table default status values: 1 active, 2 paused,
*/
drop database if exists beaver;
CREATE DATABASE if not exists beaver;

use beaver;
DROP TABLE IF exists templates;
CREATE TABLE templates (
  templateid int(11) not null auto_increment,
  templatename varchar(100) not null,
  availableregion varchar(50) null,
  status int(11) not null,
  createdat timestamp null default now(),
  updatedat timestamp null default now() on update current_timestamp,
  primary key (`templateid`),
  UNIQUE KEY `templatename` (`templatename`)
) ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8;


/*
 By default the API will not sore the user name and password information, the user authentication will be done through LDAP or OSS from Godaddy. API just store the user name here.

DROP TABLE IF exists users;
CREATE TABLE users (
  username varchar(50) not null,
  comments varchar(256) null,
  status int(11) not null,
  createdat timestamp null default now(),
  updatedat timestamp null default now() on update current_timestamp,
  UNIQUE KEY `username` (`username`)
)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;
*/

DROP TABLE IF exists stacks;
CREATE TABLE stacks (
  stackid int(11) not null auto_increment,
  stackname varchar(50) not null,
  stackarn varchar(100) not null,
  username varchar(50) not null,
  templateid int(11) not null,
  availableregion varchar(50) null,
  parameters varchar(1024) null default '',
  comments varchar(256) null,
  status varchar(20) not null comment 'status value where will use AWS cloudformation API status value',
  stackoutputs text null comment 'store cloudformation outputs by json',
  stackresources text null comment 'store cloudformation resources by json',
  stackevents text null comment 'store cloudformation events json data',
  createdat timestamp null default now(),
  updatedat timestamp null default now() on update current_timestamp,
  primary key (`stackid`),
  UNIQUE KEY `stackname` (`stackname`),
  INDEX `name_status` (stackname, templateid, status)
)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;


DROP TABLE IF exists stacklogs;
CREATE TABLE stacklogs (
  stacklogid int(11) not null auto_increment,
  stackid int(11) not null,
  username varchar(50) not null,
  comments varchar(256) null,
  status varchar(20) not null comment 'status value where will use AWS cloudformation API status value',
  createdat timestamp null default now(),
  updatedat timestamp null default now() on update current_timestamp,
  primary key (`stacklogid`),
  INDEX `id_user_status` (stackid, username, status)
)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;

insert into templates (templateid, templatename, availableregion, status, createdat, updatedat) values
 (1, 'dovecot_director_demo', 'us-west-2', 1, now(), now()),
 (2, 'kinesis_data_demo', 'us-west-2', 1, now(), now()),
 (3, 'ec2_simple_demo', 'us-west-2', 1, now(), now());







