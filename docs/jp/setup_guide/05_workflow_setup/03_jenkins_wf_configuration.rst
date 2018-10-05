Jenkinsワークフロー設定
=======================

構成概要
--------

* 構成管理DB の GitBucket に Git グループを追加し、WebHook の設定をします。 
* WebHook で Jenkins ワークフローサーバと連携する設定をします。
* データベース登録ワークフロー用のプロジェクトを作成します。
* Jenkins にワークフローを登録します。


Gitbucket セットアップ
-----------------------

1. ログイン

   Web ブラウザから Git 管理コンソールを開きます。

   ::

      http://{構成管理DBのIP}/

   画面右上の「Sign in」をクリックして root ユーザでログインします。

2. グループの作成

   * 画面右上の「+」をクリックしてメニュー「New Group」を選択します。
   * 「Group name」の欄に getconfig と入力します。
   * 「Create group」をクリックします。

3. WebHook の設定

   getconfig グループに対して Group レベルの WebHook を設定します。

   * 以下の URL にアクセスして、作成した getconfig グループの管理画面に移動します。

      ::

         http://{構成管理DBのIP}/getconfig 

   * 「Edig group」ボタンをクリックします。
   * 「ServiceHooks」 タブを選択します。
   * 「Add webhook」 ボタンを押して、以下の設定をします。

      - Payload URL に以下の、Jenkins サーバのURLを設定します。
        最後の/を忘れないようにしてください。

      ::

         http://{ワークフローサーバのIP}:8080/github-webhook/

      - 「Test Hook」をクリックして、疎通確認をします。200番のコードが返ってくればOKです。
      - 「Which events would you like to trigger this webhook?」 の下の
         チェックボックスに Pull Request と Push の2つにチェックを入れます。
      - 「Add webhook」をクリックして登録します。

Jenkins に Git サーバと連携する設定
-----------------------------------

Webブラウザから、Jenkins 管理画面を開きます。

::

   http://jenkins1:8080

ユーザは admin で、前節で指定したパスワードでログインします。

GitBucket の URL を Jenkins に登録します。

* 画面左側のメニューから「Jenkinsの管理」を選択します。
* 画面中央のメニューから「システム設定」を選択します。

.. note::

   Jenkins には見かけ上、GitHub Enterprise のように振舞う（API が同じ）ので、
   GitHub Enterprise を登録するイメージで設定してください。

「GitHub」と「GitHub Enterprise Servers」の2つの設定セクションに登録します。

* 「GitHub」 設定セクションから「Add GitHub Server」をクリック

   - 「Name」 に 構成管理DBのホスト名 を入力
   - 「API URL」 に http://{構成管理DBのIP}/api/v3/ を入力
   - 「Credentials」 はなしを選択

* 「GitHub Enterprise Servers」設定セクションから「追加」をクリック

   - 「API endpoint」に http://{構成管理DBのIP}/api/v3 を入力
   - 「Name」に 構成管理DBのホスト名 を入力

   .. note::

      「POST is required 」のエラーが発生しますが、無視してかまいません
   
.. note::

   プロキシーを設定している場合は、上記で設定したサーバIPをプロキシーから除外する設定をします。

   * 画面左側のメニューから「Jenkinsの管理」を選択します。
   * 画面中央のメニューから「プラグインの管理」を選択します。
   * 「高度な設定」タブでプロキシーを設定。以下を除外設定。

   ::

      jenkins1
      redmine1

データベース登録用 Git プロジェクト作成
---------------------------------------

Git 管理コンソール画面に戻ります。

::

   http://{構成管理DBのIP}/

画面右上の「+」をクリックしてメニュー「New Repository」を選択します。

* 「Owner」 に "getconfig" を選択します。
* 「Repository name」の欄に、 "server_shipping" を入力します。
* 「Description」の欄に、 "機器搬入時の変更管理" を入力します。
* 「Private」を選択します。

「Create repository」をクリックして登録します。
登録後、表示された画面の「Push an existing repository from the command line」の
したの以下の行をコピーします。

::

   git remote add origin http://redmine1/git/getconfig/server_shipping.git
   git push -u origin master
   

データベース登録ワークフロー用プロジェクトの登録
------------------------------------------------

Getconfig ホームからデータベース登録ワークフローをコピーして、Git リポジトリに登録します。

ワークフローサーバにリモートデスクトップ接続して、PowerShellコンソールを開きます。

Desktop ディレクトリに移動します。

::

   cd C:\Users\Administrator\Desktop\

Geconfig ホームのデータベース登録ワークフローディレクトリを server_shipping にコピーします。

::

   Copy-Item -Path C:\server-acceptance\cleansing\ -Destination .\server_shipping -Recurse

ディレクトリに移動し、Jenkinsfile を編集します。 

::

   cd server_shipping
   notepad++.exe .\Jenkinsfile

データベース登録ワークフロー用プロジェクトの設定

Pythonライブラリのインストール
------------------------------

PowerShell コンソールに戻ります。
Pathを通すために、環境変数を更新します。

::

   $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine")


server_shipping ディレクトリに移動して、以下のコマンドで Git ローカルリポジトリの初期化をします。

::

   cd .\server_shipping\
   git init .
   git add .
   git commit -m "first commit"

.. note::

   git commit 子マントでgit ユーザ情報が設定されていない旨のエラーが発生した場合は、
   以下の git コマンドでユーザ情報を登録します。

   ::

      git config --global user.email "root@example.com"
      git config --global user.name "root"

.. note::

   Filename too longエラーの対処。以下コマンドを実行します。

   ::

      git config --system core.longpaths true


以下のコマンドでGitリモートリポジトリにプッシュします。

::

   git remote add origin http://redmine1/git/getconfig/server_shipping.git
   git push -u origin master

ユーザ名は root、パスワードは前節で指定した値を入力して「OK」をクリックします。

Jenkins ジョブの作成
---------------------

画面左上の「Jenkins」をクリックしてダッシュボードに戻ります。
メニュー「新規ジョブ作成」を選択します。

* 「name」に getconfig GitHub Organization を入力します。
* ジョブ種別に「GitHub Organization」を選択して、「OK」をクリックします。
* 「GitHub Organization」設定セクションに移動します

   - 「API endpoint」に上記作成した Web フックを選択します
   - 「Credentials」の「追跡」をクリックします

      + GitBucekt の認証情報を登録します
      + ユーザに root パスワードに設定したパスワードを入力し、それ以外は空欄にして OK を
        クリックします
      + Credentials リストボックスから作成した作成した認証情報を選択します

* 画面下の「保存」をクリックして完了します。

設定したWeb フックを選んで、認証

getconfig グループを作成
test2 プロジェクト作成
   プライベート
   getconfig グループ配下に
クライアントからプッシュする場合は、プロキシーを外す
   ~/.gitconfig
   [http "http://testgit003/"]
       proxy =

Jenkins セットアップ
====================

hosts ファイルに Gitbucket サーバ登録

 testgit003

Jenkins 管理画面からパイプラインセットアップ

Git Bash を開いてGit操作確認

git clone http://testgit003/git/getconfig/test2.git
cd test2

git config --global user.email "minoru.furusawa@toshiba.co.jp"
git config --global user.name "Minoru Furusawa"
git push

Gitbucket Webフック設定

/etc/hosts にjenkins3 を追加

sudo vi /etc/hosts
ping jenkins3

getconfig グループに対して Group レベルの WebHook を設定します。
http://testgit003/getconfig にアクセスして、ServiceHooks タブを選択します。


Add webhook ボタンを押して出てくる画面で、以下の設定をします。

Payload URL に http://jenkins3:8080/github-webhook/ を設定
最後の/を忘れないように。
Content type は application/x-www-form-urlencoded のままで OK です。
Security Token は空白で OK です。
Which events would you like to trigger this webhook? の下の
チェックボックスでは Pull Request と Push の2つにチェックを入れます

Jenkins の設定

まず、GitBucket の URL を Jenkins に登録します。
Jenkins には見かけ上、GitHub Enterprise のように振舞う（API が同じ）ので、
GitHub Enterprise を登録するような気持ちで設定してください。

Jenkins の管理→システムの設定で、「GitHub」と「GitHub Enterprise Servers」の
2箇所に登録します。

API URL (API endpoint) は http://testgit003/api/v3/

Credentials はなしで構いません。

Jenkinsの位置

http://jenkins3:8080/


GitHubサーバ
testgit003
http://testgit003/api/v3/

GitHub Enterprise Servers

http://testgit003/api/v3
Gitbucket on testgit003

"This URL requires POST" jenkins エラーが出るが、無視する

プラグインの高度な設定でプロキシーを設定。以下を除外設定

testgit003
jenkins3

Filename too longエラーの対処。GitBash で以下コマンドを実行する

git config --system core.longpaths true

新規ジョブ作成で名前を getconfig GitHub Organization を選んで OK

設定したWeb フックを選んで、認証


Python をインストールする

choco install python3

Python ライブラリのインストール

cd Desktop\test2\cleansing
pip install .

Redmien セットアップ
===========================

サブプロジェクトによるJenkinsジョブ管理手順整理
----------------------------------------------------

プロジェクト名

server_shipping サーバ出荷時の変更管理プロセス
ip_address_clensing IPアドレス棚卸しプロセス
zabbix_inventory_update Zabbix監視設定インベントリ登録
middleware_inventory_update ミドルウェアインベントリ登録

GItBucket で空のプロジェクト作成、グループは getconfig 下

[psadmin@paas rep_network_hosts]$ 
ls
Changes.txt  DataCleansing  Jenkinsfile  README.md

mkdir server_shipping
cd server_shipping

touch README.md
git init
git add README.md
git commit -m "first commit"
git remote add origin http://gitbucket/git/getconfig/server_shipping.git
git push -u origin master

git submodule add http://gitbucket/git/getconfig/test2.git test2

プロジェクトホーム下の
Jenkins ファイルを編集

 
