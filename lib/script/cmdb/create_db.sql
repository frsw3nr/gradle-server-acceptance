-- project name : getconfig
-- author       : minoru.furusawa@toshiba.co.jp
-- rdbms type   : mysql, h2

create table version (
  build integer not null
  , constraint build_pkc primary key (build)
) ;

insert into version(build) values (1);

create table accounts (
  id integer auto_increment not null
  , account_name varchar(128) not null
  , username varchar(128)
  , password varchar(128)
  , remote_ip varchar(128)
  , created timestamp default current_timestamp not null
  , constraint accounts_pkc primary key (id)
) ;

alter table accounts add unique uk_account (account_name) ;

create table platforms (
  id integer  auto_increment not null
  , platform_name varchar(128) not null
  , build integer
  , created timestamp default current_timestamp not null
  , constraint platforms_pkc primary key (id)
) ;

alter table platforms add unique uk_platform (platform_name) ;

create table platform_config_details (
  id integer  auto_increment not null
  , platform_id integer  not null
  , item_name varchar(128) not null
  , value varchar(4000)
  , created timestamp default current_timestamp not null
  , constraint platform_config_details_pkc primary key (id)
) ;

alter table platform_config_details add unique uk_platform_config_detail (platform_id, item_name) ;

create table tags (
  id integer  auto_increment not null
  , tag_name varchar(128) not null
  , created timestamp default current_timestamp not null
  , constraint tags_pkc primary key (id)
) ;

alter table tags add unique uk_tag (tag_name);

create table tag_nodes (
  id integer auto_increment not null
  , tag_id integer  not null
  , node_id integer  not null
  , created timestamp default current_timestamp not null
  , constraint tag_nodes_pkc primary key (id)
) ;

alter table tag_nodes add unique uk_tag_node (tag_id,node_id);

create table groups (
  id integer  auto_increment not null
  , group_name varchar(128) not null
  , created timestamp default current_timestamp not null
  , constraint groups_pkc primary key (id)
) ;

alter table groups add unique uk_group (group_name);

create table nodes (
  id integer auto_increment not null
  , group_id integer
  , node_name varchar(128) not null
  , ip varchar(128)
  , specific_password varchar(128)
  , compare_node varchar(128)
  , alias_name varchar(128)
  , created timestamp default current_timestamp not null
  , constraint nodes_pkc primary key (id)
) ;

alter table nodes add unique uk_node (node_name);

create table node_configs (
  id integer  auto_increment not null
  , node_id integer  not null
  , platform_id integer  not null
  , account_id integer
  , created timestamp default current_timestamp not null
  , constraint node_configs_PKC primary key (id)
) ;

alter table node_configs add unique uk_node_config (platform_id,node_id);

create table node_config_details (
  id integer  auto_increment not null
  , node_config_id integer not null
  , item_name varchar(128) not null
  , value varchar(4000)
  , created timestamp default current_timestamp not null
  , constraint node_config_details_pkc primary key (id)
) ;

alter table node_config_details add unique uk_node_config_details (node_config_id, item_name);

create table verify_tests (
  id integer  auto_increment
  , verify_test_name varchar(128) not null
  , created timestamp default current_timestamp not null
  , constraint verify_tests_pkc primary key (id)
) ;

alter table verify_tests add unique uk_verify_test (verify_test_name);

create table verify_histories (
  id integer  auto_increment not null
  , verify_test_id integer  not null
  , node_id integer  not null
  , metric_id integer  not null
  , verified boolean
  , created timestamp default current_timestamp not null
  , constraint verify_histories_pkc primary key (id)
) ;

alter table verify_histories add unique uk_verify_history (verify_test_id, node_id, metric_id);

create table verify_configs (
  id integer  auto_increment not null
  , verify_test_id integer not null
  , item_name varchar(128) not null
  , value varchar(4000)
  , created timestamp default current_timestamp not null
  , constraint verify_configs_pkc primary key (id)
) ;

alter table verify_configs add unique uk_test_config (verify_test_id,item_name) ;

create table metrics (
  id integer  auto_increment not null
  , platform_id integer not null
  , metric_name varchar(128) not null
  , level integer default 0
  , device_flag boolean
  , created timestamp default current_timestamp not null
  , constraint metrics_PKC primary key (id)
) ;

alter table metrics add unique uk_metric (platform_id, metric_name);

create table test_results (
  id integer  auto_increment not null
  , node_id integer not null
  , metric_id integer not null
  , verify integer
  , value varchar(4000)
  , created timestamp default current_timestamp not null
  , constraint test_results_pkc primary key (id)
) ;

alter table test_results add unique uk_test_result (node_id, metric_id);

create table device_results (
  id integer  auto_increment not null
  , node_id integer not null
  , metric_id integer not null
  , seq integer not null
  , item_name varchar(128) not null
  , value varchar(4000)
  , created timestamp default current_timestamp not null
  , constraint device_results_PKC primary key (id)
) ;

alter table device_results add unique uk_device_result_hist (node_id,metric_id,seq,item_name) ;

