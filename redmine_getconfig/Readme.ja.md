サーバ構築エビデンス収集ツール Redmine プラグイン
=================================================

システム概要
------------

課題管理システム [Redmine](http://www.redmine.org/) に、
[サーバ構築エビデンス収集ツール](https://github.com/frsw3nr/gradle-server-acceptance)
の検査結果の検索機能を追加します。
以下の利用を想定しています。

* 検査PC から、Redmineデータベースにサーバ構築エビデンス検査結果を登録
* Redmine にエビデンス検査結果の検索ページを追加
* Redmine チケットのカスタムフィールドに、エビデンス検査結果の検索ページをリンク

システム要件
------------

* Redmine 3.0 以上
* MySQL 5.5 以上
* gradle-server-acceptance v0.1.6 以上

インストール
------------

事前に、 Redmine 環境が必要です。データベースは MySQL を使用してください。
ここでは、CentOS6 上のユーザホームディレクトリ下に、
Redmine を構築した環境をベースに手順を記します。

**プラグインの配布**

redmine/plugins にディレクトリ移動し、git clone でプラグインプロジェクトをダウンロードします。

```
cd ~/redmine/plugins
git clone http://github.com/frsw3nr/redmine_getconfig
```

**Ruby ライブラリのインストール**

```
cd ~/redmine
bundle install
```

**データベース初期化**

Redmine データベース内にエビデンス収集用テーブルを作成します。

```
bundle exec bin/rake redmine:plugins:migrate
```

作成したテーブルの文字コードを utf8 から utf8mb4 にコード変更します。

```
mysql -u root -p redmine < docs/db_change_utf8_to_utf8mb4.sql
```

利用方法
--------

検査PC 上で
[Gradle server acceptance](https://github.com/frsw3nr/gradle-server-acceptance)
を用いてサーバの構成情報の収集をし、収集した結果を Redmine データベースに登録します。
登録した収集結果は、Redmine 検索ページから検索します。

**検査PC側作業**

MySQL 設定ファイルに Redmine データーベース接続情報を設定します。
c:\server-acceptance\config\cmdb.groovy を開いて、
以下のパラメータを編集します。

```
cmdb.dataSource.username = "redmine_username"
cmdb.dataSource.password = "redmine_password"
cmdb.dataSource.url = "jdbc:mysql://redmine_server:3306/redmine?useUnicode=true&characterEncoding=utf8"
```

設定後、getconfig コマンドで検査を実行し、検査結果を Redmine データベースに登録します。
例として、ostrich Linux サーバ の構成情報の Redmine 登録方法を記します。
getconfig コマンドを用いてエビデンスを収集します。

```
getconfig -s ostrich    # 検査の実行
getconfig -u db         # 検査結果のデータベース登録
```

**Redmine 検索ページでの検査結果検索**

Redmine ベースURL の下の、「/inventory/{検査対象サーバ}」 が検索ページとなります。
上記例の ostrich の検索の場合、URLは以下となります。

```
http://{Redmineサーバ}:3000/inventory/ostrich
```

**Redmine カスタムフィールドのカスタマイズ**

チケットにカスタムフィールど追加することで、
チケット画面から検査結果検索ページをリンクすることが可能です。
メニュー管理、カスタムフィールドで以下のカスタムフィールドを登録してください。


* 書式 : 「テキスト」を選択
* 名称 : 「インベントリ情報」を入力
* 値に設定するリンクURL : 「/redmine/inventory/%value%」または、「/inventory/%value%」を入力
    * Redmine のベース URL に合わせて設定してください

リファレンス
------------

* [Gradle server acceptance](https://github.com/frsw3nr/gradle-server-acceptance)
* [Plugin Tutorial](http://www.redmine.org/projects/redmine/wiki/Plugin_Tutorial)

AUTHOR
------

Minoru Furusawa <minoru.furusawa@toshiba.co.jp>

COPYRIGHT
-----------

Copyright 2014-2017, Minoru Furusawa, Toshiba corporation.
