Jenkins セットアップ
====================

事前準備
--------

* Windows Server 2012 R2 環境を構築します
* リモートデスクトップ接続を許可設定をします
* プロキシー設定をしてプロキシー接続できるようにします
* 以下の手順で 検査用 PC のセットアップを事前に行います
   * :doc:`../../02_StandaloneTest/01_Setup/01_TestPCSetup`
   * :doc:`../../02_StandaloneTest/01_Setup/02_DevelopmentOption`

.. note::

   リモートデスクトップ設定については、ブログ `Windows Server 2012 リモートデスクトップ接続を許可する`_ を参考にしてください

.. _Windows Server 2012 リモートデスクトップ接続を許可する: http://symfoware.blog68.fc2.com/blog-entry-1010.html

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

一旦、ここでOS再起動します。

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

