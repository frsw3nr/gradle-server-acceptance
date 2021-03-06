Getconfig作業プロジェクト作成
-----------------------------

構築フェーズでの管理対象設備の構成収集手順を記します。
ここでの収集は実機での構成収集（インベントリ収集）となり、
構成収集作業PCにて、構成収集ツール Getconfig を用いて収集を行います。

構成収集作業PCはジョブスケジューラによる構成収集の自動化機能が
ありますが、ここでは手動での構成収集手順を記します。

Getconfig作業プロジェクト作成
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

はじめに検査用プロジェクトを作成します。
administrator ユーザで構成収集作業PCにリモートデスクトップ接続をしてください。
Getconfig を実行する作業ディレクトリは以下とします。

::

   c:\users\administrator\GetconfigProjects

PowerShellを開き、上記作業ディレクトリの下に移動して、
「getconfig -g <プロジェクトホーム>」で指定したディレクトリに
プロジェクトを作成してください。
ここではプロジェクト名を「20170623_構成管理検証用」 という名前で作成します。

::

   cd c:\users\administrator\GetconfigProjects
   getconfig -g 20170623_構成管理検証用

実行すると、以下のディレクトリが生成されます。

::

   cd 20170623_構成管理検証用
   dir

::

   Mode                LastWriteTime     Length Name
   ----                -------------     ------ ----
   d----        2017/07/10     16:54            build
   d----        2017/07/10     16:54            config
   d----        2017/06/12     11:08            image
   d----        2017/06/12     11:08            lib
   d----        2017/07/10     16:54            node
   d----        2017/07/10     16:54            src
   -a---        2017/01/25     11:57        164 .gitignore
   -a---        2017/06/05      9:23      12845 Changes.txt
   -a---        2017/02/07     13:31        580 LICENSE.txt
   -a---        2017/02/06      8:11       3900 Readme.md
   -a---        2017/06/05      9:21      25029 サーバーチェックシート.xlsx

本ディレクトリは構成収集の実行スクリプト、ライブラリ、設定ファイル、
実行結果をまとめたディレクトリセットで、構成収集を実行する際は、
本ディレクトリに移動して構成収集を実行します。

.. note::

   作成したディレクトリはプロジェクトディレクトリと呼びます。