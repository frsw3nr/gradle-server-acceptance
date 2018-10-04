Redmineインストール
===================

CentOS6.x に Redmine をインストールし、
Redmine を構成管理データベースとしてカスタマイズします。

構成概要
--------

* Ruby は rbenv で、/opt/ruby の下に配置
* Redmine は 管理者ユーザの $HOME下の ~/redmine に配置
* GitBucket サーバとのバッティングを避けるため、接続ポートは 8080 にします
* Passenger で /sbin/httpd と連動。URL は http://サーバ:8080/redmine

システム要件
------------

以下の設定をしたCentOS環境を想定します。

* CentOSは 6.x を使用
* CPU 1 Core以上 / Memory 4 GB以上 / Disk 100 GB以上のHWリソースが必要です
* SELinuxは無効化されていること
* イントラネット環境の場合、プロキシーの設定がされていること

.. note:: SELinux の無効化

   インストールするソフトウェアの設定は SELinux が機能しない設定となっているため、
   SELinux を無効にしてください。 root で以下を実行してください。

   getenforce コマンドで SELinux の動作状況を調べます。

   ::

       getenforce

   Enforcing と出力された場合は、SELinux が有効となっています。
   以下のコマンドで SELinux を無効化します。

   ::

       setenforce 0 

   /etc/selinux/config を編集し、再起動時の SELinux 状態を無効にします。

   ::

       vi /etc/selinux/config

   SELINUX の値を disabled に変更して保存します。

   ::

       SELINUX=disabled

.. note:: Firewall の許可設定

   Firewall の設定がされている場合は、これらポートのアクセス許可設定をします。
   設定は iptables の設定ファイルを編集して行いますが、ここでは簡略化のため、
   iptables 自体を停止して全ポートのアクセス許可設定をします。

   ::

       /etc/rc.d/init.d/iptables stop 
       chkconfig iptables off 
       chkconfig ip6tables off 

yumパッケージインストール
-------------------------

visudo 権限のある管理者ユーザでサーバにログインし、以下のコマンドでパッケージを
インストールします。

::

   sudo -E yum -y install gcc gcc-c++
   sudo -E yum -y install httpd httpd-devel
   sudo -E yum -y install openssl-devel readline-devel zlib-devel curl-devel

EPEL yum リポジトリをインストールします

::

   sudo -E yum -y install epel-release

MySQL 5.5 インストール用に remi リポジトリをインストールします。

::

   sudo -E rpm -ivh http://rpms.famillecollet.com/enterprise/remi-release-6.rpm

MySQL をインストールします。

::

   sudo -E yum install mysql-server mysql-devel --enablerepo=remi

MySQL を起動し、自動起動設定をします。

::

   sudo /etc/init.d/mysqld start
   sudo chkconfig mysqld on

Node.js をインストールします。

::

   sudo -E yum -y install libyaml libyaml-devel --enablerepo=epel
   sudo -E yum -y install nodejs npm --enablerepo=epel

Ruby インストール
-----------------

関連する開発用パッケージをインストールします。

::

   sudo -E yum -y install gcc make openssl-devel libffi-devel readline-devel git ImageMagick-devel

GitHub サイトから rbenv をダウンロードします。

::

   cd /opt/
   sudo chmod a+wrx .
   git clone https://github.com/sstephenson/rbenv.git
   mkdir ./rbenv/plugins
   cd ./rbenv/plugins
   git clone https://github.com/sstephenson/ruby-build.git

/etc/profile に追記します。

::

   sudo vi /etc/profile

最終行に下記３行を追記します。

::

   export RBENV_ROOT="/opt/rbenv"
   export PATH="${RBENV_ROOT}/bin:${PATH}"
   eval "$(rbenv init -)"

ソースで/etc/profile を反映し rbenv にパスが通った事とバージョン確認します。

::

   source /etc/profile
   rbenv -v

rbenv を利用して ruby 2.5.1 をインストール。インストール可能なrubyのバージョンを確認します。

::

   rbenv install -l

リストの中から最新の安定版を検索してインストールします。

::

   rbenv install 2.5.1

2.5.1 をシステム標準のバージョンとして設定

::

   rbenv global 2.5.1
   ruby -v

/opt/rbenb 下のオーナーを管理ユーザ psadmin に変更します。

::

   sudo chown -R psadmin. /opt/rbenv/

gemを最新版に更新します。

::

   gem update --system --no-rdoc --no-ri

bundlerをインストールします。

::

   gem install bundler --no-rdoc --no-ri

passengerと関連するライブラリインストールします。

::

   gem install daemon_controller rack passenger --no-rdoc --no-ri

MySQL セットアップ
------------------

既に MySQL はインストールされていることを前提に Redmine 用 DB を作成します。
my.cnfにutf8の設定を追加

::

   sudo vi /etc/my.cnf

[mysqld]の下に以下を追加します。

::

   character-set-server=utf8

DB、ユーザー作成します。
パスワードは適宜変更してください。

::

   mysql -u root -p
   create database redmine default character set utf8;
   grant all on redmine.* to redmine@localhost identified by 'change_password';
   flush privileges;
   exit

Redmine インストール
--------------------

以下から最新のredmineを取得します。

::

   http://www.redmine.org/projects/redmine/wiki/Download

ホームの下に redmine を作成

::

   cd /tmp
   wget http://www.redmine.org/releases/redmine-3.4.6.tar.gz

配置します

::

   cd $HOME
   tar zxvf /tmp/redmine-3.4.6.tar.gz
   ln -s redmine-3.4.6 redmine

Redmine ビルド
--------------

database.ymlを作成

::

   cd ~/redmine/
   cp config/database.yml.example config/database.yml
   vi config/database.yml

productionセクションの username, password を編集します。

::

   production:
     adapter: mysql2
     database: redmine
     host: localhost
     username: redmine
     password: "change_password"
     encoding: utf8

Redmine 依存ライブラリをインストールします。
"vendor/bundle"下にgemパッケージをインストールします。

::

   bundle install --path vendor/bundle

MySQL データベースを初期化します。

::

   bundle exec rake generate_secret_token
   RAILS_ENV=production bundle exec rake db:migrate

passengerとhttpdの設定
----------------------

httpdモジュールインストールします。

::

   passenger-install-apache2-module

.. note::

   `Anaconda`_ がインストールされている環境で、~/anaconda2/bin
   のパスが通っていると、anaconda2 の lib を参照してしまい、
   Curlのライブラリが見つからないなどの相性問題が発生する場合があります。
   その場合、 .bashrc から anaconda2 のパスを外してください。

   .. _Anaconda: http://https://www.continuum.io/downloads

出力メッセージで以下の箇所をコピーします。

::

   LoadModule passenger_module /opt/rbenv/versions/2.5.1/lib/ruby/gems/2.5.0/gems/passenger-5.3.4/buildout/apache2/mod_passenger.so
   <IfModule mod_passenger.c>
     PassengerRoot /opt/rbenv/versions/2.5.1/lib/ruby/gems/2.5.0/gems/passenger-5.3.4
     PassengerDefaultRuby /opt/rbenv/versions/2.5.1/bin/ruby
   </IfModule>

passenger用http設定ファイルを編集します。

::

   sudo vi /etc/httpd/conf.d/passenger.conf

出力メッセージでコピーした行を追加して、
その後ろに「 # Passengerが追加するHTTPヘッダを削除するための設定（任意）。」
から始まる以下の行を追加します。

::

   LoadModule passenger_module /opt/rbenv/versions/2.5.1/lib/ruby/gems/2.5.0/gems/passenger-5.3.4/buildout/apache2/mod_passenger.so
   <IfModule mod_passenger.c>
     PassengerRoot /opt/rbenv/versions/2.5.1/lib/ruby/gems/2.5.0/gems/passenger-5.3.4
     PassengerDefaultRuby /opt/rbenv/versions/2.5.1/bin/ruby
   </IfModule>

   # Passengerが追加するHTTPヘッダを削除するための設定（任意）。
   Header always unset "X-Powered-By"
   Header always unset "X-Rack-Cache"
   Header always unset "X-Content-Digest"
   Header always unset "X-Runtime"

   PassengerMaxPoolSize 20
   PassengerMaxInstancesPerApp 4
   PassengerPoolIdleTime 3600
   PassengerHighPerformance on
   PassengerStatThrottleRate 10
   PassengerSpawnMethod smart
   RailsAppSpawnerIdleTime 86400
   PassengerMaxPreloaderIdleTime 0

   # DocumentRootのサブディレクトリで実行する設定
   RackBaseURI /redmine

シンボリックリンクを作成します。

::

   sudo ln -s ~/redmine/public /var/www/html/redmine

権限を設定します。

::

   sudo chown -R apache:apache /var/www/html/redmine

ホームディレクトリの参照権限、実行権限を追加します。

::

   sudo chmod a+rx $HOME

接続ポートを8080に変更します。

::

   sudo vi /etc/httpd/conf/httpd.conf

Listen パラメータの行を編集します。 

::

   Listen 8080

httpdサービス自動起動を有効化します。

::

   sudo chkconfig httpd on

httpdサービスを再起動します。

::

   sudo service httpd configtest
   sudo service httpd restart

WebブラウザからRedmineに接続して接続確認します。

::

   http://{サーバ}:8080/redmine/

admin/admin でログインします。
パスワード変更画面で、新しいパスワードの入力をして保存します。
個人設定画面で、言語を「Japanese」、タイムゾーンを「(GMT+09:00) Tokyo」を選択して、
保存します。

