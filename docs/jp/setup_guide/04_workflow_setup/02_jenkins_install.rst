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

初期パスワードを聞かれるので、notepad++ で表示されたパスのファイルを開いて、パスワードを入力します。

「Insutall suggested plugins」 を選択します。

以下を入力して、管理者ユーザを登録します。

* ユーザ名 : admin
* パスワード : <適切なパスワードを入力>
* 名前 : 管理者
* メールアドレス : <管理者用メールアドレスを入力>

「 Save and Finish」 を選択して完了します。


