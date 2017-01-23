Jenkinsジョブの作成
===================

事前準備
--------

GitBucket サイトを作成し、以下のGitリポジトリを事前に作成します

* Getconfigベースモジュールソースリポジトリ
   デフォルト値： http://testgit001/gitbucket/git/server-acceptance/gradle-server-acceptance.git
* 

Getconfigベースモジュールのデプロイジョブ
-----------------------------------------

ベースモジュールデプロイジョブの作成
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Jenkinsサーバに接続し、以下の手順でジョブを作成します。

2.メニューから「新規ジョブ作成」選択

   Enter an item nameに「DeployGetConfigBase」を入力し、「Pipeline」を選んで「OK」クリック

2.「General」タブを選択

3.「ビルドのパラメータ化」をチェックして以下パラメータを設定

   名前： GetConfigBaseSCM
   デフォルト値： http://testgit001/gitbucket/git/server-acceptance/gradle-server-acceptance.git

4.「Pipeline」タブを選択して、以下を設定

   Definition : 「Pipeline script from SCM」を選択
   SCM : 「Git」 を選択
   Repositories : http://testgit001/gitbucket/git/server-acceptance/gradle-server-acceptance.git
   (GetConfigBaseSCM パラメータと同じ)
   Script Path : lib/script/DeployBaseModuleJob.groovy

5. 「保存」をクリックして保存

ベースモジュールデプロイジョブ実行
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

作成したジョブを実行します。

1. Jenkins ダッシュボードから、「DeployGetConfigBase」ジョブを選択
2. メニューから「パラメータ付きビルド」を選択
3. デフォルトの入力値のままで「ビルド」をクリック
4. ビルド履歴から実行中のジョブのプログレスバーをクリックし、コンソールログを出力
5. ログメッセージから、「Input requested」のURLをクリックして、環境設定を入力

   targetBranch : master
   testOption : チェックしない
   targetDirectory : c:\

6. 入力が終わったら「デプロイする」でデプロイ実行

