drop table if exists test_configs;

create table test_configs (
  id integer primary key autoincrement
  , test_id int(11) not null
  , item_name varchar(48) not null
  , value varchar(256)
) ;

create unique index uk_test_config on test_configs(test_id,item_name) ;

drop table if exists device_result_hists;

create table device_result_hists (
  id integer primary key autoincrement
  , test_id int(11) not null
  , node_id int(11) not null
  , metric_id int(11) not null
  , seq int(11) not null
  , item_name varchar(48) not null
  , value varchar(4000)
) ;

create unique index uk_device_result_hist on device_result_hists(test_id,node_id,metric_id,seq,item_name) ;

drop table if exists metrics;

create table metrics (
  id integer primary key autoincrement
  , platform_id int(11) not null
  , metric_name varchar(48) not null
  , level INTEGER  default 0
  , device_flag INTEGER 
) ;

create unique index uk_metric on metrics(platform_id,metric_name) ;

drop table if exists site_nodes;

create table site_nodes (
  id integer primary key autoincrement
  , site_id int(11)  not null
  , node_id int(11) not null
) ;

create unique index uk_site_nodes on site_nodes(site_id,node_id);

drop table if exists sites;

create table sites (
  id integer primary key autoincrement
  , site_name varchar(48)
) ;
create unique index uk_sites on sites(site_name);

drop table if exists result_hists;

create table result_hists (
  id integer primary key autoincrement
  , test_id int(11) not null
  , node_id int(11) not null
  , metric_id int(11) not null
  , verify INTEGER
  , value varchar(4000)
) ;

create unique index uk_result_hist on result_hists(test_id,node_id,metric_id) ;

drop table if exists tests;

create table tests (
  id integer primary key autoincrement
  , test_name varchar(48)
  , created DATETIME
) ;

create unique index uk_test on tests(test_name) ;

drop table if exists node_configs;

create table node_configs (
  id integer primary key autoincrement
  , platform_id int(11) not null
  , node_id int(11) not null
  , item_name varchar(48) not null
  , value varchar(4000)
) ;

create unique index uk_node_config on node_configs(platform_id,node_id,item_name) ;

drop table if exists accounts;

create table accounts (
  id integer primary key autoincrement
  , node_id int(11) not null
  , platform_id int(11) not null
  , account_name varchar(48) not null
  , username varchar(48)
  , password varchar(48)
  , remote_ip varchar(48)
) ;

create unique index uk_account on accounts(node_id,platform_id,account_name) ;

drop table if exists platforms;

create table platforms (
  id integer primary key autoincrement
  , platform_name varchar(48)
  , build int(11)
  , created DATETIME
) ;

create unique index uk_platform on platforms(platform_name) ;

drop table if exists nodes;

create table nodes (
  id integer primary key autoincrement
  , node_name varchar(48)
  , ip varchar(48)
) ;

create unique index uk_node on nodes(node_name) ;
