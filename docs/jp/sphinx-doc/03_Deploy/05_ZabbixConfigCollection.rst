Zabbix監視設定収集
------------------

Zabbixテンプレートのダウンロードと解凍
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

PowerShell を開いて、プロジェクトディレクトリに移動します。
以下のダウンロードサイトから「 server-acceptance-zabbix.zip」をダウンロードし、
作成したプロジェクトディレクトリに下に保存します。

::

   http://133.116.134.203/docs/gradle/

.. note::

   ログインユーザとパスワードは psadmin/psadmin です。

7-zipを用いてzipファイルをプロジェクトディレクトリ下に解凍します。
zipファイルを選択し、[右クリック]、[7-Zip]、[ここに展開]を選択します。
解凍したディレクトリ構成は以下となります。

   .. figure:: image/06_zabbixTemplate.png
      :align: center
      :alt: zabbix Template
      :width: 640px

Zabbix 検査シート入力
^^^^^^^^^^^^^^^^^^^^^

プロジェクトディレクトリ下の「監視設定チェックシートZabbix.xlsx」を開いてください。
手順は Linux サーバと同等となります。

.. note::

   「server_name」の項目はZabbixサーバの管理対象ホストのホスト名定義と同じ
   にしてください。大文字、小文字の区別があり、同一名でないと収集は行いません。

   .. figure:: image/06_zabbixTemplate2.png
      :align: center
      :alt: zabbix Template
      :width: 640px

config\\config_zabbix.groovyの編集
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

サクラエディタなど UTF-8 に対応したエディタで、構成ファイル
「config\config_zabbix.groovy」を編集します。
以下のZabbixサーバ接続情報を入力します。

::

   // Zabbix接続情報

   account.Remote.Test.server   = 'zabbixServer1'
   account.Remote.Test.user     = 'admin'
   account.Remote.Test.password = 'getperf'

.. note::

   serverはZabbixサーバのIPアドレス、user、password は Zabbix管理者アカウント
   を入力してください。

Zabbix 検査実行
^^^^^^^^^^^^^^^

「getconfig -c .\\config\\config_zabbix.groovy」を実行して、検査を実行します。

::

   getconfig -c .\config\config_zabbix.groovy

実行後、プロジェクトディレクトリ下の「build」の下に生成された
Excel検査結果を開いて結果を確認します。
Excel検査結果の確認ができたら"getconfig -u local"
でローカルデータベースに検査結果を登録します。

::

   getconfig -c .\config\config_zabbix.groovy -u local

次に、"getconfig -u db"を実行し、
構成管理データベースに検査結果を登録します。
本コマンドは構成管理データベースのRedmineデータベースに検査結果を
登録します。

::

   getconfig -c .\config\config_zabbix.groovy -u db
