検査PCセットアップ
==================

スタンドアローン構成で、検査用 Windows PC環境をセットアップします。

システム要件
------------

Windows 7 64bit、Windows Server 2012 R2以上のPCが必要です。必要スペックは以下の通りです。

* CPU 1 Core以上
* Memory 4 GB以上
* Disk 100 GB以上

事前準備
--------

ネットワークプロキシーの設定
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

イントラネット環境でプロキシ―構成が必要な場合、InternetExploer を起動し、プロキシ―設定をします。
InternetExploler を開いて、「インターネットオプション設定」を選択します。
「接続」、「LAN設定」の順に選択します。

プロキシーサーバの欄にプロキシーのアドレス、ポート番号を入力します。

「詳細設定」を選択し、「プロキシーの設定除外」の欄に、検査対象の vCenter のアドレスを追加します。
また、本ツールのダウンロードサイトなどイントラネット内サーバでアクセスが必要なサーバのアドレスを追加します。

SSL証明書のインストール(社外 SSL Webアクセスの制限がある場合)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

プロキシ―構成で外部へのウェブアクセスでSSL通信のセキュリティ強化がされている場合、
SSL証明書の追加インストールが必要な場合があります。
SSL証明書がインストールされていない場合、この後のパッケージのインストールで、
「セキュリティ証明書には問題があります」とエラーが発生する場合があります。
社内の利用規定にしたがって、SSL証明書をインストールしてください。

PowerShell のインストール
~~~~~~~~~~~~~~~~~~~~~~~~~

OSが以下のバージョンの場合、必須パッケージの PowerShell 4.0 以上をインストールします。

* Windows7, Windows Server 2008 R2, Windows Server 2012

.. note::


   Windows 8 以上、Windows Server 2012 R2 以上は、PowerShell 4.0 以上が標準インストールされているので本手順は不要です。


PowerShell の必須バージョンは 4.0 以上となり、ここでは最新の PowerShell 5.0 をインストールします。
PowerShell 5.0 は、Windows Management Framework 5.0 (WFM 5.0) にバンドルされています。

事前に、Microsoft .NET Framework 4.5のインストールが必要です。
以下のURLよりダウンロードしてインストールしてください。

::

   http://www.microsoft.com/en-us/download/details.aspx?id=30653

WMF5.0を以下のURLからダウンロードして、インストールします。

::

   https://www.microsoft.com/en-us/download/details.aspx?id=50395

PowerShell のリモートアクセス設定
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

PowerShell でリモートアクセスをできるようにします。
管理者ユーザでPowerShellを起動し、以下コマンドを実行して、「信頼されたホストの一覧」に追加します。

::

   Set-Item wsman:\localhost\Client\TrustedHosts -Value * -Force

パッケージインストール
----------------------

以下のソフトウェアをインストールします。

* Java関連
    * JDK1.8
    * Gradle(ビルドツール)
* Git 関連
    * git.install(Git)
    * TortoiseGit(Git GUIクライアント)
    * WinSCP(SCPクライアント)
* UTF-8対応したユーティリティ
    * notepad++(テキストエディタ)
    * 7-zip(zipアーカイバ)
* Unix 関連
    * UnxUtils(Unix コマンドユーティリティ)
* VMware 関連
    * VMware vSphere Client
* その他
    * Google Chrome(Webブラウザ確認用)


本手順書では、Windows 版パッケージ管理ツール `Chocolatey`_ を用いて、各種ソフトウェアをインストールします。


.. _Chocolatey: https://chocolatey.org/


PowerShell コンソールから、 Chocolatey をインストールします。
事前に以下コマンドでChocolatey インストールスクリプトの実行許可設定をします。

::

   Set-ExecutionPolicy RemoteSigned

.. note::

   RemoteSignedは、 署名付きのスクリプト、及びローカルに保存されているスクリプトを実行可能にします。


以下コマンドを実行して、 Chocolatey をインストールします。

::

   iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))

続けて、以下 Chocolatey コマンドで各種ソフトウェアをインストールします。

::

   choco install -y unxutils winscp 7zip notepadplusplus.install jdk8 gradle TortoiseGit git.install GoogleChrome vmwarevsphereclient

Office 製品がない場合は、以下コマンドで、Libre Office をインストールします。

::

   choco install -y libreoffice-oldstable

PowerCLIインストール
---------------------

VMware PowerCLI のインストールは Chocolatey がまだ未サポートのため、手動でインストールします。

VMWareサイトから PowerCLI モジュールをダウンロードしてインストールします。
バージョンは PowerCLI 6.x を選びます。

::

   https://www.vmware.com/support/developer/PowerCLI/

.. note::

   ダウンロードには VMWare アカウントが必要となり、未登録の場合はサインアップしてください。

ダウンロードした VMWare-PowerCLI-\*.exe を起動して、既定の設定でインストールします。

一旦、ここでOSを再起動します。

gradle-server-acceptanceインストール
------------------------------------

ダウンロードサイトからバイナリモジュール gradle-server-acceptance-0.1.7.zip
をダウンロードして、c:\\ の直下にコピーします。

.. note::

   バイナリモジュール の作成手順については、 :doc:`02_DevelopmentOption` を参照してください。

エクスプローラを起動して、ダウンロードしたファイルを選択し、
右クリックで 7-zip メニューを開いて「ここに展開」を選択します。

c:\server-acceptance ディレクトリが作成されます。

実行パス環境変数の設定
----------------------

実行パス環境変数に本ディレクトリを追加します。

コントロールパネルを開いて、「システム」、「システムの詳細設定」を選択します。
「環境変数」をクリックします。

システムの環境変数のリストから、Path を選択して、「編集」をクリックします。
値の最後に ;c:\server-acceptance を追加して、パスを追加します。

設定を反映するため、PowerShell　を一旦閉じて、再度、起動します。
PowerShell コンソールから、 getconfig -h コマンドを実行して、
以下ヘルプメッセージが出力されることを確認します。


::

   getconfig -h
   usage: getconfig -c ./config/config.groovy
    -c,--config <config.groovy>             Config file path
    -d,--dry-run                            Enable Dry run test
       --decode <config.groovy-encrypted>   Decode config file
       --encode <config.groovy>             Encode config file
       --excel <check_sheet.xlsx>           Excel sheet path
    -g,--generate </work/project>           Generate project directory
    -h,--help                               Print usage
    -i,--input <test_servers.groovy>        Target server config script
    -k,--keyword <password>                 Config file password
       --parallel <arg>                     Degree of test runner processes
    -r,--resource <arg>                     Dry run test resource directory
    -s,--server <svr1,svr2,...>             Filtering list of servers
    -t,--test <vm,cpu,...>                  Filtering list of test_ids
    -u,--update <local|db|db-all>           Update node config
       --verify                             Disable verify test
    -x,--xport </work/project.zip>          Export project zip file


動作確認
--------

試しにLinuxまたはWindowsサーバを検査対象として、検査シナリオの動作確認をします。

検査プロジェクトの作成
~~~~~~~~~~~~~~~~~~~~~~

新規にプロジェクトを作成して、サーバの検査シナリオを手動実行します。
はじめに作業用ディレクトリを作成します。
ここでは、c:\\Users\\Public\\workを作業ディレクトリとします。

::

   cd c:\Users\Public\work

指定したディレクトリにプロジェクトを作成します。
-g {ディレクトリ} オプションを指定して、getconfig を実行します。

::

   getconfig -g .\test-project1

作成されたディレクトリがプロジェクトディレクトリとなり、本ディレクトリ下で検査を行います。


チェックシート.xlsx 編集
~~~~~~~~~~~~~~~~~~~~~~~~

プロジェクトディレクトリ下の"チェックシート.xlsx"を開き、
シート「チェック対象」を開いて検査対象サーバの接続情報を記入します。

   入力列の1列目に自PCの接続情報を記入します。

   * **server_name** : サーバ名
   * **ip** : IPアドレス
   * **platform** : Windowsサーバの場合は'Windows'、Linuxの場合は'Linux'
   * **os_account_id** : デフォルト値のまま'Test'を指定

設定ファイル config/config.groovy 編集
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

プロジェクトディレクトリ下の "config\\config.groovy" を Notepad++ で開きます。

.. note::

   メモ帳で開くと文字化けが発生します。
   Notepad++ など UTF-8 対応のテキストエディタを使用してください。

Windowsサーバの場合、「Windows接続情報」の箇所にサーバの接続情報を記入します。

   * **account.Windows.Test.user** : Windowsログオン名
   * **account.Windows.Test.password** : パスワード

Linuxの場合、「Linux接続情報」の箇所にサーバの接続情報を記入します。

   * **account.Linux.Test.user** : Linuxユーザ名
   * **account.Linux.Test.password** : パスワード

getconfig検査コマンド実行と確認
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

PowerShell コンソールからプロジェクトディレクトリに移動し、getconfigコマンドを実行します。

::

   cd c:\Users\Public\work\test-project1
   getconfig

実行が完了すると、 **build** の下に検査結果 **チェックシート_{日時}.xlsx** が生成されます。Excel で開いて、結果を確認します。
シート「ゲストOSチェックシート(Windows)」または、「ゲストOSチェックシート(Linux)」を選択し、
検査対象サーバー名の列に値が登録されていれば、検査は成功です。
また、シート「検査ルール」よりも右側のシートにデバイス付き検査項目の結果が登録されれいることを確認します。

.. note::

   Windows 環境により、PowerShell のリモートアクセス許可が有効化されていない場合があります。
   その場合、PowerShellを管理者権限で実行して、PowerShell コンソールから以下の有効化設定をします。

   ::

      Enable-PSRemoting

   .. note:: Windows Server 2012 以上の規定値は有効化です。

