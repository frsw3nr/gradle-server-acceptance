Solarisサーバ収集
-----------------

Solarisテンプレートのダウンロードと解凍
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

PowerShell を開いて、プロジェクトディレクトリに移動します。
以下のダウンロードサイトから「 server-acceptance-solaris.zip」をダウンロードし、
作成したプロジェクトディレクトリに下に保存します。

::

   http://133.116.134.203/docs/gradle/

.. note::

   ログインユーザとパスワードは psadmin/psadmin です。

7-zipを用いてzipファイルをプロジェクトディレクトリ下に解凍します。
zipファイルを選択し、[右クリック]、[7-Zip]、[ここに展開]を選択します。
解凍したディレクトリ構成は以下となります。

   .. figure:: image/05_solarisTemplate.png
      :align: center
      :alt: Solaris Template
      :width: 640px

Solaris検査シート入力
^^^^^^^^^^^^^^^^^^^^^

「Solarisチェックシート.xlsx」を開いて、シート「検査対象」
に検査対象サーバの項目を入力します。

   .. figure:: image/05_solarisTemplate2.png
      :align: center
      :alt: Solaris Template2

手順は Linux サーバと同等となります。

config\\config.groovyの編集
^^^^^^^^^^^^^^^^^^^^^^^^^^^

サクラエディタなど UTF-8 に対応したエディタで、構成ファイル
「config\config_solars.groovy」を編集します。
以下のOSアカウント情報を入力します。

::

   account.Solaris.Test.user = 'someuser'
   account.Solaris.Test.password = 'P@ssword'

Solaris検査実行
^^^^^^^^^^^^^^^

「getconfig -c .\config\config_solaris.groovy」を実行して、検査を実行します。

::

   getconfig -c .\config\config_solaris.groovy

実行後、プロジェクトディレクトリ下の「build」の下に生成された
Excel検査結果を開いて結果を確認します。
Excel検査結果の確認ができたら"getconfig -u local"
でローカルデータベースに検査結果を登録します。

::

   getconfig -c .\config\config_solaris.groovy -u local

.. note::

   -c 実行オプションで設定ファイルを指定して実行してください。

次に、"getconfig -u db"を実行し、
構成管理データベースに検査結果を登録します。
本コマンドは構成管理データベースのRedmineデータベースに検査結果を
登録します。

::

   getconfig -c .\config\config_solaris.groovy -u db
