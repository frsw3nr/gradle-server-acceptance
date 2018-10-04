インベントリ収集ツールセットアップ
===================================

CentOS6 環境に Getconfig インベントリ収集ツールをセットアップします

構成概要
----------

* JDK8 をインストールします
* ビルドツール gradle 2.3 以上をインストールします

Getconfig ダウンロード
----------------------

GitHub から、 gradle-server-acceptance をダウンロードする

バイナリコンパイル

::

   cd gradle-server-acceptance 
   gradle zip

.. note::

   ユニットテストを実行する場合は、以下のコマンドを実行

   ::

      gradle test

Path にGetconfig ホームを追加

::

   vi ~/.bash_profile

最終行に追加

::

   export PATH=$HOME/gradle-server-acceptance:$PATH

実行確認
--------

::

   source ~/.bash_profile
   getconfig -h


