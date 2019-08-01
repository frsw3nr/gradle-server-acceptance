Git リポジトリ作成/同期
^^^^^^^^^^^^^^^^^^^^^^^

前節で作成したインベントリ収集のプロジェクトを Git リモートリポジトリ
(GitBucket)に登録します。
Git リモートリポジトリに登録することにより、 Jenkins ジョブスケジューラからプロジェクトにアクセス出来る様にします。

GitBucket リポジトリ登録
~~~~~~~~~~~~~~~~~~~~~~~~

Webブラウザから GitBucket サーバに管理者アカウントでログインします。
URL は以下の通りです。

::

   http://{構成管理データベースIP}/gitbucket/

画面右上の「+」メニューから「Create a new repository」を選択します。

   .. figure:: image/07_gitbucket1.png
      :align: center
      :alt: GitBucket 1
      :width: 240px

リポジトリ登録画面から以下のフィールドの入力をして、
「Create repository」を実行してください。

   .. figure:: image/07_gitbucket2.png
      :align: center
      :alt: GitBucket 2
      :width: 640px

* Owner は所属するグループを選択してください。
* Repository name は、前節で作成したプロジェクト名
  「test_zabbix_config」と同一にしてください。
* Privete のタグをチェックし、最後に「Create repository」をクリックしてください。

作成したプロジェクトの同期
~~~~~~~~~~~~~~~~~~~~~~~~~~

上記でGitBucket にリポジトリを登録すると、「Quick setup」
のメッセージが出力されます。
「Quick setup」の下段の既設のリポジトリの同期手順で、
構成収集作業PCで作成したプロジェクトを同期します。
作業PCで「Git Bash」コンソールを起動して、以下Gitコマンドを実行します。

::

   Push an existing repository from the command line
   git remote add origin http://testgit001/gitbucket/git/server-acceptance/test_zabbix_coonfig.git
   git push -u origin master

構成収集作業PCから、スタートメニューを開いてください。
画面右上に表示される、コマンド検索フィールドに 「git」と入力してください。
入力後、表示された選択リストから、「Git Bash」を選択してください。

   .. figure:: image/07_gitbucket3.png
      :align: center
      :alt: GitBucket 3

「Git Bash」のコンソール画面から以下の入力をして、作成したプロジェクトとの
同期を取ります。

   .. figure:: image/07_gitbucket4.png
      :align: center
      :alt: GitBucket 4
      :width: 480px

Getconfig プロジェクトディレクトリに移動します。

::

   cd ~/jobs/test_git_config/

git init コマンドで、Git ローカルリポジトリとして初期化します。

::

   git init .

GitBucketの「Quick setup」で表示された、git remote add origin コマンドを実行します。
前節で作成したGitBucketリモートリポジトリを登録します。

::

   git remote add origin http://testgit001/gitbucket/git/server-acceptance/test_zabbix_coonfig.git

.. note::

   URLの箇所は、GitBucketで表示されたURLに変更してください。

git add コマンドでプロジェクトディレクトリ下ファイルをGit 管理対象として登録します。
git commit , git push コマンドで登録をコミットして、
リモートリポジトリの同期(コピー)をします。

::

   git add .
   git commit -a -m "first init"
   git push -u origin master

以上の操作実行後、作業PC のローカルリポジトリ下のプロジェクトディレクトリと
GitBucket のリモートリポジトリが同期した状態となります。
GitBucket 管理画面からリポジトリを参照すると、同期した管理対象ファイル、ディレクトリ
の一覧が表示されます。

   .. figure:: image/07_gitbucket5.png
      :align: center
      :alt: GitBucket 2
      :width: 640px

