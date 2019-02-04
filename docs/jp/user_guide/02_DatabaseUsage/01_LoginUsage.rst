ログイン手順
============

Webブラウザ経由で以下のサーバにアクセスします。

* 構成管理データベース

   - 構成管理、チケット管理データベース
   - CentOS 6.8 環境のサーバで、 Redmine を使用します。
   - IP : {DBサーバのIP}
   - Redmine URL: http://{DBサーバのIP}:8080/redmine/
   - Redmine ログイン情報

      + ログインID は、ローマ字入力で「{苗字}-{名前の頭文字}」の形式で入力してください。
        例えば、古澤 実の場合、furusawa-m とします
      + 初期パスワードは P@ssw0rd でログインしてください。
      + ログイン後、パスワードを変更してください。

* ワークフローサーバ

   - データクレンジング処理のジョブ管理用サーバ
   - Windows server 2012R2 環境のサーバで、ジョブ管理に Jenkins を使用します。
   - IP : {DBサーバのIP}
   - リモートデスクトップ接続情報 : administrator/{管理者パスワード}
   - Jenkins URL: http://{WFサーバのIP}:8080/
   - Jenkins ログイン情報

* 変更管理データベース

   - 収集したインベントリの変更管理用サーバ
   - CentOS 6.8 環境のサーバで、 GibBucket を使用します。
   - IP : {DBサーバのIP}
   - GitBucket URL: http://{DBサーバのIP}/
   - GitBucket ログイン情報 : root/{管理者パスワード}

