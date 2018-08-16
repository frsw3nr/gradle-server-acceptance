ボイラープレート作成
====================

cd ~/work/gradle/gradle-server-acceptance
cookiecutter https://github.com/nvie/cookiecutter-python-cli.git

You've downloaded /home/psadmin/.cookiecutters/cookiecutter-python-cli before. Is it okay to delete and re-download it? [yes]:
full_name [Vincent Driessen]: Minoru Furusawa
email [vincent@3rdcloud.com]: minoru.furusawa@toshiba.co.jp
github_username [nvie]: frsw3nr
project_name [My Tool]: Getconfig Cleansing
repo_name [python-mytool]: cleansing
pypi_name [mytool]: getconfigcleansing
script_name [my-tool]: gctool
package_name [my_tool]: getconfig_cleansing
project_short_description [My Tool does one thing, and one thing well.]: Getconfig data cleansing
release_date [2015-04-15]: 2018-08-18
year [2015]: 2018
version [0.1.0]:

インストールテスト

cd cleansing
pip install .

gctool -h
gctool test
ls -l `which gctool`


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
        getconfig_cleansing
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
