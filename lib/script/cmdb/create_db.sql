-- Project Name : Getconfig
-- Author       : minoru.furusawa@toshiba.co.jp
-- RDBMS Type   : MySQL, H2

create table VERSION (
  BUILD INTEGER not null
  , constraint BUILD_PKC primary key (BUILD)
) ;

INSERT INTO VERSION(BUILD) VALUES (1);

create table DEVICE_RESULT (
  NODE_ID INTEGER not null
  , METRIC_ID INTEGER not null
  , SEQ INTEGER not null
  , ITEM_NAME VARCHAR(128) not null
  , VALUE VARCHAR(4000)
  , CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  , constraint DEVICE_RESULT_PKC primary key (NODE_ID, METRIC_ID, SEQ, ITEM_NAME)
);

create table TEST_RESULT (
  NODE_ID INTEGER not null
  , METRIC_ID INTEGER not null
  , VALUE VARCHAR(4000)
  , VERIFY INTEGER
  , CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  , constraint TEST_RESULT_PKC primary key (NODE_ID, METRIC_ID)
);

create table METRIC (
  ID INTEGER not null auto_increment
  , DOMAIN_ID INTEGER not null
  , METRIC_NAME VARCHAR(128) not null
  , CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  , constraint METRIC_PKC primary key (ID)
);

create unique index UK_METRIC on METRIC(DOMAIN_ID, METRIC_NAME);

create table NODE (
  ID INTEGER not null auto_increment
  , SITE_ID INTEGER not null
  , TENANT_ID INTEGER not null
  , NODE_NAME VARCHAR(128) not null
  , CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  , constraint NODE_PKC primary key (ID)
);

create unique index UK_NODE on NODE(SITE_ID, NODE_NAME);

create table TENANT (
  ID INTEGER not null auto_increment
  , TENANT_NAME VARCHAR(128) not null
  , CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  , constraint TENANT_PKC primary key (ID)
);

create unique index UK_TENANT on TENANT(TENANT_NAME);

INSERT INTO TENANT(TENANT_NAME) VALUES ('_Default');

create table DOMAIN (
  ID INTEGER not null auto_increment
  , DOMAIN_NAME VARCHAR(128) not null
  , CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  , constraint DOMAIN_PKC primary key (ID)
);

create unique index UK_DOMAIN on DOMAIN(DOMAIN_NAME);

create table SITE (
  ID INTEGER not null auto_increment
  , SITE_NAME VARCHAR(128) not null
  , CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  , constraint SITE_PKC primary key (ID)
);

create unique index UK_SITE on SITE(SITE_NAME);
