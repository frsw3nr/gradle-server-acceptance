作業報告
========

作業完了報告
------------

監視設定と確認作業が完了したら、管理者に報告します。

エビデンス登録
--------------

管理者は内容の確認をし、結果を構成管理データベースに登録します。

前頁の検査で実行したWindowsサーバ環境から実行します。

スタートメニューから PowerShell を起動して、コンソールを開きます。
Zabbix 監視設定収集用の Getconfig プロジェクトディレクトリ
「C:\\users\\administrator\\work\\server-acceptance-zabbix」に移動します。

::

   cd C:\users\administrator\work\server-acceptance-zabbix

「getconfig -u db」コマンドを実行して、インベントリ収集結果を
Redmine 構成管理データベースに登録します。

::

   getconfig -u db

