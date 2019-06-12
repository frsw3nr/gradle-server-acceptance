-- project name : getconfig
-- author       : minoru.furusawa@toshiba.co.jp
-- rdbms type   : mysql, h2

create table version (
  build integer not null
  , constraint build_pkc primary key (build)
) ;

insert into version(build) values (1);

create table projects (
  id integer not null auto_increment
  , project_name varchar(128) not null
  , project_path varchar(2048) not null
  , created timestamp not null default current_timestamp
  , constraint project_pkc primary key (id)
);

create unique index uk_project on projects(project_name);

create table nodes (
  id integer not null auto_increment
  , project_id integer not null
  , node_name varchar(128) not null
  , platform varchar(128) not null
  , data_type varchar(128)
  , created timestamp not null default current_timestamp
  , constraint node_pkc primary key (id)
);

create unique index uk_network_node on nodes(project_id, node_name);
