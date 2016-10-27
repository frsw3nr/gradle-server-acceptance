サーバ構築エビデンス収集ツール
=====================================

システム概要
--------------

ディレクトリ構成
----------------

インストール手順
================

事前準備
--------

インストール
-----------

**デプロイ**

    TARGET_DIR=~/work/tmp/gradle-server-acceptance

    # jar ファイルコピー
    mkdir -p $TARGET_DIR/build/libs
    cp ./build/libs/gradle-server-acceptance-0.1.0-all.jar $TARGET_DIR/build/libs

    # 設定ファイル、バッチスクリプトコピー
    cp -r getconfig getconfig.bat config/ lib check_sheet.xlsx $TARGET_DIR

    # テストリソースコピー
    mkdir -p $TARGET_DIR/src/test/resources/log/
    cp -rp src/test/resources/log/* $TARGET_DIR/src/test/resources/log/

**デプロイ後の構成**

主なファイル構成は以下の通り。

X:.
├check_sheet.xlsx    # チェックシート
├─config            # 設定ファイル
├─src
│  └─test
│      └─resources # DryRunテスト用データ
│          └─log
├─build
│  └─libs          # Jarファイル本体
└─lib
    ├─template      # PowerShell テンプレート
    └─InfraTestSpec # 検査コード


利用手順
========

注意事項
--------


Refference
-----------


AUTHOR
-----------

Minoru Furusawa <minoru.furusawa@toshiba.co.jp>

COPYRIGHT
-----------

Copyright 2014-2016, Minoru Furusawa, Toshiba corporation.

LICENSE
-----------

