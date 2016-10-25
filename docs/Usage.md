利用手順
========

事前準備
--------

**設定情報確認**

Linux 接続情報

	Hostname: ostrich, IP: 192.168.10.1, User: someuser, Password: XXX

vCenter 接続情報

	IP : 192.168.10.100, User: root, Password: XXXX

VM名

	ostrich

**検査ファイル設定**

1.チェックシート.xlsx

シート「チェック対象VM」に検査対象サーバの接続情報記入。

2. 設定ファイル

config/config.groovy 内のサーバ接続情報の箇所を編集。

	// vCenter接続情報
	
	account.vCenter.Test.server   = '192.168.10.100'
	account.vCenter.Test.user     = 'root'
	account.vCenter.Test.password = 'XXXX'
	
	// Linux 接続情報
	
	account.Linux.Test.user      = 'someuser'
	account.Linux.Test.password  = 'XXXX'
	account.Linux.Test.work_dir  = '/tmp/gradle_test'

検査実行
--------

**getconfig実行**

server-acceptanceディレクトリに移動してテスト実行

	cd (解凍ディレクトリ)\server-acceptance
	getconfig

検査対象サーバを絞り込みたい場合

	getconfig -s ostrich

さらに検査IDを絞り込みたい場合

	getconfig -s ostrich -t hostname,lsb

**検査結果確認**

buildの下に検査結果が出力される。

