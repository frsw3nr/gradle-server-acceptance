Getconfig Redmineプラグインインストール
=======================================

前節でインストールした Redmine を構成管理データベース用プラグインをインストールします。

* redmine-getconfig プラグインのインストール
* Redmine 設定変更

Getconfig プラグインの追加
--------------------------

Redmineリンクから参照できるようにします。

GitHub から管理者ユーザのホーム下に、Getconfig モジュールをダウンロードします。

::

   cd ~/
   git clone https://github.com/frsw3nr/gradle-server-acceptance


プラグイン用ライブラリを ~/redmine/plugins 下にリンクします。

::

   cd redmine/plugins
   ln -s ~/gradle-server-acceptance/redmine_getconfig/ redmine_getconfig

プラグイン用のテーブルをインストールします。

::

   cd ~/redmine
   bundle install
   RAILS_ENV=production bundle exec bin/rake redmine:plugins:migrate

テーブルの文字コードの設定を utf8 から utf8mb4 に変更します。

::

   mysql -u root -p redmine < plugins/redmine_getconfig/docs/db_change_utf8_to_utf8mb4.sql

