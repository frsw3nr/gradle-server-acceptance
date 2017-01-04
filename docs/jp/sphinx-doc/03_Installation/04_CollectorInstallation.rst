採取エージェントのインストール
==============================

Windows Server 2012 R2 セットアップ
===================================

事前準備
--------

Windows Server 2012 R2 環境を構築
リモートデスクトップ接続を許可設定する
http://symfoware.blog68.fc2.com/blog-entry-1010.html
プロキシー設定をしてプロキシー接続できるようにする

Chocolateyインストール
======================

Windows 版パッケージ管理ツール [Chorolatey](https://chocolatey.org)
を用いて、各種ソフトウェアのインストールをする。
Powershell を起動して、Chocolatey をインストール。

PowerShellからスクリプトを実行許可設定。
RemoteSignedは、 署名付きのスクリプト、及びローカルに保存されているスクリプトを
実行可能にする。

::

   Set-ExecutionPolicy RemoteSigned

Chocolatey インストール

::

   iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))


パッケージインストール
----------------------

Chocolateyコマンドを用いて、各種ソフトウェアをインストールする。
インストール対象は以下の通り。

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
    * UnxUtils(Unix コマンドユーティリティ、Jenkins UNIX用APIとの相性をよくするため)
* VMware 関連
    * VMware vSphere Client
* その他
    * Google Chrome(Webブラウザ、Jenkins 確認用)

VMware PowerCLI は Chorolatey がサポートしていないため手動インストールする。

::

   choco install -y unxutils winscp 7zip notepadplusplus.install jdk8 gradle TortoiseGit git.install GoogleChrome vmwarevsphereclient

Office 製品がない場合は、Libre Office を入れる。

::

   choco install -y libreoffice-oldstable

PowerCLIインストール
---------------------

VMWareサイトからダウンロードしてインストール

https://www.vmware.com/support/developer/PowerCLI/

VMWare アカウントが必要なので、サインインしてダウンロード
ダウンロードした VMWare-PowerCLI-\*.exe を起動して、既定設定でインストール

Jenkinsインストール、セットアップ
---------------------------------

Chocolatey でインストール

::

   choco install -y jenkins

Chrome から Jenkis サイトに接続

http://localhost:8080/

初期パスワードを聞かれるので、notepad++ で表示されたパスのファイルを開いて、パスワードを入力

Insutall suggested plugins を選択

管理者ユーザの登録。

ユーザ名・パスワード・名前・メールアドレスを入力して、 Save and Finish を選択。

一旦、ここでOS再起動。

gradle-servier-acceptanceインストール
-------------------------------------

バイナリ版アーカイブをダウンロードして、c:\ の直下にコピー。
エクスプローラを起動して、c:\ を参照して、ダウンロードした以下ファイルを
選択して、右クリックで7-zipメニューを開いて「ここに展開」を選択。

gradle-server-acceptance-0.1.6.zip

c:\server-acceptance ディレクトリが作成される

手動でテスト実行
----------------

PowerShellを起動し以下コマンドを実行して、リモート操作する側が相手を
「信頼されたホストの一覧」に追加する。

::

   Set-Item wsman:\localhost\Client\TrustedHosts -Value * -Force

サーバチェックシート.xls を開いて、シート「チェック対象」を編集する
サンプルは、Windows, Linux, VMHost の3種となるが何れかでよい

config\config.groovy を開いて、チェック対象サーバの接続情報を編集する

::

   cd c:\server-acceptance
   .\getconfig

Jenkinsジョブスケジューラ
-------------------------

Jenkinsの管理

In-process Script Approval

::

   method hudson.model.Job getBuildByNumber int
   method java.io.File getName
   method java.lang.String join java.lang.CharSequence java.lang.CharSequence[]
   method java.util.regex.Matcher matches
   method jenkins.model.Jenkins getItemByFullName java.lang.String
   method org.jenkinsci.plugins.workflow.support.actions.EnvironmentAction getEnvironment
   new java.io.File java.lang.String
   staticMethod jenkins.model.Jenkins getInstance
   staticMethod org.codehaus.groovy.runtime.DefaultGroovyMethods eachFile java.io.File groovy.lang.Closure
   staticMethod org.codehaus.groovy.runtime.DefaultGroovyMethods println groovy.lang.Closure java.lang.Object

新規ジョブ作成

* Pipeline名
   * 検査シナリオ実行
* Definition
   * Pipeline script
   * SCM
      * Git
   * Repository URL
      * http://root:root@192.168.10.1:8090/git/root/test1-job.git
   * Script Path
   * Jenkinsfile.groovy

リファレンス
------------

* https://ics.media/entry/2410/2
* https://wiki.jenkins-ci.org/display/JA/Installing+Jenkins
* https://wiki.jenkins-ci.org/pages/viewpage.action?pageId=36111078

