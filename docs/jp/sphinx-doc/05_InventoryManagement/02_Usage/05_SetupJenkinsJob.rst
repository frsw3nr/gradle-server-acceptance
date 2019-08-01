Jenkins ジョブ設定/動作確認
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Jenkinsジョブ登録
~~~~~~~~~~~~~~~~~

Jenkins ジョブの設定をします。
Jenkins 管理者ユーザでJenkinsにログインしてください。

::

   http://jenins:8080/

メニューから「新規ジョブ」を選択してください。

   .. figure:: image/08_jenkins1.png
      :align: center
      :alt: Jenkins config 1

ジョブ登録画面から以下の設定をします。

   .. figure:: image/08_jenkins2.png
      :align: center
      :alt: Jenkins config 2
      :width: 640px

* ジョブ名を記入してください。ここでは、「TestZabbixConfig」とします。
* 「pipline」テンプレートを選択してください。
* 画面下側の「OK」をクリックして設定を完了してください。

ジョブ設定画面から順にパラメータ設定をします。
はじめに「ビルドパラメータの追加」をチェックして、以下のパラメータを追加します。

   .. figure:: image/08_jenkins3.png
      :align: center
      :alt: Jenkins config 2

* タイプに「文字列」を選択してください。
* 名前に「GetConfigSCM」を入力してください。
* デフォルト値に、GitBucketに登録したリモートリポジトリのURLに、
  ユーザ名、パスワードを追加したURLを入力してください。
* 画面右下に表示される「保存」をクリックして設定を管理してください。

.. note::

   以下例の様にデフォルト値のURL先頭箇所に、「http://{ユーザ}:{パスワード}@～」
   として、GitBucket のログインユーザ、パスワードを追加してください。

   ::

      http://person-a:Passw0rd@testgit001/gitbucket/git/server-acceptance/test_zabbix_config.git

「パイプライン」をチェックして、以下のパラメータを追加します。

   .. figure:: image/08_jenkins4.png
      :align: center
      :alt: Jenkins config 4

* 「Definition」に、「Pipeline script fom SCM」を選択してください。
* 「SCM」に、「Git」を選択してください。
* 「Repository URL」に、前述のGitBucketリモートリポジトリURLを入力してください。
* 「Script Path」に、「lib/script/GetconfigJob.groovy」を入力してください。
* 画面右下に表示される「保存」をクリックして設定を完了してください。

Jenkinsジョブ実行
~~~~~~~~~~~~~~~~~

Jenkinsのポータル画面から、作成した 「TestZabbixConfig」 ジョブを選択してください。
メニューから「パラメータビルド」を選択してください。

   .. figure:: image/08_jenkins5.png
      :align: center
      :alt: Jenkins config 5
      :width: 640px

.. note::

   初回に実行すると、 "jenkinsci.plugins.scriptsecurity" という
   セキュリティ例外エラーが発生します。
   :doc:`../../01_Setup/06_ManagementServer/02_JobManagement/01_GetconfigDeploy`
   の「動作確認」を参考にセキュリティ例外の除外設定をしてください。

パラメータは既定値のまま変更せずに、「ビルド」をクリックしてください。
しばらくすると、ジョブ実行ステータスの画面が表示されます。

   .. figure:: image/08_jenkins6.png
      :align: center
      :alt: Jenkins config 6

画面右下にある、ジョブのプログレスバーをクリックし、コンソールログ表示
画面に移動してください。

コンソールログ最下部にある、「Input request」をクリックしてください。
以下の実行オプション入力画面のフィールドを指定して、「検査する」
をクリックしてください。

   .. figure:: image/08_jenkins8.png
      :align: center
      :alt: Jenkins config 8

* targetBranch

   「master」を選択してください

* -cオプション

   「config_zabbix.groovy」を選択してください

* -s,-tオプション

   検査対象リストとテストIDリストの指定オプションとなります。
   未記入のまま、指定なしにしてください。

プロジェクトの実行が順に進み、最後に「Finished: SUCCESS」が出力されます。

::

   [Pipeline] End of Pipeline
   Finished: SUCCESS

画面左上にある「Back to Project」をクリックしてください。

   .. figure:: image/08_jenkins11.png
      :align: center
      :alt: Jenkins config 11

ジョブ実行結果サマリが表示されます。

Jenkinsジョブ実行結果の確認
~~~~~~~~~~~~~~~~~~~~~~~~~~~

GitBucket から実行結果を確認します。
test_zabbix_config プロジェクトを選択してください。

   .. figure:: image/08_jenkins9.png
      :align: center
      :alt: Jenkins config 9

プロジェクトのブラウザ画面で、コメントに「Jenkinsjob=...」と記載された
箇所が前回の実行結果から変更があった個所になります。

* コメントは、「Jenkins job={Jenkinsジョブ名}[{ジョブID}]」の形式で設定します。

「Jenkinsjob=...」のリンクをクリックしてください。
今回のジョブの実行で変更があった個所のリストを出力します。

   .. figure:: image/08_jenkins10.png
      :align: center
      :alt: Jenkins config 10

この例では、Zabbixのトリガーに変更があった個所をリスト表示しています。

JenkinsジョブにRedmineチケット抽出処理を追加
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

作業PC から、Git Bash を開きます。
作業ディレクトリを~/work とし、その下にインベントリ収集プロジェクトを
GitBucketから複製します。~/work が存在しない場合は作成します。

::

   mkdir ~/work

~/work に移動して、前述で作成したGitリポジトリを複製します。

::

   cd ~/work
   git clone http://alpaca1/gitbucket/git/root/test_zabbix_config.git

Jenkins ジョブスクリプトを編集します。

::

   sakura lib/script/GetconfigJob.groovy

52行目付近の「bat "getconfig ${getconfig_opt}"」の行の上に以下の行を追加します。
本コマンドで、事前にRedmineチケットから検査対象設備を抽出して検査仕様を作成します。

::

  bat "getconfig ${getconfig_opt} -r --silent"  // 追加
  bat "getconfig ${getconfig_opt}"

編集後、エディタを閉じて、Git Bash コンソールから
git status コマンドで変更内容を確認します。

::

   git status

スクリプトが変更されていることを確認します。

::

   modified:   lib/script/GetconfigJob.groovy

コメントに"Add --use-redmine getconfig command"を追加して変更をコミットします。

::

   git commmit -a -m "Add --use-redmine getconfig command"

git push で GitBucket に変更内容をプッシュします。

::

   git push

GitBucket への Git リポジトリ変更反映後、Jenkinsジョブを再実行してください。
コンソール出力に追加したコマンドが実行されていることを確認します。

::

   getconfig を実行します...
   [Pipeline] bat
   [TestZabbixConfig] Running batch script

   C:\Program Files (x86)\Jenkins\workspace\TestZabbixConfig>getconfig -c config/config_zabbix.groovy -r --silent 
   09:32:49 INFO  j.c.t.I.a.TestRunner - Parse Arguments : [-c, config/config_zabbix.groovy, -r, --silent]
   09:32:49 INFO  j.c.t.I.a.TestRunner -  home          : C:\Program Files (x86)\Jenkins\workspace\TestZabbixConfig
