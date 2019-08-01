-- project name : getconfig
-- author       : minoru.furusawa@toshiba.co.jp
-- rdbms type   : mysql, h2

create table version (
  build integer not null
  , constraint build_pkc primary key (build)
) ;

insert into version(build) values (1);

create table device_results (
  node_id integer not null
  , metric_id integer not null
  , seq integer not null
  , item_name varchar(128) not null
  , value varchar(4000)
  , created timestamp not null default current_timestamp
  , constraint device_result_pkc primary key (node_id, metric_id, seq, item_name)
);

create table test_results (
  node_id integer not null
  , metric_id integer not null
  , value varchar(4000)
  , verify integer
  , created timestamp not null default current_timestamp
  , constraint test_result_pkc primary key (node_id, metric_id)
);

create table metrics (
  id integer not null auto_increment
  , platform_id integer not null
  , metric_name varchar(128) not null
  , device_flag integer
  , created timestamp not null default current_timestamp
  , constraint metric_pkc primary key (id)
);

create unique index uk_metric on metrics(platform_id, metric_name);

create table nodes (
  id integer not null auto_increment
  , tenant_id integer not null
  , node_name varchar(128) not null
  , ip varchar(128)
  , created timestamp not null default current_timestamp
  , constraint node_pkc primary key (id)
);

create unique index uk_node on nodes(node_name);

create table tenants (
  id integer not null auto_increment
  , tenant_name varchar(128) not null
  , created timestamp not null default current_timestamp
  , constraint tenant_pkc primary key (id)
);

create unique index uk_tenant on tenants(tenant_name);

insert into tenants(tenant_name) values ('_Default');

create table platforms (
  id integer not null auto_increment
  , platform_name varchar(128) not null
  , created timestamp not null default current_timestamp
  , constraint platform_pkc primary key (id)
);

create unique index uk_platform on platforms(platform_name);

create table node_configs (
  id integer not null auto_increment
  , platform_id integer not null
  , node_id integer not null
  , item_name varchar(128) not null
  , value varchar(4000)
  , created timestamp not null default current_timestamp
  , constraint node_config_pkc primary key (id)
);

create unique index uk_node_config on node_configs(platform_id, node_id, item_name);

create table sites (
  id integer not null auto_increment
  , site_name varchar(128) not null
  , created timestamp not null default current_timestamp
  , constraint site_pkc primary key (id)
);

create unique index uk_site on sites(site_name);

create table site_nodes (
  id integer not null auto_increment
  , site_id integer not null
  , node_id integer not null
  , created timestamp not null default current_timestamp
  , constraint site_node_pkc primary key (id)
);

create unique index uk_site_node on site_nodes(site_id, node_id);

create table networks (
  id integer not null auto_increment
  , network_name varchar(128) not null
  , ip varchar(128)
  , network_address bigint(20) not null default 0
  , subnet_mask bigint(20) not null default 0
  , created timestamp not null default current_timestamp
  , constraint network_pkc primary key (id)
);

create unique index uk_network on networks(network_name);

create table network_nodes (
  id integer not null auto_increment
  , network_id integer not null
  , node_id integer not null
  , ip varchar(128)
  , network_address bigint(20) not null default 0
  , subnet_mask bigint(20) not null default 0
  , created timestamp not null default current_timestamp
  , constraint network_node_pkc primary key (id)
);

create unique index uk_network_node on network_nodes(network_id, node_id);
