Jenkins セットアップ
====================

システム要件
------------

Windows Server 2012 R2以上のPCが必要です。必要スペックは以下の通りです。

* CPU 1 Core以上
* Memory 4 GB以上
* Disk 100 GB以上

事前準備
--------

* Windows Server 2012 R2 環境を構築します
* リモートデスクトップ接続を許可設定をします
* プロキシー設定をしてプロキシー接続できるようにします
* 以下の手順で 検査用 PC のセットアップを事前に行います
   * :doc:`../..//05_StandaloneTest/01_TestPCSetup`
   * :doc:`../..//05_StandaloneTest/02_DevelopmentOption`

Jenkinsインストール、セットアップ
---------------------------------

Chocolatey で Jenkins をインストールします。

::

   choco install -y jenkins

Chrome から Jenkins サイトに接続します。

::

   http://サーバ:8080/

初期パスワードを聞かれるので、notepad++ で表示されたパスのファイルを開いて、パスワードを入力します。

Insutall suggested plugins を選択します。

管理者ユーザを登録します。

ユーザ名・パスワード・名前・メールアドレスを入力して、 Save and Finish を選択。

