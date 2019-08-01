Getconfigインベントリ収集
=========================

前節でセットアップしたインベントリ収集PCにて、インベントリ収集の動作確認をします。

ここでは、例として以下のサーバの2台のインベントリ収集を行います。

.. csv-table::
   :header: ホスト名, 種別, OS, IP
   :widths: 15, 10, 20, 35

   redmine, 仮想マシン, CentOS 6.7, 192.168.0.11
   jenkins, 仮想マシン, Windows Server 2016, 192.168.0.14

.. note::

   仮想マシンは以下の、 VMWare ESXi に構築したサーバを想定しています。

   * ホスト名 : esxi
   * IP : 192.168.10.100
   * ユーザ : root
   * パスワード : P@ssword

プロジェクトの作成
------------------

PowerShell コンソールを開いて、「getconfig -g "プロジェクト名"」でプロジェクトを作成
します。ここでは、C:\\Users\\Administrator\\Desktop の下に、 project1 という名前のプロジェクト
を作成します。

::

   cd C:\Users\Administrator\Desktop
   getconfig -g project1

検査シナリオExcelシート編集
---------------------------

作成した project1 ディレクトリ直下にある Excel ファイルを開きます。

::

   cd .\project1
   .\サーバチェックシート.xlsx

シート「検査対象」を選択し、インベントリ収集対象の接続情報を入力します。

   .. figure:: ./image/sheet_target.png
      :align: center
      :alt: Target Sheet
      :width: 640px

以下の通り値を入力します。

.. csv-table::
   :header: 列, redmine, jenkins, 備考
   :widths: 15, 10, 10, 45

   検査ドメイン, Linux, Windows, 検査するシナリオID(チェックシート名カッコ内のID)
   対象サーバ, redmine, jenkins, 検査対象サーバ名
   IPアドレス, 192.168.0.22, 192.168.0.14, 検査対象サーバのIP
   ユーザID, Test, Test, connfig.goovy 内に記述した接続 ID
   エイリアス名, redmine, jenkins, ESXiサーバ仮想マシン名

検査シナリオ設定ファイル編集
----------------------------

設定ファイルを編集します。

::

   notepad .\config\config.groovy

各検査対象の接続情報を編集します。

::

   // vCenter接続情報

   account.vCenter.Test.server   = '192.168.10.100'
   account.vCenter.Test.user     = 'test_user'
   account.vCenter.Test.password = 'P@ssword'

   // Linux 接続情報

   account.Linux.Test.user      = 'someuser'
   account.Linux.Test.password  = 'P@ssword'
   account.Linux.Test.work_dir  = '/tmp/gradle_test'

   // Windows 接続情報

   account.Windows.Test.user     = 'administrator'
   account.Windows.Test.password = 'P@ssword'

ESXi サーバの接続IP、各検査対象のユーザID、パスワードを入力します。

検査実行
--------

getconfig コマンドを実行して、検査対象のインベントリ収集を行います。

::

   getconfig

getconfig 実行後、 build 下に保存されたインベントリ収集結果を確認します。

::

   dir .\build

保存された「サーバチェックシート_{日時}.xlsx」を開いてインベントリ収集結果を確認します。

::

   .\build\サーバチェックシート_20180924_060139.xlsx

* シート「検査レポート」がインベントリ収集結果のサマリレポートとなります。
* シート「チェックシート(Linux)」が Linux 検査対象サーバのインベントリとなります。
* シート「チェックシート(Windows)」が Windows 検査対象サーバのインベントリとなります。
* シート「テンプレート(Windows)」以降のシートはデバイス別インベントリの詳細レポートとなります。

一通り結果の確認をし、インベントリ収集結果をコミットをします。

::

   getconfig -u local
