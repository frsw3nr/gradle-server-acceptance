監視設定時の変更管理
====================

プロジェクトの作成
------------------

ワークフローサーバにリモートデスクトップ接続し、PowerShell コンソールを開きます。

* 前節で作成した {Desktop}\\cleansing_sample ディレクトリに移動し、
   Geconfig ホームのデータベース登録ワークフローをコピーします。

   - コピー元 ： C:\\server-acceptance\\cleansing
   - コピー先 ： {Desktop}\\cleansing_sample\\zabbix_monitor_setup

   ::

      cd C:\Users\Administrator\Desktop\cleansing_sample
      Copy-Item -Path C:\server-acceptance\cleansing\ -Destination .\zabbix_monitor_setup -Recurse

IPアドレス棚卸しジョブの動作確認
--------------------------------

* コピーしたディレクトリに移動します。

   ::

      cd zabbix_monitor_setup

* project2 インベントリデータの登録スクリプトを実行します。

   本スクリプトはインベントリ保存ディレクトリの、project2 ディレクトリ下のインベントリ
   と台帳のつき合わせをし、結果を Redmine チケットに登録します

   ::

      python .\getconfig\job\template\scheduler_zabbix_setup1.py -d TokyoDC project2

   実行後、Redmine サイトにて、チケットが登録されているか確認してください。


