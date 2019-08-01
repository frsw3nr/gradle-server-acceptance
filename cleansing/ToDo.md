ボイラープレート作成
====================

```
cd ~/work/gradle/gradle-server-acceptance
cookiecutter https://github.com/nvie/cookiecutter-python-cli.git

full_name [Vincent Driessen]: 
Minoru Furusawa
email [vincent@3rdcloud.com]: 
minoru.furusawa@toshiba.co.jp
github_username [nvie]: 
frsw3nr
project_name [My Tool]: 
Getconfig Cleansing
repo_name [python-mytool]: 
cleansing
pypi_name [mytool]: 
getconfigcleansing
script_name [my-tool]: 
gctool
package_name [my_tool]: 
getconfig
project_short_description [My Tool does one thing, and one thing well.]: 
Getconfig data cleansing
release_date [2015-04-15]: 
2018-08-18
year [2015]: 
2018
version [0.1.0]:
```

インストールテスト

```
cd cleansing
pip install .

gctool -h
gctool test
ls -l `which gctool`
```

ディレクトリ構造調査
========================

* 現行調査
    * DataCleansing
    * getconfig
    * redmine-getconfig
* ディレクトリ作成
    * ドキュメント
    * ライブラリ
    * データ

DataCleansing調査
=================

build ... エビデンス
dat ... 台帳
cleansing ライブラリ
docs ドキュメント

cleansing 下
 <root>    ... 基底, regist singleton stat transfer
 getconfig ... インベントリ
 master    ... 台帳読み込み
 transfer  ... 突合せ
 classify  ... 分類
 regist    ... Redmine 登録
 report    ... レポート

getconfig 調査
==============

<root> ... getconfig
build    ... インベントリ
src      ... ソース
lib      ... ライブラリ
image    ... ドキュメント(イメージ)
config   ... 設定ファイル
template ... テンプレート
docs     ... ドキュメント
node     ... ノード定義

redmine-getconfig 調査
======================

docs ... ドキュメント
redmine ... プラグイン

ディレクトリ構造検討
====================

ルートディレクトリ

```
    redmine-getconfig
        Redmine プラグイン
    cleansing
        getconfig
            cli.py
            master/transfer/classify/regist
        setup.py
        ReadME.md
        config
            clensing.ini
        pip で 「gctool <コマンド>」インストール
    docs
    config
```

プロジェクトホーム

```
    build
        各プロジェクトディレクトリ/build/エビデンス.xlsx
            プロジェクト名とExcel名解析
        All/None/...
    data
        ワーク用台帳
    lib
        clensing
            ルートのcleansingをそのままコピー
            PYTHONPATH 追加
```

機能
    ロード
        フィルタリング
            buildの下のディレクトリリストを更新順に検索
            Excelファイルパスから、プロジェクト名、案件名抽出
    変換
    分類
    登録

ToDo
====

* python cli フレームワーク Click 調査
    サブコマンド
* PYTHONPATH 調査
    import sys
    sys.path.append('your certain directory')
* cleansig API 移行
    
* プロトタイピング
    * エビデンス読込み
    * Redmine登録
    * 台帳突き合わせ

ロード機能プロト
================

要件
----

* インベントリのロード。検査シートの値を読み込み
    * CSVファイルに変換してbuild/cleansing/inventory 下に保存する
    * プラットフォーム名ごとに保存
        * IA / Sparc / Power / Storage / Network
* 旧バージョン(検査シートなし)でも読みたい。チェックシートから抽出
* ノード定義ファイルからも読みたい ※ 検討保留
* ディレクトリ指定で読みたい
    * 複数Excelファイルの解析
    * ホスト名、プラットフォーム、メトリックをキーにリスト化
* ネットワークインベントリはARPリスト一覧を抽出したい
* 台帳のロードの場合、build/cleansing/master下に保存する

仕様検討
--------

* コマンド
    * gctool [--debug] load <ディレクトリ> --grep <キーワード>
    * gctool [--debug] load <Excelパス>
    * gcadmin [--debug] [--grep <キーワード>] [load/transfer/classify/regist] <ディレクトリ|Excelパス>
* 検討項目
    * 旧バージョンの読み込み
        * 検査シートの有無チェック
        * 検査シート定義ファイル読み込み
            * キー: プラットフォーム、メトリック
            * 値: 項目名
        * チェックシートデータを読んで定義項目の値抽出
    * 複数ファイル解析
        * パスから項目抽出
            * プロジェクト名
            * Excel作業名(Excelサフィックス、ポストフィックス)
            * 実行日付
        * 順にExcelファイル読み込み
            * パス抽出項目をキーにデータストア
        * ファイル指定の場合
            * パスから項目抽出して、CSV出力
    * ノード定義ファイルからも読みたい
        * 前処理で対処することとして保留。エビデンスとして検査シートの作成は必要
            * 検査対象に全リスト記入して、予行演習モードだ再集計する

ToDo
----

* ベースパターン実装
    * 1Excelファイルを読み込んでCSV出力
        * チェックシートデータ読み込み
* 旧バージョン読み込み実装
    * 検査シート定義ファイル作成
* 複数ファイル解析実装
    * 別プロジェクトからの実行
    * ファイルパス解析

インポートテスト
----------------



ベースパターン
--------------

* コマンド名は gcadmin に変える
* 基本書式は、 gcadmin [load|transfer|classify|regist] <パス名> とする
* スケジューラで順に実行する
    * load の場合は、load のみ。regist は全て実行
* パス名のパーサ
    * パス名から抽出する。抽出値：プロジェクト、Excel名、日時
    * Excel 名から、ロードスクリプト読み込み
    * click ソースの examples/complex/cli.py が参考になる
    * __import__('complex.commands.cmd_' + name, None, None, ['cli'])
* テストは pytest を使用する
    * py.test test_mod.py   # モジュール内のテストを実行
    * py.test somepath      # 指定したパスの全てのテストを実行
    * py.test -k string     # string を含む名前をもつテストのみを実行
* Click のテストは省略する。暗黙の定義が多く、可読性が下がるため。
* cli.py 定義 (Click 構造)
    * <root> に cli.py を配置し、cli を呼び出す
        * examples/naval/naval.py を参考にする
        * コマンドリスト
            * load(input_path),transfer(input_path),...

