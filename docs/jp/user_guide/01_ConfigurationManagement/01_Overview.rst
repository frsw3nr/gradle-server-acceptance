目的
====

システム運用で使用する以下のITインフラ構成情報を収集してデータベース化し、一元管理します。
システム立ち上げ、運用、終息までの間で、システム運用に関わる異なる複数の組織の
メンバーが以下の情報を共有をすることでシステム運用の効率化を図ります。

* 構築後のITインフラ機器インベントリ情報(実機の構成収集結果)
* システム設計書（サーバ設計書、監視設計書など）
* 設計値と実機の値が合っているかの整合性チェック結果
* 構成変更作業で発生するサーバ構成情報の変更記録

システム構成
============

システム構成は以下の通りです。

   .. figure:: ../image/cmdb_overview.png
      :align: center
      :alt: CMDB Overview
      :width: 720px

* 構成管理データベース(Linux サーバ)
   * Redmine チケットに管理対象設備を登録し、計画／構築／運用フェーズで発生する管理対象設備の変更内容をチケットに更新します。
   * Redmine チケットの付帯情報としてホスト名、IPアドレス、システム名、保守情報などのメタ情報を管理します。
   * 構成収集作業PCから収集した管理対象設備のインベントリ情報を Redmine データベースに蓄積し、Redmine チケットとの関連付けをします。

* 構成管理ワークフロー(Windows サーバ)
   * Getconfig 構成収集ツールを用いて管理対象設備のインベントリを収集します。
   * Getconfig は各管理対象設備にリモートアクセスして、各種収集コマンドを実行します。
   * コマンド実行結果から必要な収集項目を抽出し、構成管理データベースの Redmine データベースにアップロードします。
   * 前回のインベントリ収集実行時の差分(変更内容)は Git リポジトリに登録します。
   * 収集した値と、設計書の値が合っているかの整合性チェックをします。
   * インベントリ収集はオフラインのPCでも実行可能です。
     例えば工場出荷前のローカル環境の作業PCでインベントリ収集を行い、作業終了後、オンライン接続し、実行結果を構成管理データベースにアップロードします。

