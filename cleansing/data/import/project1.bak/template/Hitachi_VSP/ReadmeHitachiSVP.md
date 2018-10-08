日立VSPストレージ構成情報収集
=============================

概要
----

以下構成で日立ストレージの構成情報収集を行います

```
[作業PC] --(PowerShell)--> [SVP管理サーバ] --(SVP管理コマンド)--> [VSPストレージ]
```

注意点
------

当初、PowerShellによるSVP管理サーバからストレージ構成情報の収集自動化を検討していましたが、
PowerShellとSVP管理コマンドとの相性が悪く、安定性に課題が多いことから、
PowerShellによる操作自動化を取り下げ、一部手動で構成収集を行う手順としました。
手順は以下の通りです。

1. SVP管理サーバのWebコンソールから手動で構成レポート(*1)を作成、ダウンロード
2. 日立VSPストレージ検査シナリオ編集
3. getconfig 実行。ダウンロードした構成レポートを読み込んでストレージ構成情報を収集

(*1) ストレージ構成レポートCSVをtar圧縮したファイル

使用方法
--------

ディレクトリ直下にある「HitachiVSP構成レポート作成手順.xlsx」 を参照してください

AUTHOR
-----------

Minoru Furusawa <minoru.furusawa@toshiba.co.jp>

COPYRIGHT
-----------

Copyright 2014-2017, Minoru Furusawa, Toshiba corporation.
