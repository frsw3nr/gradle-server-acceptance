構成情報の収集
==============

コレクターでエビデンス手動作成
------------------------------

プロジェクト作成



プロジェクト移動

::

   cd /d "C:\Program Files (x86)\Jenkins\workspace\test1\"

設定ファイル、ライブラリコピー

c:\server-acceptance\config\config_db.groovy をコピー

::

   cp c:\server-acceptance\config\config_db.groovy config

config_db.groovy 編集

::

   cmdb.dataSource.url = "jdbc:mysql://192.168.10.1:3306/cmdb"
   cmdb.dataSource.driver = "com.mysql.jdbc.Driver"

c:\server-acceptance\lib\script をコピー

::

   cp c:\server-acceptance\lib\script lib\script

手動で検査実行

::

   getconfig -c config\config.groovy
   getconfig -u db

::

   getconfig -c config\config_zabbix.groovy
   getconfig -u db

MySQL workbench をインストール

::

   choco install mysql.workbench

クエリー実行

::

   SELECT
      DOMAIN_NAME, NODE_NAME, METRIC_NAME, VALUE, VERIFY
   FROM
      TEST_RESULT, METRIC, NODE, DOMAIN
   WHERE
      TEST_RESULT.NODE_ID = NODE.ID
   AND
      TEST_RESULT.METRIC_ID = METRIC.ID
   AND
      METRIC.DOMAIN_ID = DOMAIN.ID
