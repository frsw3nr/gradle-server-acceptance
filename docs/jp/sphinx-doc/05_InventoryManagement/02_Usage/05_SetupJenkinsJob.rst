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
* デフォルト値に、GitBucketに登録したリモートリポジトリのURLを入力してください。
* 画面右下に表示される「保存」をクリックして設定を管理してください。

.. note::

   デフォルト値のURLは以下例の様に先頭の箇所に、http://{ユーザ}:{パスワード}@～
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

パラメータは既定値のまま変更せずに、「ビルド」をクリックしてください。
しばらくすると、ジョブ実行ステータスの画面が表示されます。

   .. figure:: image/08_jenkins6.png
      :align: center
      :alt: Jenkins config 6

画面右下にある、ジョブのプログレスバーをクリックし、コンソールログ表示
画面に移動してください。

コンソールログ最下部にある、「Input request」をクリックしてください。
以下の実行オプション入力画面のフィールドはそのままにして、「検査する」
をクリックしてください。

   .. figure:: image/08_jenkins8.png
      :align: center
      :alt: Jenkins config 8

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

この例では、Zabbixのトリガーに変更があった個所のリストの表示となります。

