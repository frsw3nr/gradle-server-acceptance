Python セットアップ
===================

Windows ワークフローサーバに、構成管理データベースへのデータ登録用Pythonスクリプトを
設定します。初めに、Python 実行環境をセットアップします。

構成概要
--------

* Chocolatey で miniconda3 (Anaconda製 Python 3)をインストール
* conda install コマンドで mysqlclient ライブラリパッケージをインストール
* pip コマンドで 前節で配布した c:\\server-acceptance\\cleansing 下の
  requirements.txt リストのライブラリパッケージをインストール

Pyhton インストール
-------------------

PowerShell コンソールを開いて、Chocolatey で Python をインストールします。

::

   choco install miniconda3

.. note::

   MySQL用Pythonライブラリ mysqlclient のインストールは C++ コンパイラなど
   環境に依存する場合が多いため、バイナリインストールが可能な、Anaconda Inc. の
    conda パッケージ管理ツールを使用します。

Path 環境変数に、C:\\tools\\miniconda3 と、C:\\tools\\miniconda3\\Scripts を追加します。

::

   $system_path = [System.Environment]::GetEnvironmentVariable("Path", "Machine")
   $system_path = "C:\tools\miniconda3;C:\tools\miniconda3\Scripts;" + $system_path
   [System.Environment]::SetEnvironmentVariable("Path", $system_path, "Machine")

.. note::

   上述の環境変数設定コマンドが利用できない場合はコントロールパネルからパスを追加してください。

   * コントロールパネルを開きます。
   * 「システムとセキュリティ」、「システム」、「システムの詳細設定」、「環境変数」を選択します。
   * システムの環境変数のリストから、Path を選択して、「編集」をクリックします。
      * 値の先頭に C:\\tools\\miniconda3;C:\\tools\\miniconda3\\Scripts; を追加して、パスを追加します。

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

前節の :doc:`../01_inventory_collector_setup/04_getconfig_install`
でインストールした Getconfig ホームディレクトリ下の、 cleansing 
ディレクトリに移動します。

::

   cd C:\server-acceptance\cleansing\

Python 依存ライブラリをインストールします。

::

   pip install -r .\requirements.txt


