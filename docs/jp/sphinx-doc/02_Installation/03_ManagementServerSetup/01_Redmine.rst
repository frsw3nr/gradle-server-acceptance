Redmine インストール
====================

CentOS に課題管理システム(ITS)のOSS Redmine をインストールし、Redmine を構成管理DBとしてカスタマイズします。

構成概要
--------

* Ruby は rbenv で、/opt/ruby の下に配置
* Redmine は ~/redmine に配置
* Passenger で /sbin/httpd と連動。URL は http://サーバ/redmine

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

公式gitからclone

::

   sudo cd /opt/
   sudo chmod a+wrx /opt/
   cd /opt/
   git clone git://github.com/sstephenson/rbenv.git
   mkdir /opt/rbenv/plugins
   cd /opt/rbenv/plugins
   git clone git://github.com/sstephenson/ruby-build.git

/etc/profile に追記

::

   sudo vi /etc/profile

下記３行を追記

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

インストールされたgemパッケージ確認

::

   gem list

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
パスワードについて既定値を getperf としています。本値は適切な名前に変更してください。

::

   mysql -u root -p
   create database redmine default character set utf8;
   grant all on redmine.* to redmine@localhost identified by 'getperf';
   flush privileges;
   exit

Redmine インストール
--------------------

以下から最新のredmineを取得する

::

   http://www.redmine.org/projects/redmine/wiki/Download

ホームの下に redmine を作成

::

   cd ~/
   wget http://www.redmine.org/releases/redmine-3.2.5.tar.gz

配置します

::

   tar zxvf redmine-3.2.5.tar.gz
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
対象はproduction環境のみ、rmagickも除く

::

   bundle install --without development test rmagick --path vendor/bundle

Redmineのビルド

::

   bundle exec rake generate_secret_token
   RAILS_ENV=production bundle exec rake db:migrate

passengerとhttpdの設定
----------------------

httpdモジュールインストールします。

::

   passenger-install-apache2-module

.. note::

   ~/anaconda2/bin のパスが通っていると、anaconda2 の lib を参照してしまい、
   Curlのライブラリが見つからないなどの相性問題が発生する場合がある。
   .bashrc から anaconda2 のパスを外す。

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

