Linuxサーバ収集
---------------

Linux サーバ管理対象の設定
^^^^^^^^^^^^^^^^^^^^^^^^^^

構成収集作業PCにAdministratorユーザでログインしてください。
PowerShell を開いて、プロジェクトディレクトリに移動してください。
プロジェクトディレクトリ下の「サーバチェックシート.xlsx」を開いてください。
シート「チェック対象」を選択し、
入力列に検査対象の Linux サーバの情報を設定してください。
はじめに「platform」に"Linux"を選択してください。
「virtualization」に仮想化OSの場合は、"VM"、オンプレサーバの場合は
"オンプレ"を選択してください。

   .. figure:: image/02_registTargetServer.png
      :align: center
      :alt: New Project
      :width: 640px

「platform」,「virtualization」項目選択後に、「～を入力して下さい」
と表示されたセルの値を入します

* 「server_name」にサーバ名を入力
* 「ip」にIPアドレスを入力
* 「os_account_id」に "Test" を入力
   * config\\config.groovy に記述する、Linux のアカウントIDとなります
* 「os_specific_password」に個別のパスワードを入力
   * OSアカウントで特定のパスワード設定が必要な場合はパスワードを入力します
   * 未記入の場合は config\\config.groovy 内パラメータ値が反映されます
* 「remote_account_id」に "Test" を入力
   * VM構成の場合の仮想化インフラ側のアカウント ID となります
   * vCenter サーバもしくは、 ESXi ホストのアカウントIDを入力
   * config\\config.groovy に記述します
* 「remote_alias」 vCenter サーバ若しくは ESXi ホストで管理しているVMのエイリアス名を入力
   * vSphere Client 管理コンソールからメニュー、ホーム、
     インベントリを選択し、画面左側のツリーリストに表示されるVM名を入力

config\\config.groovyの編集
^^^^^^^^^^^^^^^^^^^^^^^^^^^

サクラエディタなどUTF-8に対応したエディタで設定ファイルの編集をします。

::

   sakura config\config.groovy

検査対象サーバのアカウント情報を編集してください。

::

   // vCenter接続情報

   account.Remote.Test.server   = '192.168.10.100'
   account.Remote.Test.user     = 'test_user'
   account.Remote.Test.password = 'P@ssword'

   // Linux 接続情報

   account.Linux.Test.user      = 'someuser'
   account.Linux.Test.password  = 'P@ssword'
   account.Linux.Test.work_dir  = '/tmp/gradle_test'
   // account.Linux.Test.logon_test = [['user':'test1' , 'password':'test1'],
   //                                  ['user':'root'  , 'password':'P@ssw0rd']]

* アカウントIDについて
   * 各サーバで接続アカウント情報が異なる場合は、アカウントIDを変えて複数アカウント情報を設定してください。
   * シート「検査対象」の"os_account_id"で指定します
   * account.Remote.、account.Linux.の後の文字列がアカウントIDとなります
* Linuxログオンテストについて
   * 最終行の"account.Linux.Test.logon_test"に接続テスト用アカウントを指定します
   * テストを行う場合はコメントアウトを外して設定してください

Linux検査実行
^^^^^^^^^^^^^

PowerShellコンソールから、プロジェクトディレクトリに移動して、getconfig を実行してください。

::

   getconfig

コンソールログは以下となります。

::

   <中略>
   18:49:50 INFO  j.c.t.I.a.EvidenceSheet - Insert device sheet : Linux_network
   18:49:51 INFO  j.c.t.I.a.EvidenceSheet - Insert device sheet : Linux_filesystem
   18:49:51 INFO  j.c.t.I.a.EvidenceSheet - Insert device sheet : Linux_user
   18:49:52 INFO  j.c.t.I.a.EvidenceSheet - Insert device sheet : Linux_service
   18:49:52 INFO  j.c.t.I.a.EvidenceSheet - Insert device sheet : Linux_packages
   18:50:08 INFO  j.c.t.I.a.TestScheduler - Finish server acceptance test, Total Elapsed : 28358 ms

実行後、プロジェクトディレクトリ下の「build」の下に生成された
Excel検査結果を開いて結果を確認します。

   .. figure:: image/04_verifyTestResult.png
      :align: center
      :alt: New Project
      :width: 720px

Excel検査結果の確認ができたら"getconfig -u local"
でローカルデータベースに検査結果を登録します。
本コマンドはプロジェクトりディレクトリ下の「node」ディレクトリに
JSONフォーマット形式で、検査結果を保存します。

::

   getconfig -u local
   12:42:14 INFO  j.c.t.I.a.EvidenceManager - Archive log from './build/log' to './src/test/resources/log/'

次に、"getconfig -u db"を実行し、
構成管理データベースに検査結果を登録します。
本コマンドは構成管理データベースのRedmineデータベースに検査結果を
登録します。

::

   getconfig -u db
   12:42:23 INFO  j.c.t.I.a.CMDBModel - Regist domain Linux
   12:42:23 INFO  j.c.t.I.a.CMDBModel - Regist node gittest
   12:42:23 INFO  j.c.t.I.a.CMDBModel - Regist device gittest filesystem
   12:42:23 INFO  j.c.t.I.a.CMDBModel - Regist device gittest network
   12:42:23 INFO  j.c.t.I.a.CMDBModel - Regist device gittest packages
   12:42:33 INFO  j.c.t.I.a.CMDBModel - Regist device gittest service
   12:42:33 INFO  j.c.t.I.a.CMDBModel - Regist device gittest user
   12:42:33 INFO  j.c.t.I.a.CMDBModel - Export, Elapsed : 10702 ms

.. note::

   Redmineデータベースへの登録データと、Redmineチケットとの関連付けは、
   後述の :doc:`06_UploadInventoryData` で手順を記します。

