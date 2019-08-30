HP iLOサーバ構成収集ツール
==========================

システム概要
------------

HP IA サーバのシステム構成情報を収集します。

* v2.8 以降のバージョンでは、 REST API による収集シナリオに変更しました。
* 下記、[Scripting Tools for Windows PowerShell] は不要となります。

HPE社
[Scripting Tools for Windows PowerShell](https://www.hpe.com/us/en/product-catalog/detail/pip.5440657.html)
を用いて、PowerShell経由で iLOの構成情報を収集します。

注意事項
--------

以下機能を用いるため、iLO5 以上(ProLiant Gen8 servers 以上)のみをサポートします。

* iLO RESTful API- Redfish Conformant- for iLO 4 PowerShell RAW cmdlets support.
* GET_EMBEDDED_HEALTH RIBCLコマンドによるSTORAGE情報採取

ライブラリ最新バージョンの 2.0 では API の仕様変更により、動作しません。
以下の バージョン 1.4.0.2 をインストールしてください。

```
Type:   Utility - Tools
Version:    1.4.0.2(7 Mar 2017)
Operating System(s):    
Microsoft Windows 10 (64-bit) | View all
File name:  HPiLOCmdlets-x64.exe (1.1 MB)
```

複数のバージョンの上記ライブラリをインストールすると、インストールパスが正しく認識されない場合があります。
その場合は以下の既定のインストールディレクトリをパスに追加してください。

```
$Env:PSModulePath = $Env:PSModulePath + ";C:\Program Files\Hewlett-Packard\PowerShell\Modules\HPiLOCmdlets"
```

事前準備
--------

[Scripting Tools for Windows PowerShell](https://www.hpe.com/us/en/product-catalog/detail/pip.5440657.html)
が必要です。
上記ダウンロードサイトから Windows7 64bit版のパッケージをダウンロードして、インストールしてください。
本パッケージは PowerShell 環境で、iLO にアクセスするライブラリを提供します。

ビルド方法
----------

GitHub サイトからリポジトリをクローンして、以下のエクスポートコマンドを実行します。

```
cd server-acceptance-iLO
getconfig -x ../server-acceptance-iLO.zip
      [zip] Building zip: /work/gradle/server-acceptance-iLO.zip
```

利用方法
--------

1. プロジェクトディレクトリ下に、 server-acceptance-iLO.zip を解凍します。
2. 「iLOチェックシート.xlsx」を開き、シート「チェック対象」に検査対象サーバの接続情報を記入します。
3. config/config-iLO.groovy 内のサーバアカウント情報を編集します。
4. server-acceptance ディレクトリに移動し、以下の getconfig コマンドを実行します。

```
getconfig -c ./config/config-iLO.groovy
```

AUTHOR
-----------

Minoru Furusawa <minoru.furusawa@toshiba.co.jp>

COPYRIGHT
-----------

Copyright 2014-2017, Minoru Furusawa, Toshiba corporation.
