Redmine 連携
============

ToDo
====

* プロトタイプ作成
    * 新 redmine2 構築
    * XlsxFormatIssueExporter プラグイン追加
    * カスタムフィールド設定
    * イシューリスト一覧フォーマット設定
    * 収集ツール CSV読み込み
* 要件検討

プロトタイプ作成
================

新 redmine2 構築
----------------

ホーム下にredmine2 作成

```
cd ~/work
wget http://www.redmine.org/releases/redmine-3.2.5.tar.gz
tar zxvf redmine-3.2.5.tar.gz
mv redmine-3.2.5 ~/redmine2
```

database.ymlを作成

```
cd ~/redmine2/
cp config/database.yml.example config/database.yml
vi config/database.yml   # database: redmine2 に変更
```

テスト用DB作成

```
mysql -u root -p
create database redmine2 default character set utf8;
flush privileges;
exit
```

既存DBバックアップインポート

```
mysqldump -u root -p redmine  > redmine.dmp
mysql     -u root -p redmine2 < redmine.dmp
```

Rubyライブラリインストール

```
sudo yum install ImageMagick ImageMagick-devel
bundle install
```

```
bundle exec rake generate_secret_token
```

Rails起動

```
./bin/rails s -b 0.0.0.0
```

http://サーバ:3000/ で確認

カスタムフィールド設定
----------------------

メニュー、管理、設定、カスタムフィールドで追加
全プロジェクト向け、すべてのトラッカーを指定して登録

```
config :
    server_name : テキスト
    ip : テキスト
    platform : リスト[Linux,Windows,ESXi,SC3000]
account
    os_account_id : テキスト
    remote_account_id : テキスト
    remote_alias : テキスト
rule
    verify_id : テキスト
    NumCpu : 整数
    MemoryGB : 整数
    ESXiHost : テキスト
    HDDType : テキスト
```

重複するカスタムフィールド、不要なカスタムフィールドは削除

イシューリスト一覧フォーマット設定
----------------------------------

プラグインインストール

```
cd ~/redmine2/plugins
git clone https://github.com/two-pack/redmine_xlsx_format_issue_exporter.git
cd ..
bundle install
```

Rails起動

```
./bin/rails s -b 0.0.0.0
```

メニュー「管理」→「設定」→「チケットトラッキング」の「チケットの一覧で表示する項目」
以下の項目を追加する。

```
server_name
ip
platform
os_account_id
remote_account_id
remote_alias
```
