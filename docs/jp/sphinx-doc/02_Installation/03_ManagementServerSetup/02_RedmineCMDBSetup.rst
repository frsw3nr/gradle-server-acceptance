Redmine インストール
====================

Redmine を構成管理DBとしてカスタマイズします。

Redmineカスタマイズ
-------------------

テーマ変更

::

   cd ~/redmine/public/themes
   git clone https://github.com/farend/redmine_theme_farend_basic.git
   mv redmine_theme_farend_basic/ farend_basic

Webブラウザから、http://サーバ/redmine にアクセス
admin/admin で接続

上部メニューから、管理、設定、表示、テーマで「farend_basic」に変更

初期設定
--------

メニュー、管理、全般でタイトルを変更。ITインフラ構成管理

以下サイトを参考にして、日本語の利用に適した設定の通り変更します

::

   https://redmine.jp/tech_note/first-step/admin/

以下サイトを参考にして、アクセス権限の変更をします

::

   http://redmine.jp/faq/administration/require-auth/

"チケット"を"管理対象"に変更します

::

   cd ~/redmine/config/locales
   cp ja.yml ja.yml.bak
   sed -i -e 's/チケット/管理対象/g' ja.yml

プロジェクトの作成
------------------

