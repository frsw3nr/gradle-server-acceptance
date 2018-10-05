機器搬入時の変更管理
====================

プロジェクトの作成
------------------

Getconfig ホームからデータベース登録ワークフローをコピーして、変更管理用プロジェクトを作成します。

ワークフローサーバにリモートデスクトップ接続して、PowerShellコンソールを開きます。

Desktop ディレクトリに移動します。

::

   cd C:\Users\Administrator\Desktop\

サンプル用のプロジェクトを作成して移動ます。

::

   mkdir cleansing_sample
   cd cleansing_sample

Geconfig ホームのデータベース登録ワークフローディレクトリを server_shipping にコピーします。

::

   Copy-Item -Path C:\server-acceptance\cleansing\ -Destination .\server_shipping -Recurse

同様にサンプルデータディレクトリを cleansing_data にコピーします。

::

   Copy-Item -Path C:\server-acceptance\cleansing\data -Destination .\cleansing_data -Recurse

環境変数のセットアップ
----------------------

Python ライブラリパス PYTHONPATH にカレントディレクトリを設定します。

::

   [System.Environment]::SetEnvironmentVariable("PYTHONPATH", ".", "Machine")

サンプルデータのインベントリ保存用ディレクトリパスを環境変数 GETCONFIG_INVENTORY_DIR に設定します。

::

   $inventory_dir = "C:\Users\Administrator\Desktop\cleansing_sample\cleansing_data\import"
   [System.Environment]::SetEnvironmentVariable("GETCONFIG_INVENTORY_DIR", $inventory_dir, "Machine")

サンプルデータの台帳保存用ディレクトリパスを環境変数 GETCONFIG_MASTER_DIR に設定します。

::

   $master_dir = "C:\Users\Administrator\Desktop\cleansing_sample\cleansing_data\master"
   [System.Environment]::SetEnvironmentVariable("GETCONFIG_MASTER_DIR", $master_dir, "Machine")

Redmine API キーを環境変数 REDMINE_API_KEY に設定します。

::

   $redmine_api_key="{adminユーザのRedmineキー}"
   [System.Environment]::SetEnvironmentVariable("REDMINE_API_KEY", $redmine_api_key, "Machine")

Redmine URL を環境変数 REDMINE_URL に設定します。

::

   $redmine_url="http://{DBサーバIP}:8080/redmine/"
   [System.Environment]::SetEnvironmentVariable("REDMINE_URL", $redmine_url, "Machine")

環境変数を更新するために、PowerShellコンソールを閉じて、再度 PowerShellコンソールを起動します。

ジョブの動作確認
----------------

サンプルデータを用いて各種ジョブの動作確認をします。



$env:REDMINE_URL
