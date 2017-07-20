Getconfig 動作確認
^^^^^^^^^^^^^^^^^^

作成した Getconfig プロジェクトを手動で実行して動作確認をします。
ここでは以下の順番で段階的に実行して動作確認をします。

1. 従来の手動による Getconfig によるインベントリ収集
2. 手動でRedmine 設備チケットの情報を抽出して Excel 検査仕様書を作成する手順
3. 2を自動で行う手順

Zabbix 検査シート入力
~~~~~~~~~~~~~~~~~~~~~

プロジェクトディレクトリ下の「監視設定チェックシートZabbix.xlsx」を開いて、
以下の入力をしてください。

   .. figure:: image/06_zabbixSheet1.png
      :align: center
      :alt: zabbix Sheet 1
      :width: 640px

* server_name は「構成管理データベース」と「構成収集作業PC」のホスト名を入力します。

   .. note::

      ホスト名は Zabbix で設定したホスト名と同じにします。
      実際の環境に合せて設定してください。

* platform は 「Zabbix」 固定にしてください。
* remote_account_id は「Test」に固定してください。次のconfig_zabbix.groovy 
  設定ファイル内パラメータ値を使用します。
* verify_id は「RuleAP」固定にしてください。

.. note::

   手順の詳細は、:doc:`../../03_Deploy/05_ZabbixConfigCollection` を参照してください。


config\\config_zabbix.groovyの編集
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

サクラエディタなど UTF-8 に対応したエディタで、構成ファイル
「config\\config_zabbix.groovy」を編集します。

::

   sakura config\config_zabbix.groovy

以下のZabbixサーバ接続情報を入力します。

::

   // Zabbix接続情報

   account.Remote.Test.server   = 'zabbixServer1'
   account.Remote.Test.user     = 'admin'
   account.Remote.Test.password = 'getperf'

.. note::

   serverはZabbixサーバのIPアドレス、user、password は Zabbix管理者アカウント
   を入力してください。

手動での Zabbix 設定収集実行
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

「getconfig -c .\\config\\config_zabbix.groovy」を実行してください。

::

   getconfig -c .\config\config_zabbix.groovy

実行後、プロジェクトディレクトリ下の「build」の下に生成されたExcel検査結果を
開いて結果を確認します。
監視設定がされている場合、検査結果は以下となります。

   .. figure:: image/06_zabbixTemplate3.png
      :align: center
      :alt: zabbix Template
      :width: 720px

Redmine チケットから検査対象抽出
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Redmine チケットから検査対象設備の情報を抽出して、Excel検査仕様書を
作成します。前回の getconfig コマンドに、-r(--redmineオプションの短縮形)
を追加してgetconfig を実行してください。

::

   getconfig -c .\config\config_zabbix.groovy -r

コマンド実行後、コンソールから出力されるメッセージに従って値を入力してください。

   .. figure:: image/06_zabbixRedmine1.png
      :align: center
      :alt: zabbix redmine
      :width: 720px

* Project の入力は、構成管理データベースと構成収集作業PCが所属するプロジェクトを選択してください。
* Status、Version、Trackerの入力は 「(指定なし)」[0]を入力してください。

Trackerまでの入力が終わり、「検索したサーバは以下の通りです。よろしいですか?」
の後に y を入力すると、以下の通り、既存の Excel 検査仕様書をバックアップして、
新規にRedmineの抽出結果を基にしたExcel検査仕様書を作成します。

::

   15:55:03 INFO  j.c.t.I.a.EvidenceSheet - Backup: ./監視設定チェックシート_Zabbix-backup.xlsx
   15:55:03 INFO  j.c.t.I.a.EvidenceSheet - Update: ./監視設定チェックシート_Zabbix.xlsx

実際に作成された「監視設定チェックシート_Zabbix.xlsx」を開くと、
シート「チェック対象」に、Redmine 設備チケットの情報が登録されていることを確認します。
これ後の作業は作成されたExcel 検査仕様書を基に、前節と同じ手順で
getconfig を実行します。

::

   getconfig -c .\config\config_zabbix.groovy

Redmine チケット情報抽出の自動化
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

前節のRedmine チケット情報抽出はコンソールから手動で入力していましたが、
本作業を自動化します。

config_zabbix.groovy ファイルを開いてください。

::

   sakura .\config\config_zabbix.groovy

以下のredmine.default_filter_options パラメータを編集します。
本パラメータは Redmine にチケット検索をする際のフィルター設定となります。

::

   redmine.default_filter_options = [
       'project': '監視サイト',
       'status': '%',
       'version': '%',
       'tracker': '%',
   ]

* project の設定は検査対象設備チケットが所属するプロジェクト名を指定してください。
* その他の設定は'%'(ワイルドカード)を指定してください。

編集後、前回の getconfig コマンドに --silent オプションを追加してください。

::

   getconfig -c .\config\config_zabbix.groovy -r --silent

実行すると、前回のコンソール入力のメッセージ表示がなくなり、
Excel 検査仕様書の作成までが実行されます。
config_zabbix.groovy に Redmine チケットのフィルター設定をすることにより、
チケットの抽出からExcel 検査仕様書の作成までを自動化します。

.. note::

   後述の Jenkins で定期実行するジョブには本設定を行い、手入力を介さず、
   コマンドで自動実行できる準備をします。
