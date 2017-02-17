開発用オプション
================

本ソフトウェアソースのコンパイル手順を以下に記します。
実行環境は、:doc:`01_TestPCSetup` で各種ソフトウェアをインストールした検査PCを用います。

ソースコードのダウンロードとビルド
----------------------------------

PowerShell を起動します。
作業用ディレクトリを作成します。
ここでは、c:\\Users\\Public\\work を作業ディレクトリとします。

::

    cd c:\Users\Public
    mkdir .\work
    cd .\work

GitHubサイトからソースコードをダウンロードします。

::

    git clone https://github.com/frsw3nr/gradle-server-acceptance.git

プロジェクトホームに移動し、gradle を用いてビルドします。

::

    cd .\gradle-server-acceptance\
    gradle zipApp

.\\build\\distributions の下に gradle-server-acceptance-0.1.7.zip ファイルが作成されます。

::

    dir .\build\distributions\

        ディレクトリ: C:\Users\Public\work\gradle-server-acceptance\build\distributions

    Mode                LastWriteTime         Length Name
    ----                -------------         ------ ----
    -a----     平成 29/2/17     11:10       35239931 gradle-server-acceptance-0.1.8.zip

本ファイルを、検査用PCからダウンロードできるよう、 FTP サーバなどファイル共有サイトにアップロードしてください。

単体テスト
----------

単体テストをする場合は以下コマンドを実行します。

::

    gradle test

