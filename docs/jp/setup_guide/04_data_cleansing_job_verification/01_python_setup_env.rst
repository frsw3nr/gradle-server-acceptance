Python環境設定
==============

各ワークフローで使用する共有ディレクトリ、環境変数の設定をします。


モジュール共通化構成

::

   c:\getconfig_cleansing\cleansing
   c:\getconfig_cleansing\jobs
   c:\getconfig_cleansing\jobs\ip_address_cleansing
   c:\getconfig_cleansing\jobs\server_shipping

共通モジュール配布

::

   mkdir c:\getconfig_cleansing
   cd c:\getconfig_cleansing
   git clone http://{構成管理DB}/git/getconfig/cleansing.git

ジョブ配布

::

   mkdir c:\getconfig_cleansing\jobs
   cd c:\getconfig_cleansing\jobs
   git clone http://{構成管理DB}/git/getconfig/server_shipping.git
   git clone http://{構成管理DB}/git/getconfig/ip_address_cleansing.git
   git clone http://{構成管理DB}/git/getconfig/zabbix_monitor_setup.git
   git clone http://{構成管理DB}/git/getconfig/mw_setup.git

データディレクトリ

::

   cd c:\
   git clone http://{構成管理DB}/git/getconfig/cleansing_data.git

環境変数

::

   GETCONFIG_CLEANSING_HOME C:\getconfig_cleansing\cleansing
   [System.Environment]::SetEnvironmentVariable("GETCONFIG_CLEANSING_HOME", "C:\getconfig_cleansing\cleansing", "Machine")
   [System.Environment]::SetEnvironmentVariable("PYTHONPATH", "C:\getconfig_cleansing\cleansing;.", "Machine")
   $inventory_dir = "C:\cleansing_data\import"
   [System.Environment]::SetEnvironmentVariable("GETCONFIG_INVENTORY_DIR", $inventory_dir, "Machine")
   $master_dir = "C:\cleansing_data\master"
   [System.Environment]::SetEnvironmentVariable("GETCONFIG_MASTER_DIR", $master_dir, "Machine")
   $redmine_api_key="{APIキー}"
   [System.Environment]::SetEnvironmentVariable("REDMINE_API_KEY", $redmine_api_key, "Machine")
   $redmine_url="http://{構成管理DB}:8080/redmine/"
   [System.Environment]::SetEnvironmentVariable("REDMINE_URL", $redmine_url, "Machine")

テスト

::

   cd $env:GETCONFIG_CLEANSING_HOME
   python getconfig/job/template/scheduler_network2.py -d {サイト} -s

共通モジュールホームでの実行は可

::

   notepad++ ip_address_cleansing.bat
   cd /d %~dp0
   python.exe %GETCONFIG_CLEANSING_HOME%\getconfig\job\template\scheduler_network2.py %*
   cd

バッチジョブでの実行

::

   cd C:\getconfig_cleansing\jobs\ip_address_cleansing
   .\ip_address_cleansing.bat -d {サイト} -s


データソースディレクトリ作成
----------------------------

Getconfig ホームからサンプルデータをコピーします。

ワークフローサーバにリモートデスクトップ接続し、 PowerShell コンソールを開きます。

* Desktop ディレクトリに移動します。

   ::

      cd C:\Users\Administrator\Desktop\

* サンプルデータ保存用ディレクトリを作成して、移動します

   ::

      mkdir cleansing_sample
      cd cleansing_sample

* サンプルデータディレクトリを cleansing_data にコピーします。

   - コピー元 ： C:\\server-acceptance\\cleansing\\data
   - コピー先 ： {Desktop}\\cleansing_sample\\cleansing_data

   ::

      Copy-Item -Path C:\server-acceptance\cleansing\data -Destination .\cleansing_data -Recurse

環境変数のセットアップ
----------------------

* Python ライブラリパス PYTHONPATH にカレントディレクトリを設定します。

   - パラメータ : PYTHONPATH
   - 値 : .

   ::

      [System.Environment]::SetEnvironmentVariable("PYTHONPATH", ".", "Machine")

* インベントリ保存用ディレクトリパス環境変数 GETCONFIG_INVENTORY_DIR を設定します。

   - パラメータ : GETCONFIG_INVENTORY_DIR
   - 値 : {Desktop}\\cleansing_sample\\cleansing_data\\import

   ::

      $inventory_dir = "C:\Users\Administrator\Desktop\cleansing_sample\cleansing_data\import"
      [System.Environment]::SetEnvironmentVariable("GETCONFIG_INVENTORY_DIR", $inventory_dir, "Machine")

* サンプルデータの台帳保存用ディレクトリパス環境変数 GETCONFIG_MASTER_DIR を設定します。

   - パラメータ : GETCONFIG_MASTER_DIR
   - 値 : {Desktop}\\cleansing_sample\\cleansing_data\\master

   ::

      $master_dir = "C:\Users\Administrator\Desktop\cleansing_sample\cleansing_data\master"
      [System.Environment]::SetEnvironmentVariable("GETCONFIG_MASTER_DIR", $master_dir, "Machine")

* Redmine API キーを環境変数 REDMINE_API_KEY に設定します。

   - パラメータ : REDMINE_API_KEY
   - 値 : :doc:`../03_database_setup/03_redmine_configuration` で確認したAPIキー

   ::

      $redmine_api_key="{adminユーザのRedmineキー}"
      [System.Environment]::SetEnvironmentVariable("REDMINE_API_KEY", $redmine_api_key, "Machine")

* Redmine URL を環境変数 REDMINE_URL に設定します。

   - パラメータ : REDMINE_URL
   - 値 : http://{DBサーバIP}:8080/redmine/

   ::

      $redmine_url="http://{DBサーバIP}:8080/redmine/"
      [System.Environment]::SetEnvironmentVariable("REDMINE_URL", $redmine_url, "Machine")

Python スクリプトの動作確認
---------------------------

サンプルデータを用いて各種スクリプトの動作確認をします。

* 環境変数を更新するために、PowerShellコンソールを閉じて、再度 PowerShell コンソールを起動します。
* データベース登録用スクリプトディレクトリに移動します。

   ::

      cd C:\server-acceptance\cleansing\

* インベントリリスト出力スクリプトの動作確認

   以下のインベントリリスト出力スクリプトを実行します
   
   ::

      python .\getconfig\job\get_inventory_projects.py

   本スクリプトはインベントリデータ保存ディレクトリ下のディレクトリリストを出力します。
   以下の結果が出力されることを確認します。

   ::

      INVENTORY: C:\Users\Administrator\Desktop\server_shipping\data\import
      project : project4
      project : v1.24
      project : net1
      project : old1
      project : project1

* Redmine プロジェクトリスト出力スクリプトの動作確認

   以下のRedmine プロジェクトリスト出力スクリプトを実行します
   
   ::

      python .\getconfig\job\get_redmine_projects.py

   本スクリプトは Redmine プロジェクトのリストを出力します。
   以下の結果が出力されることを確認します。

   ::

      2018/10/05 18:29:43 [INFO] redmine_repository Init RedmineRepository
      project : TokyoDC
      project : cmdb

   .. note::

      上記「TokyoDC」はこの後の機器搬入時の変更管理ジョブで使用しますので、
      事前に Redmine 管理画面からプロジェクトを作成してください。

Getconfig データベース接続設定
------------------------------

インベントリ収取ツールGetconfigは、収集したインベントリデータを Redmine データベース内のプラグイン用テーブルにロードします。
本準備のため、 Redmine データベースへの接続設定をします。

* Getconfig DB設定ファイルのサンプルをコピーして編集します。

   ::

      cd C:\server-acceptance\config
      copy cmdb.groovy.sample cmdb.groovy
      notepad++ cmdb.groovy

   以下の箇所を Redmine MySQL データベースの接続設定をします。

   ::

      cmdb.dataSource.username = "redmine"
      cmdb.dataSource.password = "{MySQL セットアップで指定した redmine ユーザパスワード}"
      cmdb.dataSource.url = "jdbc:mysql://{DBサーバのIP}:3306/redmine?useUnicode=true&characterEncoding=utf8"

動作確認のため、サンプルのインベントリデータを用いてデータベース登録を確認します。

* インベントリ保存ディレクトリに移動します。

   ::

      cd $env:GETCONFIG_INVENTORY_DIR

* sample1 というインベントリ収集プロジェクトを作成します。

   ::

      getconfig -g sample1

* プロジェクトに移動し、DryRun モードでインベントリ収集を実行します。

   ::

      cd sample1
      getconfig -d

* インベントリ収集結果を MySQL データベースに登録します。

   ::

      getconfig -u local
      getconfig -u db

   実行後、以下のメッセージが出力されます。

   ::

      05:19:52 INFO  j.c.t.I.a.CMDBModel - Regist node cent7
      05:19:53 INFO  j.c.t.I.a.CMDBModel - Regist node ostrich
      05:19:58 INFO  j.c.t.I.a.CMDBModel - Regist node win2012
      05:19:58 INFO  j.c.t.I.a.TestRunner - Total, Elapsed : 7812 ms

