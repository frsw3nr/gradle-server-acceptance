Redmine セットアップ
====================

CentOS にオープンソースの課題管理システム(ITS)の Redmine をインストールし、
Redmine を構成管理データベースとしてカスタマイズします。

構成概要
--------

* Ruby は rbenv で、/opt/ruby の下に配置
* Redmine は 管理者ユーザの $HOME下の ~/redmine に配置
* Passenger で /sbin/httpd と連動。URL は http://サーバ/redmine

システム要件
------------

以下の設定をしたCentOS環境を想定しています

* CentOSは 6.x を使用
* CPU 1 Core以上 / Memory 4 GB以上 / Disk 100 GB以上のHWリソースが必要です
* SELinuxは無効化されていること
* イントラネット環境の場合、プロキシーの設定がされていること

yumパッケージインストール
-------------------------

::

   sudo -E yum -y install gcc gcc-c++
   sudo -E yum -y install httpd httpd-devel
   sudo -E yum -y install mysql-server mysql-devel
   sudo -E yum -y install openssl-devel readline-devel zlib-devel curl-devel

EPELリポジトリ追加

::

   sudo -E yum -y install epel-release
   sudo -E yum -y install libyaml libyaml-devel --enablerepo=epel
   sudo -E yum -y install nodejs npm --enablerepo=epel

Ruby インストール
-----------------

関連する開発用パッケージをインストール

::

   sudo -E yum -y install gcc make openssl-devel libffi-devel readline-devel git
   sudo -E yum -y install ImageMagick-devel

公式gitからclone

::

   cd /opt/
   sudo chmod a+wrx .
   git clone https://github.com/sstephenson/rbenv.git
   mkdir ./rbenv/plugins
   cd ./rbenv/plugins
   git clone https://github.com/sstephenson/ruby-build.git

/etc/profile に追記

::

   sudo vi /etc/profile

最終行に下記３行を追記

::

   export RBENV_ROOT="/opt/rbenv"
   export PATH="${RBENV_ROOT}/bin:${PATH}"
   eval "$(rbenv init -)"

ソースで/etc/profile を反映し rbenv にパスが通った事とバージョン確認

::

   source /etc/profile
   rbenv -v

rbenv を利用して ruby 2.2.3をインストール。インストール可能なrubyのバージョンを確認

::

   rbenv install -l

2.2.3をインストール

::

   rbenv install 2.3.3

2.2.3をシステム標準のバージョンとして設定

::

   rbenv global 2.3.3
   ruby -v

gemパッケージインストール

::

   sudo chown -R psadmin. /opt/rbenv/

.. note:: psadmin の箇所はログインした管理者ユーザを指定してください

gemを最新版に更新

::

   gem update --system --no-rdoc --no-ri
   gem -v

bundlerインストール

::

   gem install bundler --no-rdoc --no-ri

passenger関連gemインストール

::

   gem install daemon_controller rack passenger --no-rdoc --no-ri

MySQL セットアップ
------------------

既に MySQL はインストールされていることを前提にRedmine 用 DB を作成します。
my.cnfにutf8の設定を追加

::

   sudo vi /etc/my.cnf

[mysqld]の箇所

::

   character-set-server=utf8

[mysql]の箇所に追加

::

   character-set-server=utf8

DB、ユーザー作成。
パスワードについは適切な名前に変更してください。

::

   mysql -u root -p
   create database redmine default character set utf8;
   grant all on redmine.* to redmine@localhost identified by '********';
   flush privileges;
   exit

Redmine インストール
--------------------

以下から最新のredmineを取得する

::

   http://www.redmine.org/projects/redmine/wiki/Download

ホームの下に redmine を作成

::

   cd /tmp
   wget http://www.redmine.org/releases/redmine-3.2.5.tar.gz

配置します

::

   cd $HOME
   tar zxvf /tmp/redmine-3.2.5.tar.gz
   ln -s redmine-3.2.5 redmine

Redmine ビルド
--------------

database.ymlを作成

::

   cd ~/redmine/
   cp config/database.yml.example config/database.yml
   vi config/database.yml

productionとdevelpmentセクションの username, password を編集します。

::

   production:
     adapter: mysql2
     database: redmine
     host: localhost
     username: redmine
     password: "********"
     encoding: utf8

   development:
     adapter: mysql2
     database: redmine
     host: localhost
     username: redmine
     password: "********"
     encoding: utf8

bundleインストール（インターネット上の最新リソースを参照）

"vendor/bundle"にgemパッケージ等をインストールする

::

   bundle install --path vendor/bundle

Redmineのビルド

::

   bundle exec rake generate_secret_token
   RAILS_ENV=production bundle exec rake db:migrate

passengerとhttpdの設定
----------------------

httpdモジュールインストールします。

::

   passenger-install-apache2-module

出力メッセージで以下の箇所をコピーします。

::

   LoadModule passenger_module /opt/rbenv/versions/2.3.3/lib/ruby/gems/2.3.0/gems/passenger-5.1.2/buildout/apache2/mod_passenger.so
   <IfModule mod_passenger.c>
     PassengerRoot /opt/rbenv/versions/2.3.3/lib/ruby/gems/2.3.0/gems/passenger-5.1.2
     PassengerDefaultRuby /opt/rbenv/versions/2.3.3/bin/ruby
   </IfModule>

passenger用conf設定

::

   sudo vi /etc/httpd/conf.d/passenger.conf

以下を編集

::

   LoadModule passenger_module /opt/rbenv/versions/2.3.3/lib/ruby/gems/2.3.0/gems/passenger-5.1.2/buildout/apache2/mod_passenger.so
   <IfModule mod_passenger.c>
     PassengerRoot /opt/rbenv/versions/2.3.3/lib/ruby/gems/2.3.0/gems/passenger-5.1.2
     PassengerDefaultRuby /opt/rbenv/versions/2.3.3/bin/ruby
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

シンボリックリンク作成

::

   sudo ln -s ~/redmine/public /var/www/html/redmine

権限設定

::

   sudo chown -R apache:apache /var/www/html/redmine

ホームディレクトリの参照権限、実行権限の追加

::

   sudo chmod a+rx $HOME

httpdサービス自動起動有効化

::

   sudo chkconfig httpd on

httpdサービス起動

::

   sudo service httpd configtest
   sudo service httpd restart

WebブラウザからRedmineに接続して動作確認

::

   http://{サーバ}/redmine/

