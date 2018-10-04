Python 環境セットアップ
=======================

構成概要
--------

* Git 2 以上が必要
* pyenv で Python 3 をインストールします。
* conda, pip コマンドで構成管理データベース登録用 Python ライブラリをインストールします。
* 構成管理データベース登録用 Python スクリプトの動作確認をします。

Python インストール
-------------------

pyenvをインストールします。

::

   git clone https://github.com/yyuu/pyenv.git ~/.pyenv
   echo 'export PYENV_ROOT="$HOME/.pyenv"' >> ~/.bashrc
   echo 'export PATH="$PYENV_ROOT/bin:$PATH"' >> ~/.bashrc
   echo 'eval "$(pyenv init -)"' >> ~/.bashrc
   source ~/.bashrc

anacondaをインストールします。
pyenvで両方共存させることも可能ですが、condaで2/3の切り替えができるので片方でいいです。

::

   pyenv install -l | grep conda

minicondaの最新版を確認

::

   pyenv install miniconda3-4.3.30

メインのpythonに設定

::

   pyenv rehash
   pyenv global miniconda3-4.3.30

Python パスを追加

::

   echo 'export PATH="$PYENV_ROOT/versions/miniconda3-4.3.30/bin/:$PATH"' >> ~/.bashrc
   source ~/.bashrc

インストールバージョン確認。

::

   python --version
   Python 3.6.3 :: Anaconda, Inc.

condaをアップデート。

::

   conda update conda

Python ライブラリインストール
-----------------------------

mysqlclient をインストールします。

::

   conda install mysqlclient

インストールがすんだら、「exit」で Anaconda Prompt を終了してください。

Pythonライブラリのインストール

前節でインストールした Getconfig ホームディレクトリ下の、 cleansing 
ディレクトリに移動します。

::

   cd ~/gradle-server-acceptance/cleansing

Python ライブラリをインストールします。

::

   pip install -r requirements.txt

Pthon ライブラリパスにカレントディレクトリを追加します。

::

   vi ~/.bashrc

   export PYTHONPATH=.:$PYTHONPATH

   source ~/.bashrc

.. note::

   ユニットテストを実行する場合
   pytest ライブラリをインストール。

   ::

      pip install pytest

   テスト実行。詳細は、testsディレクトリ下の各テストスクリプトを参照

   ::

      cd ~/gradle-server-acceptance/cleansing
      py.test tests/   

動作確認
--------

