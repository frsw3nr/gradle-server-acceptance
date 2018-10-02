Python セットアップ(オプション)
===============================

.. note::

   構成管理データベースでPython データベース登録スクリプトを実行する場合は、
   以下の手順で Python 環境をセットアップします。

構成概要
--------

* psadmin 管理者ユーザでインストール作業を行います。
* pyenv パッケージ管理ツールで Anaconda 製 Python 3 をインストールします。
* Python パッケージライブラリ管理コマンドを用いて、Python ライブラリをインストールします。

Python インストール
-------------------

Python インストールに必要な依存パッケージをインストールします。

::

   sudo -E yum -y install gcc gcc-c++ make git openssl-devel bzip2-devel zlib-devel \
   readline-devel sqlite-devel bzip2 sqlite \
   zlib-devel bzip2 bzip2-devel readline-devel sqlite sqlite-devel openssl-devel


GitHub サイトから、pyenv をダウンロードします。

::

   git clone git://github.com/yyuu/pyenv.git ~/.pyenv

環境変数を設定します。

::

   vi ~/.bash_profile

最終行に以下を追加します。

::

   export PYENV_ROOT="${HOME}/.pyenv"
   if [ -d "${PYENV_ROOT}" ]; then
       export PATH=${PYENV_ROOT}/bin:$PATH
       eval "$(pyenv init -)"
   fi

環境変数を読込みます。

::

   source ~/.bash_profile

インストール可能なバージョンを確認します。
リストの中から、miniconda3の最新バージョンのパッケージ名を確認します。

::

   pyenv install --list

ここでは、「miniconda3-4.3.30」を選択してインストールします。

::

   pyenv install miniconda3-4.3.30

インストールしたパッケージをデフォルトに設定します。

::

   pyenv global miniconda3-4.3.30

「python --version」で、Pythonの実行を確認します。

::

   python --version
   Python 3.6.3 :: Anaconda, Inc.

Pythonライブラリのインストール
------------------------------

mysqlclient をインストールします。

::

   conda install mysqlclient

Getconfig ホームディレクトリ下の、 cleansing ディレクトリに移動し、
Python 依存 ライブラリをインストールします。

::

   cd ~/gradle-server-acceptance/cleansing/
   pip install -r requirements.txt

.. note::

   ユニットテストを実行する場合、pytest ライブラリをインストールして以下のコマンドを実行します。

   ::

      pip install pytest
      cd ~/gradle-server-acceptance/cleansing/
      py.test tests

Python ライブラリパスにカレントディレクトリを追加します。

::

   vi ~/.bash_profile

最終行に以下の行を追加します。

::

   export PYTHONPATH=.:$PYTHONPATH

環境変数を読込みます。

::

   source ~/.bash_profile

データベース登録スクリプトの動作確認をします。

::

   cd ~/gradle-server-acceptance/cleansing/
   python getconfig/job/template/scheduler_shipping1.py -s -d test1 project1
