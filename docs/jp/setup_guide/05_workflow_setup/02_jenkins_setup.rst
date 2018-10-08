Jenkins Git 連携設定
====================

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
   * 「Group name」の欄に "getconfig" と入力します。
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

   Jenkins は見かけ上、GitHub Enterprise のように振舞う（API が同じ）ので、
   GitHub Enterprise を登録する場合と同様の設定をしてください。

「GitHub」と「GitHub Enterprise Servers」の2つの設定セクションに登録します。

* 「GitHub」 設定セクションから「Add GitHub Server」をクリック

   - 「Name」 に "構成管理DBのホスト名" を入力
   - 「API URL」 に "http://{構成管理DBのIP}/api/v3/" を入力
   - 「Credentials」 はなしを選択

* 「GitHub Enterprise Servers」設定セクションから「追加」をクリック

   - 「API endpoint」に "http://{構成管理DBのIP}/api/v3" を入力
   - 「Name」に "構成管理DBのホスト名" を入力

   .. note::

      「POST is required 」のエラーが発生しますが、無視してかまいません
   
.. note::

   プロキシーを設定している場合は、上記で設定したサーバIPをプロキシーから除外する設定をします。

   * 画面左側のメニューから「Jenkinsの管理」を選択します。
   * 画面中央のメニューから「プラグインの管理」を選択します。
   * 「高度な設定」タブでプロキシーを設定。構成管理DB、自ホストを除外設定。

