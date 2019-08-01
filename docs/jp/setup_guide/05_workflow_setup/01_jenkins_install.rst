Jenkins インストール
====================

Jenkinsインストール、セットアップ
---------------------------------

Chocolatey で Jenkins をインストールします。

::

   choco install -y jenkins

Chrome から Jenkins サイトに接続します。

::

   http://サーバ:8080/

初期パスワードを聞かれるので、notepad で表示されたパスのファイルを開いて、パスワードを入力します。

「Insutall suggested plugins」 を選択します。

.. note::

   イントラネット環境で「this jenkins instance appears to be offline」という
   メッセージが出力され、プロキシー設定の更新をしても同様のメッセージで
   先に進まない場合があります。
   その場合、以下の記事の通り、 hudson.model.UpdateCenter.xml を編集し、
   デフォルトの "https://xxx" を "http://xxx" に変更して Jenkins を再起動する
   ことで回避できました。

   https://stackoverflow.com/questions/42408703/

以下を入力して、管理者ユーザを登録します。

* ユーザ名 : admin
* パスワード : <適切なパスワードを入力>
* 名前 : 管理者
* メールアドレス : <管理者用メールアドレスを入力>

「 Save and Finish」 を選択して完了します。

.. .. note::

.. Jenkins バージョンによって、プラグイン「Pipeline: Basic Steps」がインストール
.. されていない場合がある

.. * メニューから、「Jenkinsの管理」を選択します。
.. * 「プラグインの管理」を選択します。
.. * 「利用可能」タブを選択し、「フィルター」入力に Pipeline: Basic Stepsを入力します。
.. * リスト内の「Pipeline: Basic Steps」をチェック して、
..   「ダウンロードして再起動後にインストール」をクリックします。



