-- DEVICE_RESULT
drop table if exists DEVICE_RESULT cascade;

create table DEVICE_RESULT (
  NODE_ID INTEGER not null
  , DEVICE_ID INTEGER not null
  , METRIC_ID INTEGER not null
  , DOMAIN_ID INTEGER not null
  , VALUE VARCHAR(100)
) ;

create unique index DEVICE_RESULT_PKI
  on DEVICE_RESULT(NODE_ID,DEVICE_ID,METRIC_ID,DOMAIN_ID);

alter table DEVICE_RESULT
  add constraint DEVICE_RESULT_PKC primary key (NODE_ID,DEVICE_ID,METRIC_ID,DOMAIN_ID);

-- DEVICE
drop table if exists DEVICE cascade;

create table DEVICE (
  DEVICE_ID INTEGER not null
  , METRIC_ID INTEGER not null
  , DOMAIN_ID INTEGER not null
  , DEVICE_NAME VARCHAR(100) not null
) ;

create unique index DEVICE_PKI
  on DEVICE(DEVICE_ID,METRIC_ID,DOMAIN_ID);

alter table DEVICE
  add constraint DEVICE_PKC primary key (DEVICE_ID,METRIC_ID,DOMAIN_ID);

-- TEST_RESULT
drop table if exists TEST_RESULT cascade;

create table TEST_RESULT (
  NODE_ID INTEGER not null
  , METRIC_ID INTEGER not null
  , DOMAIN_ID INTEGER not null
  , VALUE VARCHAR(100)
  , VERIFY_RESULT INTEGER
) ;

create unique index TEST_RESULT_PKI
  on TEST_RESULT(NODE_ID,METRIC_ID,DOMAIN_ID);

alter table TEST_RESULT
  add constraint TEST_RESULT_PKC primary key (NODE_ID,METRIC_ID,DOMAIN_ID);

-- METRIC
drop table if exists METRIC cascade;

create table METRIC (
  METRIC_ID INTEGER not null
  , DOMAIN_ID INTEGER not null
  , METRIC_NAME VARCHAR(100) not null
) ;

create unique index METRIC_PKI
  on METRIC(METRIC_ID,DOMAIN_ID);

alter table METRIC
  add constraint METRIC_PKC primary key (METRIC_ID,DOMAIN_ID);

-- NODE
drop table if exists NODE cascade;

create table NODE (
  NODE_ID INTEGER not null
  , NODE_NAME VARCHAR(100) not null
  , SITE_ID INTEGER not null
  , TENANT_ID INTEGER not null
) ;

create unique index NODE_PKI
  on NODE(NODE_ID);

alter table NODE
  add constraint NODE_PKC primary key (NODE_ID);

-- TENANT
drop table if exists TENANT cascade;

create table TENANT (
  TENANT_ID INTEGER not null
  , TENANT_NAME VARCHAR(100) not null
) ;

create unique index TENANT_PKI
  on TENANT(TENANT_ID);

alter table TENANT
  add constraint TENANT_PKC primary key (TENANT_ID);

-- DOMAIN
drop table if exists DOMAIN cascade;

create table DOMAIN (
  DOMAIN_ID INTEGER not null
  , DOMAIN_NAME VARCHAR(100) not null
) ;

create unique index DOMAIN_PKI
  on DOMAIN(DOMAIN_ID);

alter table DOMAIN
  add constraint DOMAIN_PKC primary key (DOMAIN_ID);

-- SITE
drop table if exists SITE cascade;

create table SITE (
  SITE_ID INTEGER not null
  , SITE_NAME VARCHAR(100) not null UNIQUE
) ;

create unique index SITE_PKI
  on SITE(SITE_ID);

alter table SITE
  add constraint SITE_PKC primary key (SITE_ID);
