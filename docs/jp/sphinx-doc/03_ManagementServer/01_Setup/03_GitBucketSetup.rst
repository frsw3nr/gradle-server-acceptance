GitBucketセットアップ
=====================

構成概要
--------

* Tomcat+Apache 構成で GitBacket を構築します
* Redmine とサーバを共有する環境を想定しています

パッケージインストール
----------------------

EPELリポジトリの追加します。

::

   sudo -E yum install -y epel-release


remiリポジトリの追加します。

::

   sudo -E rpm -ivh http://rpms.famillecollet.com/enterprise/remi-release-6.rpm

パッケージを最新の状態にします。

::

   sudo -E yum -y update


必須パッケージに加え、普段よく使うパッケージも入れておきます。

::

   sudo -E yum --enablerepo=epel,remi install -y sudo vim-enhanced syslog httpd httpd-devel php php-devel php-pear php-mysql php-gd php-mbstring php-pecl-imagick mariadb-server wget git java-1.8.0-openjdk-devel java-1.8.0-openjdk tomcat

.. note:: Gitbucket はJava1.8が必要になります。


各種サービスを起動し、さらに再起動時にもサービスが有効になるように設定します。

::

   sudo service httpd start
   sudo service tomcat start

::

   sudo chkconfig httpd on
   sudo chkconfig tomcat on

tomcatフォルダ以下の権限を作成したtomcatユーザに変更します。

::

   useradd -s /sbin/nolog tomcat
   sudo -E chown -R tomcat:tomcat /usr/share/tomcat


GitBucketインストール
---------------------

GitBucket開発サイトからリリース情報を確認します。

::

   https://github.com/gitbucket/gitbucket
   https://github.com/gitbucket/gitbucket/releases

releaseから最新版のgitbucket.warをダウンロードして、実行用のディレクトリにコピーします。

::

   wget https://github.com/gitbucket/gitbucket/releases/download/4.7.1/gitbucket.war
   sudo cp gitbucket.war /var/lib/tomcat/webapps/


Apacheのプロキシ設定
--------------------

AJPというプロトコルによって、ApacheとTomcatの相互通信を行い、外部への配信はApacheが担当するようにします。

TomcatのAJP通信は、ポート8080ではなく8009を用います。

以下のファイルを作成します。

::

   sudo vi /etc/httpd/conf.d/gitbucket.conf

::

   <Location /gitbucket>
       ProxyPass ajp://localhost:8009/gitbucket
   </Location>

ファイルを作成したら、Apacheを再起動しておきます。

::

   sudo service restart httpd

GitBucketにアクセスしてみます。

::

   http://[hostname]/gitbucket
   # ユーザー名/パスワードは、root/root です


ログイン後はrootのパスワードを変更し、新規ユーザーを作成しておきます。

リファレンス
------------

CentOS7での構築手順

https://blacknd.com/linux-server/centos7-gitbucket-jenkins-auto-deploy/

