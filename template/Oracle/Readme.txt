Oracle データベース構成収集ツール
=================================

システム概要
------------

Oracle データベースのシステム構成情報を収集します。

* Oracle 10g, 11g, 12c をサポートします
* JDBC ドライバー経由で、Oracle データベースに接続し、ディクショナリー情報を検索します

事前準備
--------

ディクショナリー情報検索用の Oracle ユーザの作成が必要となります。
既存の Oracle ユーザでもアクセスは可能です。
新規の場合、検査対象のサーバにて以下例のコマンドを実行して、Oracle ユーザを作成してください。

```
sqlplus /nolog
```

```
connect / as sysdba

CREATE USER ZABBIX IDENTIFIED BY {パスワード} DEFAULT TABLESPACE SYSTEM TEMPORARY TABLESPACE TEMP PROFILE DEFAULT ACCOUNT UNLOCK;
GRANT CONNECT TO ZABBIX;
GRANT RESOURCE TO ZABBIX;
ALTER USER ZABBIX DEFAULT ROLE ALL;
GRANT SELECT ANY TABLE TO ZABBIX;
GRANT CREATE SESSION TO ZABBIX;
GRANT SELECT ANY DICTIONARY TO ZABBIX;
GRANT UNLIMITED TABLESPACE TO ZABBIX;
GRANT SELECT ANY DICTIONARY TO ZABBIX;
```

利用方法
--------

以下の手順で収集シナリオを設定します。

1. 「getconfig -g {ディレクトリ}」プロジェクトを作成します
2. 作成したプロジェクトディレクトリに移動し、 検査シート「template\Oracle\Oracle設定チェックシート.xlsx」 を開きます。
3. シート「検査対象」にて、以下のデータベースアクセス情報を入力します。
    * domain : 固定で 「Oracle」 と入力
    * server_name : 任意の名前を入力してください
    * ip : 検査対象 Oracle の IP アドレスを入力してください
    * account_id : Oracle 接続ユーザの ID を入力してください。ID は後述の config_oracle.groovy 設定ファイル内のアカウント設定の ID となります。
    * template_id : 固定で 「Oracle」 と入力
    * remote_alias : Oracle サービス名を入力してください
    * specific_password(任意) : config_oracle.groovy と異なるパスワードを指定する場合は入力してください
    * compare_server(任意) : 収集結果の値の比較をする場合は、server_name を入力してください
4. サクラエディタなど UTF-8に対応したエディタで .\template\Oracle\config_oracle.groovy を開いてください
5. config_oracle.groovy 内のアカウントID 情報を編集します。
   パラメータ名の「account.Oracle」の後のIDがExcelシートに記入するIDとなります

```
// Oracle接続情報

account.Oracle.Test.user     = 'zabbix'
account.Oracle.Test.password = '{パスワード}'
account.Oracle.Test.port     = 1521
```

以下の getconfig コマンドを実行します。

```
cd {プロジェクトディレクトリ}
getconfig -c .\template\Oracle\config_oracle.groovy
```

