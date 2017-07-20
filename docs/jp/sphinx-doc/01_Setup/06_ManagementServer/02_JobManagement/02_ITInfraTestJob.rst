サーバ構成収集ジョブ作成
========================

Jenkinsを用いてサーバ構成収集ジョブを作成します。

* getconfig を用いてサーバ構成収集のプロジェクトを作成します
* プロジェクトをGitBucket リポジトリに登録します
* Jenkins を用いて、作成したプロジェクトをJenkinsから実行するジョブを作成します

サーバ構成収集プロジェクトの作成
--------------------------------

getconfig プロジェクトを作成します。
ここでは、:doc:`../../../02_StandaloneTest/02_Usage/01_OsTemplate` のプロジェクトを
以下のディレクトリ作成したものとします。

::

   cd C:\Users\Administrator\Documents
   getconfig -g job-test1
   cd .\job-test1
   # この後にサーバ構成収集の設定と動作確認をします

構成情報の収集の動作確認ができたら、config.groovy の暗号化を行います。
セキュリティの制約上、アカウントのパスワード情報などを含む config.groovy を
平文で GitBucket リポジトリに登録するすることは好ましくないため、
暗号化して登録をします。

::

   cd C:\Users\Administrator\Documents\\job-test1
   getconfig --encode .\\config\\config.groovy
   Password:
   Confirm:
   INFO  j.c.t.I.a.Config - OK
   Encrypted .\\config\\config.groovy-encrypted

.. note::

   "Password", "Confirm" の箇所はパスワードを入力して下さい。
   あとのJenkinsの動作確認の際に使用します。

GitBucketリポジトリ登録
-----------------------

Gitbucketに前述で作成した getconfig プロジェクトを登録します。

Gitbucket "http://{Gitbucketサーバ}/gitbucket" に接続し、登録した管理ユーザでログインします。

画面右上の "+" アイコンを選択して、メニュー「new repository」を選択します。
以下の情報を入力します。

* Repository name : "job-test1" を入力
* Description : "テスト用OS構成情報収集" を入力
* Private を選択

「Create repository」を選択して、リポジトリ作成を完了します。
リポジトリ作成後、「Push an existing repository from the command line」
の後に表示された以下例のメッセージをメモします。

::

   git remote add origin http://{サーバアドレス}/gitbucket/git/{ユーザID}/job-test1.git
   git push -u origin master

次に、作成したプロジェクトを GitBucket に登録します。

::

   cd C:\Users\Administrator\Documents\job-test1

前述でメモした、GitBucket 用のリモートレポジトリURLを登録します。

::

   git remote add origin http://{サーバアドレス}/gitbucket/git/xxxxxx/job-test1.git
   git push -u origin master

「Git Credintial Manager for Windows」のポップアップ画面が表示されたら、
GitBucketの管理ユーザ、パスワードを入力して"OK"を選択します。

Jenkins ジョブ登録
------------------

Jenkinsにパイプラインスクリプト登録します。
Jenkins 管理コンソール "http://{Jenkinsサーバ}:8080/"に接続し、管理ユーザでログインします。
「新規ジョブ作成」を選択して、以下プロパティを入力します。

* Enter an item name : "job-test1" を入力
* Pipeline を選択して "OK" を選択

パイプラインの設定画面が表示されたら、以下のプロパティを入力します。

* Pipeline名
   * 入力した "job-test1" のまま
* Pipeline設定
   * Definition を "Pipeline script from SCM" に変更
   * SCM を"Git" に変更
   * Repository URLにGitBucket のプロジェクトリポジトリURLを入力

      ::

         http://{サーバ}/gitbucket/git/{ユーザID}/job-test1.git

   * 認証情報の追加を選択して、GitBucket管理ユーザのユーザー名、パスワードを入力
   * Script Pathに以下デプロイ用スクリプトパスを入力
      * lib/script/GetconfigJob.groovy

次にビルドパラメータの設定をします。設定画面冗談の「ビルドのパラメータ化」をチェックします。

* 名前
   * "GetConfigBaseSCM" を入力します
* デフォルト値
   * 以下の形式で GitBucketのリポジトリURLを入力します

   ::

      http://{ユーザID}:{パスワード}@{サーバ}/gitbucket/git/{ユーザID}/job-test1.git

   .. note:: GitBucketリポジトリURLにユーザID、パスワードの認証情報を追加した形式でURLを登録します。

「保存」を選択してジョブ登録を完了します。


動作確認
--------

以下の手順でジョブを実行します。

* Jenkinsジョブのメニューから「パラメータ付きビルド」を選択します。
* パラメータ GetConfigBaseSCM の値は既定値のままにして、「ビルド」を選択します。
* ビルド履歴に表示された、実行中のジョブのプログレスバーを選択して、コンソール画面を表示します。
* コンソール出力最下行の、"Input requested"を選択します。
   * パスワード(-k)の箇所にはconfig.groovy の暗号化で指定したパスワードを入力して下さい
   * その他のオプションは既定値のままにします。
* 「デプロイする」を選択します。

実行すると、前回のgetconfigモジュールのデプロイと同様に、
セキュリティエラー "jenkinsci.plugins.scriptsecurity" が発生します。

上記は、Jenkins パイプラインスクリプトのセキュリティ上の制約で、以下のスクリプト承認の
設定を繰り返し実行してください。

* 画面左上のメニューから Jenkins->Jenkinsの管理->In-process script approvals を選択
* "Approve" ボタン選択

最終的に以下のリストが除外設定リストになります。

* Signatures already approved:

   ::

      method java.io.File getName
      method java.lang.String join java.lang.CharSequence java.lang.CharSequence[]
      method java.util.Dictionary put java.lang.Object java.lang.Object
      method org.jenkinsci.plugins.workflow.support.actions.EnvironmentAction getEnvironment
      new java.io.File java.lang.String
      staticMethod java.lang.System getProperties
      staticMethod org.codehaus.groovy.runtime.DefaultGroovyMethods eachFile java.io.File groovy.lang.Closure
      staticMethod org.codehaus.groovy.runtime.DefaultGroovyMethods stripMargin java.lang.String

* Signatures already approved which may have introduced a security vulnerability

   ::

      new java.io.File java.lang.String
      staticMethod java.lang.System getProperties

リファレンス
------------

* JenkinsでCI環境構築チュートリアル(Windows編) [https://ics.media/entry/2410/2], 2016年, ICSメディア

