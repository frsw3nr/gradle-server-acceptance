Gradle server acceptance plugin for Redmine
===========================================

Overview
--------

Add search function of [Gradle server acceptance]((https://github.com/frsw3nr/gradle-server-acceptance)) **evidence test results**
to issue tracking system [Redmine](http://www.redmine.org/).

* Register server construction evidence test results to Redmine database from inspection PC
* Add search page of evidence test results to Redmine
* Link search page of evidence test results to the custom field of Redmine ticket

Requirements
------------

* Redmine 3.0 -
* MySQL 5.5 -
* gradle-server-acceptance v0.1.6 -

Install
-------

Prepare Redmine in advance.
Please use MySQL for the database.
I will describe the procedure based on the environment Redmine under the user home directory on CentOS 6.


**Distribution of plug-ins**

Move to directory 'redmine/plugins' and download plugin project with git clone.

```
cd ~/redmine/plugins
git clone http://github.com/frsw3nr/redmine_getconfig
```

**Installing the Ruby library**

```
cd ~/redmine
bundle install
```

**Database initialization**

Create an evidence collection table in the Redmine database.

```
bundle exec bin/rake redmine:plugins:migrate
```

Change the code of the created table from utf8 to utf8mb4.

```
mysql -u root -p redmine < docs/db_change_utf8_to_utf8mb4.sql
```

Usage
-----


Collect server configuration information using [Gradle server acceptance](https://github.com/frsw3nr/gradle-server-acceptance) on inspection PC.
Register the collected results in the Redmine database.

**Inspection PC procedure**


Set the Redmine database connection information in the MySQL configuration file.

Open 'c:\server-acceptance\config\cmdb.groovy' and edit the following parameters.

```
cmdb.dataSource.username = "redmine_username"
cmdb.dataSource.password = "redmine_password"
cmdb.dataSource.url = "jdbc:mysql://redmine_server:3306/redmine?useUnicode=true&characterEncoding=utf8"
```


After setting, execute inspection with the getconfig command and register the inspection result in the Redmine database.
For example, I will describe Redmine registration method of 'ostrich Linux server' configuration information.
Collect the evidence using the getconfig command.

```
getconfig -s ostrich    # Execute ispection
getconfig -u db         # Regist database
```

**Search results on Redmine search page**


The search page URL is "{Redmine base URL}/inventory?node={server}" .
In the case of searching ostrich in the above example, the URL is as follows.

```
http://{Redmine server}:3000/inventory?node=ostrich
```

**Redmine Customizing custom fields**

By adding a custom field to the ticket,
It is possible to link the inspection result search page from the ticket screen.
Please register the following custom fields with menu management, custom field.


* Formant : Select "Text"
* Name : Input "Inventory"
* Link URL : "/redmine/inventory?node=%value%" or "/inventory?node=%value%"
    * Please set it according to Redmine's base URL

Reference
---------

* [Gradle server acceptance](https://github.com/frsw3nr/gradle-server-acceptance)
* [Plugin Tutorial](http://www.redmine.org/projects/redmine/wiki/Plugin_Tutorial)

AUTHOR
------

Minoru Furusawa <minoru.furusawa@toshiba.co.jp>

COPYRIGHT
-----------

Copyright 2014-2017, Minoru Furusawa, Toshiba corporation.
