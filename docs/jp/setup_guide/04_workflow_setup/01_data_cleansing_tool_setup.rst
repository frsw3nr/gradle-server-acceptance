Python セットアップ
===================

インベントリ収集PCにて、構成管理データベースへのデータ登録用Pythonスクリプトを
実行出来るよう、Python 環境をセットアップします。

構成概要
--------

* Chocolatey で miniconda3 Python をインストール
* 環境の依存度が強い mysqlclient ライブラリは conda install コマンドでインストール
* 前節でインストールした c:\\server-acceptance\\cleansing 下の requirements.txt
  のパッケージリストをインストール

Pyhton セットアップ
-------------------

PowerShell コンソールを開いて、Chocolatey で Python をインストールします。

::

   choco install miniconda3

.. note::

   MySQL用Pythonライブラリ mysqlclient のインストールは C++ コンパイラなど
   環境に依存する場合が多いため、Anaconda Inc. の conda パッケージを使用します。

Path 環境変数に、C:\tools\miniconda3 を追加します。

* コントロールパネルを開きます。
* 「システムとセキュリティ」、「システム」、「システムの詳細設定」、「環境変数」を選択します。
* システムの環境変数のリストから、Path を選択して、「編集」をクリックします。
   * 値の先頭に C:\tools\miniconda3; を追加して、パスを追加します。


MySQLdb インストール
--------------------

スタートメニューを選択して、Anaconda Prompt を起動します。
mysqlclient をインストールします。

::

   conda install mysqlclient

インストールがすんだら、「exit」で Anaconda Prompt を終了してください。

Pythonライブラリのインストール
------------------------------

PowerShell コンソールを開きます。
Pathを通すために、環境変数を更新します。

::

   $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine")

Python パスが正しく通っているかの確認で、python --version で、
メッセージに Anaconda inc. が表示されていることを確認します。

::

   python --version
   Python 3.6.5 :: Anaconda, Inc.

前節でインストールした Getconfig ホームディレクトリ下の、 cleansing 
ディレクトリに移動します。

::

   cd C:\server-acceptance\cleansing\

Python ライブラリをインストールします。

::

   pip install -r .\requirements.txt

