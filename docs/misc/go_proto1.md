Go ボイラープレート
===================

* モックテスト調査
* JSON 
* デザインパターン
* K-Means法
* Excelレポート
* YAML 
* Prometheus

モックテスト調査
----------------

https://qiita.com/minamijoyo/items/cfd22e9e6d3581c5d81f

ざっと内容確認。より細かい内容は次の調査で具体化する

ボイラープレート調査
----------------

リファレンス調査

    https://github.com/moul/golang-boilerplate.git

Glide

### dep

    go get -u github.com/golang/dep/cmd/dep
    mkdir $GOPATH/src/github.com/trydep
    dep init

main.go 書く

    package main

    import "github.com/fatih/color"

    func main() {
        color.Red("赤")
        color.Cyan("青緑")
        color.Blue("青")
        color.Magenta("紫")
    }

dep ensure
dep status

    PROJECT                        CONSTRAINT     VERSION        REVISION  LATEST   PKGS USED
    github.com/fatih/color         v1.7.0         v1.7.0         5b77d2a   v1.7.0   1
    github.com/mattn/go-colorable  v0.0.9         v0.0.9         167de6b   v0.0.9   1
    github.com/mattn/go-isatty     v0.0.9         v0.0.9         e1f7b56   v0.0.9   1
    golang.org/x/sys               branch master  branch master  749cb33   749cb33  1

go run main.go

https://github.com/tmrts/boilr.git

https://github.com/robtec/cli-boilerplate.git

urfave/cli をインポートしている

go run cli1_urfave_cli.go

kingpin か、clif か、cobra

cobra だと設定ファイルとの連携もできる→cobraにする

### clig

https://github.com/izumin5210/clig.git

go get github.com/izumin5210/clig/cmd/clig

インポートするライブラリが多い。もう少し軽量でもよいかも

git clone https://github.com/moul/golang-boilerplate.git

JSON 調査
--------------------

リファレンス

https://qiita.com/nayuneko/items/2ec20ba69804e8bf7ca3

    mkdir json
    dep ensure

twitter の箇所から難しくなる、動的にネストしたJSONの解析

もう一度、試したほうが良い

ラップアップ
-------------

* clig でプロトタイプを作る
* JSON 読み込みサンプル
* デザインパターンサンプル(Visitor, Composite)
* K-Means 法サンプル
* Excel レポートサンプル
* YAML, Prometheus など


