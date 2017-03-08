SC3000ストレージ構成情報収集
============================

システム概要
------------

* Toshiba Total Storage Platform SC3000 の構成情報を収集します。
* `TSPM/Utility`_ を用いてSC3000ストレージのコントローラ、RAIDグループ、LU情報を収集します。

.. _TSPM/Utility: http://home1.toshiba-sol.co.jp/spji/ttsp/lineup_TSPM.htm

.. note::

   TSPM/Utility のコマンド 「tsuacs」を用いてネットワーク経由で SC3000 の
   構成情報を収集します。
   `TSPM/Utility`_ についての詳細はリンクの取扱説明書を参照してください。

システム要件
------------

検査用PC
~~~~~~~~

   * :doc:`../../02_Installation/01_TestPCSetup` で検査用PCをセットアップしてください
   * `TSPM/Utility`_ のインストールが必要です。インストールメディアを入手し、ユーティリティをインストールしてください

検査対象の SC3000
~~~~~~~~~~~~~~~~~

   * SC3000 コントローラにネットワークアドレス設定が必要です。検査用PC と LAN 接続できるアドレスを設定してください

利用方法
--------

SC3000 テンプレートのインストール
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

ダウンロード検査用から、"server-acceptance-sc3000.zip" をダウンロードします。
 7-zip を用いて、プロジェクホームディレクトリ下に "server-acceptance-sc3000.zip"
を解凍します。解凍したディレクトリ下で検査を実行します。

検査仕様の編集と実行
~~~~~~~~~~~~~~~~~~~~

「チェックシート_SC3000.xlsx」を開き、シート「チェック対象」に検査対象ストレージの接続情報を記入します。

   * server_name : SC3000ストレージのシステム名を入力
   * ip : コントローラ IP
   * platform : 'SC3000'
   * verify_id : 'Rule1'

解凍したディレクトリに移動し、getconfig コマンドを実行します。使用方法は以下の通りです。

::

   cd c:\work\server-acceptance-sc3000    # 解凍したディレクトリ
   getconfig -c .\config\config-sc3000.groovy

