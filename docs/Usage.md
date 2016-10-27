利用手順
========

検査対象サーバの環境設定
------------------------

* vCenter, ESXi の情報採取は、検査PCから SSH 接続できる設定が必要です
* Windows の情報採取は、検査対象サーバ PowerShell のリモートアクセス許可設定が必要です

	**リモートシェル許可設定**

	PowerShellコンソールでリモート接続許可設定をします。

	    Enable-PSRemoting -Force
	    Set-Item wsman:\localhost\Client\TrustedHosts -Value * -Force

	**ファイヤーウォール許可**

	サーバーマネージャーを開き、「Windows ファイアウォール」の設定をクリックして許可設定をします。
	許可設定をしないと、エラー "Get-WmiObject : RPC サーバーを利用できません" が発生します。

検査PCの事前準備
----------------

**チェックシート.xlsx 編集**

1.シート「チェック対象VM」に検査対象サーバの接続情報を記入します。

* server_name, ip, platfom

	上記は必須項目です。

* os_account_id, remote_account_id, remote_alias

	config.groovy 設定ファイル内に記入した接続アカウントIDを記入します。
	os_account_id は、Linux,Windowsなど直接サーバに接続して検査をする場合に使用します。
	remote_account_id は、vCenter などサーバ経由でリモートで検査をする場合に使用します。
	remote_alias は、リモート検査では必須です。 vCenter などリモート側のサーバ名定義を記入します。

* verify_id

	検査ルールを使用する場合にルールIDを記入します。不要な場合は未記入。

* verify_id 以降の行

	カスタマイズ用の設定項目となります。検査スクリプト内で追加した設定項目を使用します。

2.シート「検査ルール」を記入します。

採取値のチェックが必要な場合、検査ルールを記入します。
Groovy言語での記入となり、変数 x が入力パラメータとして、以下形式でルールを記入します。

* 検査値が数値の場合

	"値 == x"、"x < 値"、"値 <= x && x < 値" などの条件式を記入しします

* 検査値が文字列の場合

	"x =~ /正規表現/" で条件式を記入します

**設定ファイル config/config.groovy 編集**

config/config.groovy 内のサーバ接続情報の箇所を編集します。

* リモート検査の接続情報

```
	// vCenter接続情報
	account.Remote.Test.server   = '192.168.10.100'
	account.Remote.Test.user     = 'root'
	account.Remote.Test.password = 'XXXX'
```

* ローカル検査の接続情報

```
	// Linux 接続情報
	account.Linux.Test.user      = 'someuser'
	account.Linux.Test.password  = 'XXXX'
	account.Linux.Test.work_dir  = '/tmp/gradle_test'
```

検査実行
--------

**getconfig実行**

server-acceptanceディレクトリに移動してgetconfigコマンドを実行します

	cd (解凍ディレクトリ)\server-acceptance
	getconfig

検査対象サーバを絞り込みたい場合

	getconfig -s ostrich

さらに検査IDを絞り込みたい場合

	getconfig -s ostrich -t hostname,lsb

**検査結果確認**

buildの下に検査結果が出力される。

