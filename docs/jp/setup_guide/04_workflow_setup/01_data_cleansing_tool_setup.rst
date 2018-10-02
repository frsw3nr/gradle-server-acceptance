Python セットアップ
===================

構成管理データベースへのデータ登録用Pythonスクリプトを実行出来るように、
Python 環境をセットアップします。

構成概要
--------

* Chocolatey で miniconda3 (Anaconda製 Python 3)をインストール
* conda install コマンドで mysqlclient ライブラリパッケージをインストール
* pip コマンドで 前節で配布した c:\\server-acceptance\\cleansing 下の
  requirements.txt リストのライブラリパッケージをインストール

Pyhton セットアップ
-------------------

PowerShell コンソールを開いて、Chocolatey で Python をインストールします。

::

   choco install miniconda3

.. note::

   MySQL用Pythonライブラリ mysqlclient のインストールは C++ コンパイラなど
   環境に依存する場合が多いため、Anaconda Inc. の conda パッケージを使用します。

Path 環境変数に、C:\tools\miniconda3 と、C:\tools\miniconda3\Scripts を追加します。

* コントロールパネルを開きます。
* 「システムとセキュリティ」、「システム」、「システムの詳細設定」、「環境変数」を選択します。
* システムの環境変数のリストから、Path を選択して、「編集」をクリックします。
   * 値の先頭に C:\tools\miniconda3;C:\tools\miniconda3\Scripts; を追加して、パスを追加します。


Pythonライブラリのインストール
------------------------------

PowerShell コンソールに戻ります。
Pathを通すために、環境変数を更新します。

::

   $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine")

Python パスが正しく通っているかの確認で、python --version で、
メッセージに Anaconda inc. が表示されていることを確認します。

::

   python --version
   conda --version

mysqlclient をインストールします。

::

   conda install mysqlclient

前節でインストールした Getconfig ホームディレクトリ下の、 cleansing 
ディレクトリに移動します。

::

   cd C:\server-acceptance\cleansing\

Python ライブラリをインストールします。

::

   pip install -r .\requirements.txt

