機器搬入時の変更管理
====================

プロジェクトの作成
------------------

Getconfig ホームからデータベース登録ワークフローをコピーして、新規プロジェクトを作成します。

ワークフローサーバにリモートデスクトップ接続し、PowerShell コンソールを開きます。

* 前節で作成した {Desktop}\\cleansing_sample ディレクトリに移動します。

   ::

      cd C:\Users\Administrator\Desktop\cleansing_sample

* Geconfig ホームのデータベース登録ワークフローをコピーします。

   - コピー元 ： C:\\server-acceptance\\cleansing
   - コピー先 ： {Desktop}\\cleansing_sample\\server_shipping

   ::

      Copy-Item -Path C:\server-acceptance\cleansing\ -Destination .\server_shipping -Recurse

機器搬入時の変更管理ジョブの動作確認
------------------------------------

* コピーしたディレクトリに移動します。

   ::

      cd server_shipping

* sample1 インベントリデータの登録スクリプトを実行します。

   本スクリプトはインベントリ保存ディレクトリ下の、sample1 インベントリデータ
   と台帳のつき合わせをし、結果を Redmine チケットに登録します

   ::

      python .\getconfig\job\template\scheduler_shipping1.py sample1

   実行後、Redmine サイトにて、チケットが登録されているか確認してください。
