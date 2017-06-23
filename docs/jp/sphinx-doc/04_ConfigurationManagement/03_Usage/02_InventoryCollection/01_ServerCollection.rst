Getconfig プロジェクトの作成
============================

はじめに検査用プロジェクトを作成します。
PowerShellを開き、 「getconfig -g <プロジェクトホーム>」で指定したディレクトリにプロジェクトを作成します。
作業ディレクトリ、c:\\users\\administrator\\GetconfigProjects の下に移動して、
「20170623_監視サイト検証環境構築」 というプロジェクトを作成します。

   .. figure:: image/01_createProject.png
      :align: center
      :alt: New Project
      :width: 720px

実行すると指定したディレクトリ下に以下のディレクトリ、ファイルが生成されます。

   .. figure:: image/01_createProject2.png
      :align: center
      :alt: New Project
      :width: 720px

Linux サーバ構成収集
====================

Linux サーバ管理対象の設定
--------------------------

プロジェクトディレクトリに移動し、「サーバチェックシート.xlsx」を編集します。
シート「チェック対象」の入力列に 検査対象の Linux サーバの情報を設定します。
はじめに「platform」に"Linux"を選択してください。
「virtualization」に仮想化OSの場合は、"VM"、オンプレサーバの場合は"オンプレ"を選択してください。

   .. figure:: image/02_registTargetServer.png
      :align: center
      :alt: New Project
      :width: 720px

* 「platform」,「virtualization」項目選択後に、「～を入力して下さい」と表示されたセルの値を入します
   * server_name : サーバ名を入力
   * Ip : IPアドレスを入力
   * os_account_id : "Test" を入力
      * LinuxのアカウントID
      * config\\config.groovy に記述
   * os_specific_password
      * OSアカウントで特定のパスワード設定が必要な場合はパスワードを入力
      * 未記入の場合はconfig\\config.groovyの値が反映
   * remote_account_id : "Test" を入力
      * vCenter サーバもしくは、 ESXi ホストのアカウントID
      * config\\config.groovy に記述
   * remote_alias : vCenter 側で管理しているVMのエイリアス名。
   * vSphere Client 管理コンソールからメニュー、ホーム、インベントリを選択し、
     画面左側のツリーリストに表示されるVM名を入力

config\\config.groovyの編集
---------------------------

以下の行の接続アカウント情報を編集します。

::

   // vCenter接続情報

   account.Remote.Test.server   = '192.168.10.100'
   account.Remote.Test.user     = 'test_user'
   account.Remote.Test.password = 'P@ssword'

   // Linux 接続情報

   account.Linux.Test.user      = 'someuser'
   account.Linux.Test.password  = 'P@ssword'
   account.Linux.Test.work_dir  = '/tmp/gradle_test'
   // account.Linux.Test.logon_test = [['user':'test1' , 'password':'test1'],
   //                                  ['user':'root'  , 'password':'P@ssw0rd']]

* アカウントIDについて
   * 各サーバで接続アカウント情報が異なる場合は、アカウントIDを変えて複数アカウント情報を設定してください。
   * シート「検査対象」の"os_account_id"で指定します
   * account.Remote.、account.Linux.の後の文字列がアカウントIDとなります
* Linuxログオンテストについて
   * 最終行の"account.Linux.Test.logon_test"に接続テスト用アカウントを指定します
   * テストを行う場合はコメントアウトを外して設定してください

Linux検査実行
-------------

PowerShellを開いて、プロジェクトディレクトリに移動して、getconfig を実行します

   .. figure:: image/03_testLinuxServer.png
      :align: center
      :alt: New Project
      :width: 720px

Windows サーバ構成収集
======================

Windows サーバ管理対象の設定
----------------------------

管理対象 Windows サーバの事前準備
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

管理対象の Windows サーバに作業 PC から構成収集用の PowerShell コマンドをリモート実行出来る様、
接続許可設定をします。

パブリックネットワークの場合の構成変更
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

ネットワーク構成がパブリックネットワークの場合、ネットワーク構成をプライベートかドメインに変更します。
ここではプライベートへの変更手順を記します。

確認用コマンド

::

   Get-NetConnectionProfile -IPv4Connectivity Internet

設定用コマンド

::

   Set-NetConnectionProfile -InterfaceAlias (Get-NetConnectionProfile -IPv4Connectivity Internet).InterfaceAlias -NetworkCategory Private

WinRM リモート管理設定
~~~~~~~~~~~~~~~~~~~~~~

管理者でPowerShell を開いて、以下コマンドを実行します。

::

   winrm quickconfig

本設定は以下設定を行います。

* WinRM用のservice起動設定
* WinRM用のLisner作成
* WinRM用のファイヤーウォールの設定

一時的な設定変更で検査をする場合
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

前頁の設定はサーバ運用開始後の検査も想定した恒久的な設定となりますが、
一時的に検査作業時のみ設定をする場合、 Windows 環境で以下の設定変更をします。

PowerShell から以下のコマンドでファイヤーウォールの無効化設定をします。

::

   Get-NetFirewallProfile | Set-NetFirewallProfile -Enabled false

検査終了後、基に戻す場合は以下コマンドで有効化設定をします。

::

   Get-NetFirewallProfile | Set-NetFirewallProfile -Enabled true


PowerShell リモートアクセス許可の有効化
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Windows Server 2012 より前のOSでは、PowerShell のリモートアクセス許可が無効化されている場合があります。
その場合、PowerShellを管理者権限で実行して、PowerShell コンソールから以下のコマンドで有効化します。

::

   Enable-PSRemoting

また、「認識されないネットワーク」があり、Publicとして設定されている場合、以下のオプションを追加して有効化を試してください。

::

   Enable-PSRemoting -SkipNetworkProfileCheck

オプションを指定しない場合、「Public に設定されているため、WinRM ファイアウォール例外は機能しません。 ネットワーク接続の種類を Domain または Private に変更して、やり直してください。 」 というエラーが発生する場合があります。

.. note:: Windows Server 2012 R2 以上の場合、リモートアクセス許可の既定値は有効化です


Windows検査シート入力
---------------------

   .. figure:: image/02_registTargetWindows.png
      :align: center
      :alt: New Project
      :width: 720px

* プロジェクトディレクトリに移動し、「サーバチェックシート.xlsx」を編集します
* シート「チェック対象」の入力列に 検査対象の Windows サーバの情報を設定します
* はじめに「platform」に"Windows"を選択してください
* 「virtualization」に仮想化OSの場合は、"VM"、オンプレサーバの場合は"オンプレ"を選択してください
* 各項目の入力手順はLinux検査と同じとなります


config\\config.groovyの編集
---------------------------

notepad++などでconfig\\config.groovy を開き、以下の行の接続アカウント情報を編集します。
アカウントID,Windowsログオンテストの入力手順は、Linuxと同様です。

::

   // vCenter接続情報

   account.Remote.Test.server   = '192.168.10.100'
   account.Remote.Test.user     = 'test_user'
   account.Remote.Test.password = 'P@ssword'

   // Windows 接続情報

   account.Windows.Test.user     = 'administrator'
   account.Windows.Test.password = 'P@ssword'
   // account.Windows.Test.logon_test = [['user':'test1' , 'password':'test1'],
   //                                    ['user':'test2' , 'password':'test2']]


Windows検査実行
---------------

PowerShellを開いて、プロジェクトディレクトリに移動して、getconfig を実行します。

   .. figure:: image/03_testWindowsServer.png
      :align: center
      :alt: New Project
      :width: 720px

実行後、プロジェクトディレクトリ下のbuildの下に生成されたExcel検査結果を開いて結果を確認します。

   .. figure:: image/04_verifyTestResult.png
      :align: center
      :alt: New Project
      :width: 720px

Excel検査結果の確認ができたら"getconfig -u local"でローカルデータベースに検査結果を登録します。

   .. figure:: image/04_verifyTestResult2.png
      :align: center
      :alt: New Project
      :width: 720px

ESXiホストの検査
================

ESXiホスト検査シート入力
------------------------

プロジェクトディレクトリに移動し、「サーバチェックシート.xlsx」を編集します

   .. figure:: image/02_registTargetESXi.png
      :align: center
      :alt: Regist ESXi server
      :width: 720px

* シート「チェック対象」の入力列に 検査対象の ESXi ホストの情報を設定します
* はじめに「platform」に"VMHost"を選択してください
* 「virtualization」は未記入のままにしてください
* 各項目の入力手順はLinux検査と同じとなります

config\\config.groovyの編集
---------------------------

notepad++などでconfig\config.groovy を開き、以下の行の接続アカウント情報を編集します

::

   // VMHost 接続情報

   account.VMHost.Test.user      = 'root'
   account.VMHost.Test.password  = 'P@ssword'

ESXiホスト検査実行
------------------

PowerShellを開いて、プロジェクトディレクトリに移動して、getconfig を実行します

   .. figure:: image/03_testESXiServer.png
      :align: center
      :alt: Regist ESXi server
      :width: 720px

実行後、プロジェクトディレクトリ下のbuildの下に生成されたExcel検査結果を開いて結果を確認します。
Excel検査結果の確認ができたら"getconfig -u local"でローカルデータベースに検査結果を登録します。

ドライランモード
================

* getconfig実行オプションで、 -d オプションを追加すると予行演習(DryRun)モードを実行します
* 予行演習モードを使用すると、検査対象へのアクセスをせずに、保存済みの収集ログから再検査を行います
* 一部の検査対象を絞り込んで検査結果を作成したい場合などに使用します
* はじめに全検査対象の検査を実行します
* ここでは、例として、Linux,Windows,ESXiホストの計3台の検査を行います

   .. figure:: image/02_registTargetServerDryRun.png
      :align: center
      :alt: Regist Dry Run
      :width: 720px

getconfig で検査を実行したら getconfig -u local でローカルディレクトリに検査結果をコピーします

   .. figure:: image/05_dryRun1.png
      :align: center
      :alt: Regist Dry Run
      :width: 720px

再び検査シートのシート「検査対象」を開いて、検査結果の再作成が必要なサーバのみを絞り込みます。
ここでは、Linuxの列のみに絞り込みます

   .. figure:: image/05_dryRun2.png
      :align: center
      :alt: Regist Dry Run
      :width: 720px

getconfig -d オプションで、予行演習モードで実行します
検査対象へのアクセスをせずに再検査を行います
実行後、生成された検査結果シートは絞り込んだ対象サーバのみになります

   .. figure:: image/05_dryRun3.png
      :align: center
      :alt: Regist Dry Run
      :width: 720px


