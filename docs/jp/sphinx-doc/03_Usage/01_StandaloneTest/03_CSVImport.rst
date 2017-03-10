CSVインポートによる検査対象の読み込み
=====================================

システム概要
------------

Excelのシート「検査対象」の読み込みをせずに、CSVファイルから検査対象をインポートします。

* Redmine を構成管理データベースとして利用し、検査対象のサーバ情報を Redmine チケットに登録した環境を前提としています。
* Redmine チケットの CSV エクスポート機能を活用することを想定しています。

利用方法
--------

検査対象CSVファイルを用意します。

* 例 : config\\issues.csv

   ::

      サーバ名,IPアドレス,Platform,OSアカウントID,vCenterアカウントID,VMエイリアス名,検査ID,比較対象サーバ名,CPU数,メモリ量,ESXi名,HDD
      ostrich,192.168.10.1,Linux,Test,Test,ostrich,RuleDB,,1,2,192.168.10.100,[Thin:30]

CSVフォーマットは以下の形式となります。

   * 区切り文字は","です
   * 文字列の値を括る場合は、"{文字列}"とダブルコーテーションで括ります（省略可）
   * config\\config.groovy 内に記述した列定義の列名を含める必要があります。既定の設定列は以下の通りです
      * サーバ名
      * IPアドレス
      * Platform
      * OSアカウントID
      * vCenterアカウントID
      * VMエイリアス名
      * 検査ID
      * 比較対象サーバ名
      * CPU数
      * メモリ量
      * ESXi名
      * HDD

\-i オプションでCSVファイルを指定して getconfig を実行します。

::

   getconfig -i config\issues.csv

CSVフォーマットのカスタマイズ
-----------------------------

config\\config.groovy 内の以下の列定義をカスタマイズします。

config\\config.groovy

::

   // CSV変換マップ

   evidence.csv_item_map = [
       'サーバ名' :            'server_name',
       'IPアドレス' :          'ip',
       'Platform' :            'platform',
       'OSアカウントID' :      'os_account_id',
       'vCenterアカウントID' : 'remote_account_id',
       'VMエイリアス名' :      'remote_alias',
       '検査ID' :              'verify_id',
       '比較対象サーバ名' :    'compare_server',
       'CPU数' :               'NumCpu',
       'メモリ量' :            'MemoryGB',
       'ESXi名' :              'ESXiHost',
       'HDD' :                 'HDDtype',
   ]

「列名 : 検査シート「検査対象」の項目ID」の形式で、CSVの列名と項目IDの変換定義をします。

