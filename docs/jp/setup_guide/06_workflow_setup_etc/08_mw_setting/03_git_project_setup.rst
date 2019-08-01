Git プロジェクト登録
====================

Git プロジェクト初期化
----------------------

Git 管理コンソール画面に戻ります。

::

   http://{構成管理DBのIP}/

画面右上の「+」をクリックしてメニュー「New Repository」を選択します。

* 「Owner」 に "getconfig" を選択します。
* 「Repository name」の欄に、 "mw_setup" を入力します。
* 「Description」の欄に、 "MWセットアップ時の変更管理" を入力します。
* 「Private」を選択します。

「Create repository」をクリックして登録します。
登録後、表示された画面の「Push an existing repository from the command line」の
したの以下の行をコピーします。

::

   git remote add origin http://{構成管理DBのIP}/git/getconfig/mw_setup.git
   git push -u origin master
   

Git プロジェクト登録
--------------------

前節で作成したワークフローを、Git リポジトリに登録します。

ワークフローサーバにリモートデスクトップ接続して、PowerShellコンソールを開きます。

 :doc:`/04_data_cleansing_job_verification/03_ip_address_stocktaking` で作成したディレクトリに移動します。

::

   cd C:\Users\Administrator\Desktop\cleansing_sample\mw_setup

job ディレクトリから Jenkins ワークフロー定義の Jenkinsfile をコピーします。 

::

   copy .\job\mw_setup .\Jenkinsfile

Git リポジトリの登録をします。

::

   git init .
   git add .
   git commit -a -m "First init"
   git remote add origin http://{構成管理DBのIP}/git/getconfig/mw_setup.git
   git push -u origin master

