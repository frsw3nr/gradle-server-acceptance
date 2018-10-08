Jenkins ジョブの作成
====================

GitHub Organizationジョブの作成
-------------------------------

Jenkins 管理画面に移動し、 Git サーバで作成した「getconfig」グループのジョブ定義をします。

* 画面左上の「Jenkins」をクリックしてダッシュボードに戻ります。
* メニュー「新規ジョブ作成」を選択します。

* 「name」に 「getconfig」 を入力します。
* ジョブ種別に「GitHub Organization」を選択して、「OK」をクリックします。
* 「GitHub Organization」設定セクションに移動します

   - 「API endpoint」に上記で設定した Web フックを選択します
   - 「Credentials」の「追跡」をクリックします

      + GitBucekt の認証情報を登録します
      + ユーザに root パスワードに設定したパスワードを入力し、それ以外は空欄にして OK を
        クリックします
      + Credentials リストボックスから作成した作成した認証情報を選択します

* 画面下の「保存」をクリックして完了します。


