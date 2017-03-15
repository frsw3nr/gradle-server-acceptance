Jenkinsジョブスケジューラ設定
=============================

GitbucketとJenkinsを用いて以下のジョブスケジュール設定を行います。

* Jenkinsサーバにgetconfigモジュールをデプロイする
* Jenkinsサーバにサーバ検査ジョブを登録する

getconfig サーバ構成情報収集モジュールのデプロイ
------------------------------------------------

Gitbucketのgetconfigリポジトリ登録
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Gitbucketにgetconfigソースを登録します。

Gitbucket http://{サーバ}/gitbucket に接続し、登録した管理ユーザでログインます。

画面右上の "+" アイコンを選択して、メニュー「new repository」を選択します。
以下の情報を入力し「Create repository」を選択して、リポジトリを新規作成します。

* Repository name : "gradle-server-acceptance" を入力
* Description : "サーバ構成収集ツール" を入力
* Private を選択

リポジトリ作成後、「Push an existing repository from the command line」
に表示されたメッセージをメモします。

::

   git remote add origin http://{サーバアドレス}/gitbucket/git/furusawa-m/gradle-server-acceptance.git
   git push -u origin master


サーバ構成情報収集モジュールソースのGit登録
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Jenkins サーバにadministratorユーザでリモートデスクトップ接続します。
:doc:`../../../02_StandaloneTest/01_Setup/02_DevelopmentOption` の手順に従い、サーバ構成収集ツールのソースプロジェクトを作成します。

作成したプロジェクトを GitBucket リポジトリに登録します。

プロジェクトディレクトリに移動します。

::

   cd C:\Users\Public\work\gradle-server-acceptance

既定の GitHub リモートレポジトリURLの設定を削除します。

::

   git remote rm origin

前述でメモした、GitBucket 用のリモートレポジトリURLを登録します。

::

   git remote add origin http://{サーバアドレス}/gitbucket/git/furusawa-m/gradle-server-acceptance.git
   git push -u origin master

「Git Credintial Manager for Windows」のポップアップ画面が表示されたら、
GitBucketの管理ユーザ、パスワードを入力して"OK"を選択します。


Jenkinsジョブ登録
^^^^^^^^^^^^^^^^^

Jenkinsにパイプラインスクリプト登録します。
Jenkins 管理コンソール "http://{サーバ}:8080/"に接続し、管理ユーザでログインします。
「新規ジョブ作成」を選択して、以下プロパティを入力します。

* Enter an item name : "DeployGetconfig" を入力
* Pipeline を選択して "OK" を選択

パイプラインの設定画面が表示されたら、以下のプロパティを入力します。

* Pipeline名
   * 入力した "DeployGetconfig" のまま
* Pipeline
   * Definition を "Pipeline script from SCM" に変更
   * SCM を"Git" に変更
   * Repository URLにGitBucket のプロジェクトリポジトリURLを入力
      * http://{サーバ}/gitbucket/git/{ユーザID}/gradle-server-acceptance.git
      * 認証情報の追加を選択して、GitBucket管理ユーザのユーザー名、パスワードを入力
      * http://root:root@192.168.10.1:8090/git/root/test1-job.git
   * Script Pathに以下デプロイ用スクリプトパスを入力
      * lib/script/DeployBaseModuleJob.groovy

動作確認
^^^^^^^^

Jenkins getconfigデプロイジョブ作成
-----------------------------------


   検査ジョブの作成
      getconfigプロジェクト作成
         単体動作確認
      Gitbucketにプロジェクトを登録
      Jenkinsにパイプラインスクリプト登録
      動作確認

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

