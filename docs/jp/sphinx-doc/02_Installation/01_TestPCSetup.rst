検査PCセットアップ
==================

スタンドアローン構成で、検査用 Windows PC環境をセットアップします。

事前準備
--------

システム要件
~~~~~~~~~~~~

Windows 7 64bit、Windows 2012以上のPCが必要です。必要スペックは以下の通りです。

* CPU 1 Core以上
* Memory 4 GB以上
* Disk 100 GB以上

ネットワークプロキシーの設定
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

InternetExploler を開いて、「インターネットオプション設定」を選択します。
「接続」、「LAN設定」の順に選択します。

プロキシーサーバの欄に社内プロキシーのアドレス、ポート番号を入力します。

「詳細設定」を選択し、「プロキシーの設定除外」の欄に、
検査対象の vCenter のアドレスを追加します。
その他、社内のパッケージリポジトリサーバなどのアドレスを必要に応じて
追加します。

SSL証明書のインストール(社内Webアクセスの制限がある場合)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

社外ウェブアクセス環境のセキュリティ構成により、SSL証明書のインストールが必要な場合があります。
社内既定に従って、Webブラウザに社内ルート証明書のインストールをします。
SSL証明書がインストールされていない場合、この後のパッケージのインストールで、
「セキュリティ証明書には問題があります」とエラーが発生する場合があります。

PowerShell5.0のインストール(Windows7の場合)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Windows7の場合、必要となるV4.0 以上の
PowerShell が標準インストールされていないため、
手動で PowerShell 5.0をインストールします。
PowerShell5.0は、Windows Management Framework5.0 にバンドルされています。

事前に、Microsoft .NET Framework 4.5のインストールが必要です。
以下のURLよりダウンロードしてインストールしてください。

::

   http://www.microsoft.com/en-us/download/details.aspx?id=30653

WMF5.0を以下のURLからダウンロードして、インストールします。

::

   https://www.microsoft.com/en-us/download/details.aspx?id=50395

PowerShell設定
~~~~~~~~~~~~~~

管理者ユーザでPowerShellを起動し以下コマンドを実行して、
リモート操作する側が相手を「信頼されたホストの一覧」に追加します。

::

   Set-Item wsman:\localhost\Client\TrustedHosts -Value * -Force

Chocolateyインストール
----------------------

Windows 版パッケージ管理ツール Chocolatey を用いて、
各種ソフトウェアをインストールします。


管理者ユーザでPowerShellを起動します。
事前に以下コマンドでChocolatey インストールスクリプトの実行許可設定をします。
RemoteSignedは、 署名付きのスクリプト、及びローカルに保存されているスクリプトを
実行可能にします。

::

   Set-ExecutionPolicy RemoteSigned

以下コマンドを実行して、 Chocolatey をインストールします。

::

   iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))

パッケージインストール
----------------------

Chocolateyコマンドを用いて、各種ソフトウェアをインストールします。
インストール対象は以下の通りです。

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

管理者ユーザでPowerShellを起動し、以下のコマンドを実行します。

::

   choco install -y unxutils winscp 7zip notepadplusplus.install jdk8 gradle TortoiseGit git.install GoogleChrome vmwarevsphereclient

Office 製品がない場合は、以下コマンドで、Libre Office をインストールします。

::

   choco install -y libreoffice-oldstable

PowerCLIインストール
---------------------

VMware PowerCLI は Chocolatey のサポートがまだされていないため、
手動でインストールします。

VMWareサイトから PowerCLI モジュールをダウンロードしてインストールします。
バージョンは PowerCLI 6.x を選びます。

::

   https://www.vmware.com/support/developer/PowerCLI/

VMWare アカウントが必要なので、サインインしてダウンロード
ダウンロードした VMWare-PowerCLI-\*.exe を起動して、既定の設定でインストール

一旦、ここでOSを再起動します。

gradle-servier-acceptanceインストール
-------------------------------------

ダウンロードサイトから gradle-servier-acceptance-0.1.7.zip
をダウンロードして、c:\ の直下にコピーします。
エクスプローラを起動して、c:\ を参照して、ダウンロードした以下ファイルを
選択して、右クリックで7-zipメニューを開いて「ここに展開」を選択します。

c:\server-acceptance ディレクトリが作成されます。

実行パス環境変数に本ディレクトリを追加します。

コントロールパネルを開いて、「システム」、「システムの詳細設定」を選択します。
「環境変数」をクリックします。

リストから、Path を選択して、「編集」をクリックします。
値の先頭に c:\server-acceptance; を追加して、パスを追加します。

以上でインストールは完了です。

動作確認
--------

検査プロジェクトの作成
~~~~~~~~~~~~~~~~~~~~~~

新規にプロジェクトを作成して、サーバの検査シナリオを手動実行します。
はじめに作業用ディレクトリを作成します。

::

   mkdir c:\work

指定したディレクトリにプロジェクトを作成します。
-g {ディレクトリ} オプションを指定して、getconfig を実行します。

::

   getconfig -g c:\work\test-project1

指定したディレクトリ下にベースの検査シナリオひな形が作成されます。
本ディレクトリ下で検査対象、検査シナリオの編集をし、検査を実行します。


チェックシート.xlsx 編集
~~~~~~~~~~~~~~~~~~~~~~~~

プロジェクトディレクトリ下のチェックシート.xlsxを開きます。

1.シート「チェック対象」に検査対象サーバの接続情報を記入します。

.. note:: シート内セルが空欄の箇所は検査を実行せずにスキップします。

* server_name, platform

   上記は必須項目です。
   server_name は、シート内で一意となる検査対象の名称を記入します。
   platform は、検査対象がESXiホストの場合は'VMHost'を、ゲストOSの場合は、'Windows'または'Linux'を記入します。

* ip, os_account_id

   Linux, Windows サーバなど直接サーバに接続して検査をする場合に使用します。

   ip は検査対象サーバのアドレスを指定してください。
   os_account_id は、config.groovy 設定ファイル内に記入した接続アカウントIDを記入します。
   account.Linux、 account.Windows から始まるパラメータ名のアカウント情報を指定します。

* remote_account_id, remote_alias

   vCenter などサーバ経由でリモートで検査をする場合に使用します。

   remote_account_id は、 remote_alias は、vCenter などリモート側のサーバ名定義(エイリアス)を記入します。

* verify_id

   検査ルールを使用する場合にルールIDを記入します。不要な場合は未記入。

* 設定項目のカスタマイズ

   verify_id 以降の行はカスタマイズ用の設定項目となります。
   検査スクリプト内で設定値を参照します。
   詳細は開発者ガイドを参照してください。

設定ファイル config/config.groovy 編集
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

config/config.groovy 内のサーバ接続情報の箇所を編集します。

.. note:: メモ帳で開くと文字化けが発生します。Notepad++ など UTF-8 対応のテキストエディタを使用してください。

* リモート検査の接続情報

::

   // vCenter接続情報
   account.Remote.Test.server   = '192.168.10.100'
   account.Remote.Test.user     = 'root'
   account.Remote.Test.password = 'XXXX'


* ローカル検査の接続情報

::

   // Linux 接続情報
   account.Linux.Test.user      = 'someuser'
   account.Linux.Test.password  = 'XXXX'
   account.Linux.Test.work_dir  = '/tmp/gradle_test'

getconfig検査コマンド実行
~~~~~~~~~~~~~~~~~~~~~~~~~

server-acceptanceディレクトリに移動してgetconfigコマンドを実行します

::

   getconfig

実行結果の確認
~~~~~~~~~~~~~~

getconfigを実行すると **build** の下に検査結果が出力されます。

* チェックシート_{日時}.xlsx

   各プラットフォームの検査シートに検査対象サーバの検査結果を記録します。
   各種デバイス情報を新規シートに記録します。

* log ディレクトリ

   'log' ディレクトリの下に、
   **'{プラットフォーム}/{検査対象サーバ}/{検査シナリオ}/{検査ID}'**のファイルパス形式で、
   検査結果をログ出力します。

