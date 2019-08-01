Getconfig 収集プロジェクト作成
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

構成収集作業PCで Getconfig 収集プロジェクトを作成します。

Getconfigプロジェクト初期化
~~~~~~~~~~~~~~~~~~~~~~~~~~~

administrator ユーザで構成収集作業PCにリモートデスクトップ接続をしてください。
Getconfig を実行する作業ディレクトリは以下とします。
存在しない場合はディレクトリを作成してください。

::

   c:\users\administrator\jobs

PowerShellを開き、上記作業ディレクトリの下に移動して、
プロジェクト名を「test_zabbix_cofing」 という名前でプロジェクトを作成してください。

::

   cd c:\users\administrator\jobs
   getconfig -g test_zabbix_cofing

実行すると、以下のディレクトリが生成されます。

Zabbixテンプレートのダウンロードと解凍
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

PowerShell を開いて、プロジェクトディレクトリに移動します。
GitHub サイトから、「 server-acceptance-zabbix-master.zip」をダウンロードし、
作成したプロジェクトディレクトリに下に保存します。

::

   https://github.com/frsw3nr/server-acceptance-zabbix

7-zipを用いてzipファイルをプロジェクトディレクトリ下に解凍します。
zipファイルを選択し、[右クリック]、[7-Zip]、[開く]を選択します。
フォルダ server-acceptance-zabbix-master をダブルクリックして移動します。
[展開]をクリックして、プロジェクトディレクトリに展開します。
解凍したディレクトリ構成は以下となります。

   .. figure:: image/06_zabbixTemplate.png
      :align: center
      :alt: zabbix Template
      :width: 640px

Gitリポジトリ管理対象フィルタリング設定
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Gitリポジトリ設定で、プロジェクトディレクトリ下の
.gitignore というGitリポジトリ管理対象フィルタリング設定ファイル
を編集します。

::

   sakura .gitignore

既定の設定内容を削除して以下の通り変更して保存します。

::

   build/*
   src/*
   !.gitkeep

これは、build と src 下の変更は Git による変更管理対象外として、
それ以外のファイル、ディレクトリを変更管理対象とする設定となります。
