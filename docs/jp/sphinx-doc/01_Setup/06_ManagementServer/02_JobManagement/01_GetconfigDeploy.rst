getconfig サーバ構成情報収集モジュールのデプロイ
================================================

Gitbucketのgetconfigリポジトリ登録
----------------------------------

Gitbucketにgetconfigソースを登録します。

Gitbucket "http://{Gitbucketサーバ}/gitbucket" に接続し、登録した管理ユーザでログインします。

画面右上の "+" アイコンを選択して、メニュー「new repository」を選択します。
以下の情報を入力します。

* Repository name : "gradle-server-acceptance" を入力
* Description : "サーバ構成収集ツール" を入力
* Private を選択

「Create repository」を選択して、リポジトリ作成を完了します。
リポジトリ作成後、「Push an existing repository from the command line」
の後に表示された以下例のメッセージをメモします。

::

   git remote add origin http://{サーバアドレス}/gitbucket/git/xxxxxx/gradle-server-acceptance.git
   git push -u origin master

サーバ構成情報収集モジュールソースのGit登録
-------------------------------------------

GitBucket にサーバ構成情報収集ツール getconfig のソースを登録します。

Jenkins サーバにadministratorユーザでリモートデスクトップ接続します。
:doc:`../../../02_StandaloneTest/01_Setup/02_DevelopmentOption` の手順に従い、サーバ構成収集ツールのソースプロジェクトを作成します。

ここでは、以下ディレクトリに保存した、getconfig ソースをGitBucket に登録します。

::

   cd C:\Users\Public\work
   git clone https://github.com/frsw3nr/gradle-server-acceptance.git

作成したプロジェクトを GitBucket リポジトリに登録します。

プロジェクトディレクトリに移動します。

::

   cd C:\Users\Public\work\gradle-server-acceptance

既定の GitHub リモートレポジトリURLの設定を削除します。

::

   git remote rm origin

前述でメモした、GitBucket 用のリモートレポジトリURLを登録します。

::

   git remote add origin http://{サーバアドレス}/gitbucket/git/xxxxxx/gradle-server-acceptance.git
   git push -u origin master

「Git Credintial Manager for Windows」のポップアップ画面が表示されたら、
GitBucketの管理ユーザ、パスワードを入力して"OK"を選択します。


Jenkinsジョブ登録
-----------------

Jenkinsにパイプラインスクリプト登録します。
Jenkins 管理コンソール "http://{Jenkinsサーバ}:8080/"に接続し、管理ユーザでログインします。
「新規ジョブ作成」を選択して、以下プロパティを入力します。

* Enter an item name : "DeployGetconfig" を入力
* Pipeline を選択して "OK" を選択

パイプラインの設定画面が表示されたら、以下のプロパティを入力します。

* Pipeline名
   * 入力した "DeployGetconfig" のまま
* Pipeline設定
   * Definition を "Pipeline script from SCM" に変更
   * SCM を"Git" に変更
   * Repository URLにGitBucket のプロジェクトリポジトリURLを入力

      ::

         http://{サーバ}/gitbucket/git/{ユーザID}/gradle-server-acceptance.git

   * 認証情報の追加を選択して、GitBucket管理ユーザのユーザー名、パスワードを入力
   * Script Pathに以下デプロイ用スクリプトパスを入力
      * lib/script/DeployBaseModuleJob.groovy

次にビルドパラメータの設定をします。設定画面冗談の「ビルドのパラメータ化」をチェックします。

* 名前
   * "GetConfigBaseSCM" を入力します
* デフォルト値
   * 以下の形式で GitBucketのリポジトリURLを入力します

   ::

      http://{ユーザID}:{パスワード}@{サーバ}/gitbucket/git/{ユーザID}/gradle-server-acceptance.git

   .. note:: GitBucketリポジトリURLにユーザID、パスワードの認証情報を追加した形式でURLを登録します。

「保存」を選択してジョブ登録を完了します。

動作確認
--------

以下の手順でジョブを実行します。

* Jenkinsジョブのメニューから「パラメータ付きビルド」を選択します。
* パラメータ GetConfigBaseSCM の値は既定値のままにして、「ビルド」を選択します。
* ビルド履歴に表示された、実行中のジョブのプログレスバーを選択して、コンソール画面を表示します。
* コンソール出力最下行の、"Input requested"を選択します。
   * テストを実行する場合は、"testOption" をチェックしてください。
   * その他のオプションは既定値のままにします。
* 「デプロイする」を選択します。

実行すると、以下のセキュリティエラー "jenkinsci.plugins.scriptsecurity" が発生します。

::

   org.jenkinsci.plugins.scriptsecurity.sandbox.RejectedAccessException:
      Scripts not permitted to use staticMethod java.lang.System getProperties

上記は、Jenkins パイプラインスクリプトのセキュリティ上の制約で、以下のスクリプト承認の
設定が必要となります。

* 画面左上のメニューから Jenkins->Jenkinsの管理->In-process script approvals を選択
* "Approve" ボタン選択

本設定は一度にできないため、再度、「パラメータ付きビルド」を実行して、
セキュリティエラーを表示させてから、上記の許可設定を繰り返し実行します。
最終的に以下のリストが除外設定リストになります。

::

   method java.util.Dictionary put java.lang.Object java.lang.Object
   staticMethod java.lang.System getProperties
   staticMethod org.codehaus.groovy.runtime.DefaultGroovyMethods stripMargin java.lang.String
