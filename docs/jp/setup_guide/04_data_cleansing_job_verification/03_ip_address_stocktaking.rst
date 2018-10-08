IPアドレス棚卸し
================

プロジェクトの作成
------------------

ワークフローサーバにリモートデスクトップ接続し、PowerShell コンソールを開きます。

* 前節で作成した {Desktop}\\cleansing_sample ディレクトリに移動し、
   Geconfig ホームのデータベース登録ワークフローをコピーします。

   - コピー元 ： C:\\server-acceptance\\cleansing
   - コピー先 ： {Desktop}\\cleansing_sample\\ip_address_cleansing

   ::

      cd C:\Users\Administrator\Desktop\cleansing_sample
      Copy-Item -Path C:\server-acceptance\cleansing\ -Destination .\ip_address_cleansing -Recurse

IPアドレス棚卸しジョブの動作確認
--------------------------------

* コピーしたディレクトリに移動します。

   ::

      cd ip_address_cleansing

* net1 IPアドレスインベントリデータの登録スクリプトを実行します。

   本スクリプトはインベントリ保存ディレクトリの、net1 ディレクトリ下のインベントリ
   と台帳のつき合わせをし、結果を Redmine チケットに登録します

   ::

      python .\getconfig\job\template\scheduler_network1.py -d TokyoDC net1

   実行後、Redmine サイトにて、チケットが登録されているか確認してください。

