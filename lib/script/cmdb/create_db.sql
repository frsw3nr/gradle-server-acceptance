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
  , ITEM_NAME VARCHAR(256) not null
  , VALUE VARCHAR(4000)
  , CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  , constraint DEVICE_RESULT_PKC primary key (NODE_ID, METRIC_ID, SEQ, ITEM_NAME)
);

create table TEST_RESULT (
  NODE_ID INTEGER not null
  , METRIC_ID INTEGER not null
  , VALUE VARCHAR(4000)
  , VERIFY_RESULT INTEGER
  , CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  , constraint TEST_RESULT_PKC primary key (NODE_ID, METRIC_ID)
);

create table METRIC (
  METRIC_ID INTEGER not null auto_increment
  , DOMAIN_NAME VARCHAR(256) not null
  , METRIC_NAME VARCHAR(256) not null
  , CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  , constraint METRIC_PKC primary key (METRIC_ID)
);

create unique index UK_METRIC on METRIC(DOMAIN_NAME, METRIC_NAME);

create table NODE (
  NODE_ID INTEGER not null auto_increment
  , SITE_NAME VARCHAR(256) not null
  , TENANT_NAME VARCHAR(256) not null
  , NODE_NAME VARCHAR(256) not null
  , CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  , constraint NODE_PKC primary key (NODE_ID)
);

create unique index UK_NODE on NODE(SITE_NAME, NODE_NAME);

create table TENANT (
  TENANT_NAME VARCHAR(256) not null
  , CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  , constraint TENANT_PKC primary key (TENANT_NAME)
);

INSERT INTO TENANT(TENANT_NAME) VALUES ('_Default');

create table DOMAIN (
  DOMAIN_NAME VARCHAR(256) not null
  , CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  , constraint DOMAIN_PKC primary key (DOMAIN_NAME)
);

create table SITE (
  SITE_NAME VARCHAR(256) not null
  , CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  , constraint SITE_PKC primary key (SITE_NAME)
);
