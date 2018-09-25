GitBucketインストール
=====================

構成概要
--------

* Git プロジェクト管理用に GitBacket を構築します
* Redmine とサーバを共有する環境を想定しています
* Jenkins ワークフローとの相性の制約のため、接続ポート番号を 80 にします

パッケージインストール
----------------------

EPELリポジトリの追加します。

::

   sudo -E yum install -y epel-release

必須パッケージに加え、普段よく使うパッケージを入れておきます。

::

   sudo -E yum --enablerepo=epel install -y httpd \
   httpd-devel wget git java-1.8.0-openjdk-devel java-1.8.0-openjdk

.. note:: Gitbucket はJava1.8が必要になります。

GitBucketインストール
---------------------

GitBucket開発サイトからリリース情報を確認します。

::

   https://github.com/gitbucket/gitbucket
   https://github.com/gitbucket/gitbucket/releases

releaseから最新版のgitbucket.warをダウンロードします。

root ユーザにスイッチ後、ダウンロードします。

::

   sudo -E su -
   mkdir -p /usr/share/gitbucket/lib
   cd /usr/share/gitbucket/lib
   wget https://github.com/gitbucket/gitbucket/releases/download/4.28.0/gitbucket.war

起動スクリプトをダウンロードします。

::
   
   mkdir -p /var/lib/gitbucket
   wget https://raw.githubusercontent.com/gitbucket/gitbucket/master/contrib/gitbucket.init

ダウンロードした起動スクリプトを編集します。

::

   vi gitbucket.init

GITBUCKET_WAR_FILEの行の下に、「GITBUCKET_PORT=80」を追加します。

::

   GITBUCKET_WAR_FILE=/usr/share/gitbucket/lib/gitbucket.war
   GITBUCKET_PORT=80

編集した、GitBacket 起動スクリプトを/etc/init.d にコピーして、実行権限を付与します。

::

   cp -p gitbucket.init /etc/init.d/gitbucket
   chmod a+x /etc/init.d/gitbucket

自動起動設定をします。

::

   chkconfig gitbucket on

GitBucket を起動します。

::

   /etc/init.d/gitbucket start

管理者パスワードの変更
----------------------

rootのパスワードを変更し、新規ユーザーを作成します。

GitBucketにアクセスしてみます。

::

   http://[hostname]/gitbucket
   # ユーザー名/パスワードは、root/root です


画面右上のプロファイルアイコンを選択し、「Account Setting」を選択します。
「Password」 に新規パスワードを入力して、「Save」をクリックします。

.. メニュー「System Administration」を選択します。

.. 画面右上の「Create User」を選択してユーザを作成します。

.. * Username
.. * Password
.. * Full Name
.. * Mail Address

.. Gitクライアントから、Gitbucketをアクセスする場合の注意点
.. --------------------------------------------------------

.. クライアントがプロキシー設定している場合、
.. GitBucket サーバをプロキシーの除外設定をする必要が有ります。
.. 除外設定をせずにアクセスすると、「エラー 503: Service Unavailable」が
.. 発生します。

.. Linux の場合、以下の環境変数設定をして除外設定をします。

.. ::

..    export no_proxy=localhost,172.*,10.*,gitbucket01

