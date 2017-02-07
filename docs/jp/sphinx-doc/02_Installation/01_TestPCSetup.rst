検査PCセットアップ
==================

事前準備
--------

ネットワークプロキシーの設定
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

InternetExploler を開いて、「インターネットオプション設定」を選択します。
「接続」、「LAN設定」の順に選択します。

プロキシーサーバの欄に社内プロキシーのアドレス、ポート番号を入力します。

「詳細設定」を選択し、「プロキシーの設定除外」の欄に、
検査対象の vCenter のアドレスを追加します。
その他、社内のパッケージリポジトリサーバなどのアドレスを必要に応じて
追加します。

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

新規にプロジェクトを作成して、サーバの検査シナリオを手動実行します。
はじめに作業用ディレクトリを作成します。

::

   mkdir c:\work

指定したディレクトリにプロジェクトを作成します。

::

   getconfig -g c:\work\test-project1

プロジェクトディレクトリに移動し、サーバチェックシート.xls を開きます。
シート「チェック対象」を編集します。

Windows, Linuxの箇所を編集します。

notepad++で、config\config.groovy を開きます。

チェック対象サーバの接続情報を編集します。

検査コマンドを実行します。

::

   .\getconfig

