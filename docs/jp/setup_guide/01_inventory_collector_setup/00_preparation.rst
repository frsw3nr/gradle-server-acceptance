事前準備
========

システム要件
------------

Windows 7 64bit、Windows Server 2012 R2以上のPCが必要です。

* CPU 1 Core以上
* Memory 4 GB以上
* Disk 100 GB以上

プライベートネットワークの切り替え
----------------------------------

パブリックからプライベートネットワークの切り替え
次頁のリモートアクセス設定の事前準備でネットワークをプライベートネットワークに変更します。 管理者ユーザで PowerShell を起動し、以下コマンドを実行ます

.. note::

   以下はWindows Server 2012 R2 以上で利用可能なコマンドとなります。
   Windows7 の場合は、「コントロールパネル」、「ネットワークと共有センター」
   画面からパブリックネットワークの有無を確認してください

::

   Get-NetConnectionProfile -IPv4Connectivity Internet

上記結果の NetworkCategory が「Public」の場合は以下コマンドを実行して、
プライベートに変更します。「Private」 または 「Domain」 の場合は実行不要です。

::

   Set-NetConnectionProfile -InterfaceAlias (Get-NetConnectionProfile -IPv4Connectivity Internet).InterfaceAlias -NetworkCategory Private

