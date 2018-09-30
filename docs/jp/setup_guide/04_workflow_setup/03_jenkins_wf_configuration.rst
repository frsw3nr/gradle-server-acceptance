Jenkinsワークフロー設定
=======================

Gitbucket セットアップ
-----------------------

1. Git グループを作成
2. 作成した Git グループ化に Git プロジェクト作成
3. 

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
==================

まず、GitBucket の URL を Jenkins に登録します。
Jenkins には見かけ上、GitHub Enterprise のように振舞う
（API が同じ）ので、
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

getconfig グループを作成
test2 プロジェクト作成
   プライベート
   getconfig グループ配下に
クライアントからプッシュする場合は、プロキシーを外す
   ~/.gitconfig
   [http "http://testgit003/"]
       proxy =

Jenkins セットアップ
======================

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

 
