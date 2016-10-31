利用手順
========

事前準備
========

検査対象サーバの環境設定
------------------------

* vCenter, ESXi の情報採取は、検査PCから SSH 接続できる設定が必要です
* Windows の情報採取は、検査対象サーバ PowerShell のリモートアクセス許可設定が必要です

	*リモートシェル許可設定*

	PowerShellコンソールでリモート接続許可設定をします。

	    Enable-PSRemoting -Force

	*ファイヤーウォール許可*

	サーバーマネージャーを開き、「Windows ファイアウォール」の設定をクリックして許可設定をします。
	許可設定をしないと、エラー "Get-WmiObject : RPC サーバーを利用できません" が発生します。

	*検査PC側の許可設定*

	検査PC側でも接続許可設定が必要です。PowerShellコンソールでリモート接続許可設定をします。

	    Enable-PSRemoting -Force
	    Set-Item wsman:\localhost\Client\TrustedHosts -Value * -Force

検査PCの事前準備
----------------

*チェックシート.xlsx 編集*

1.シート「チェック対象VM」に検査対象サーバの接続情報を記入します。

* server_name, ip, platfom

	上記は必須項目です。

* os_account_id

	config.groovy 設定ファイル内に記入した接続アカウントIDを記入します。
	Linux,Windowsなどダイレクトにサーバに接続して検査をする場合に使用します。

* remote_account_id, remote_alias

	remote_account_id は、vCenter などサーバ経由でリモートで検査をする場合に使用します。
	remote_alias は、リモート検査の場合は必須となり、 vCenter などリモート側のサーバ名定義を記入します。

* verify_id

	検査ルールを使用する場合にルールIDを記入します。不要な場合は未記入。

* 設定項目のカスタマイズ

	verify_id 以降の行はカスタマイズ用の設定項目となります。
	検査スクリプト内で設定値を参照します。
	詳細は開発者ガイドを参照してください。

2.シート「検査ルール」を記入します。

採取値のチェックが必要な場合、検査ルールを記入します。
不要な場合は省略して構いません。
Groovy言語での記入となり、変数 x が入力パラメータとして、以下形式でルールを記入します。

* 不等式の評価

	**"値 == x"**、**"x < 値"**、**"値 <= x && x < 値"** などの不等号を条件式に記入します

* 正規表現の評価

	**"x =~ /正規表現/"** で条件式を記入します

*設定ファイル config/config.groovy 編集*

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

* その他の設定

以下のタイムアウト値は環境に合わせて調整してください。

```
// コマンド採取のタイムアウト
// Windows,vCenterの場合、全コマンドをまとめたバッチスクリプトのタイムアウト値
test.Linux.timeout   = 30
test.Windows.timeout = 300
test.vCenter.timeout = 300
```

使用方法
========

コマンドの使用方法
------------------

*getconfig検査コマンド実行*

server-acceptanceディレクトリに移動してgetconfigコマンドを実行します

```
cd (解凍ディレクトリ)\server-acceptance
getconfig
```

実行オプションは以下の通りです。

```
./getconfig -h
usage: getspec
 -c,--config <arg>     Config file path : ./config/config.groovy
 -d,--dry-run          Enable Dry run test
 -e,--excel <arg>      Excel test spec file path : check_sheet.xlsx
 -h,--help             Print usage
 -p,--parallel <arg>   Degree of test runner processes
 -r,--resource <arg>   Dry run test resource : ./src/test/resources/log/
 -s,--server <arg>     Filtering list of servers : svr1,svr2,...
 -t,--test <arg>       Filtering list of test_ids : vm,cpu,...
 -v,--verify           Disable verify test
```

* -c,--config <arg>

	設定ファイル config.groovy のパスを指定します。デフォルトは./config/config.groovy です。

* -d,--dry-run

	予行演習モード(DryRun)にします。
	検査対象への情報採取はせずに、予め用意したログファイルを参照して検査をします。

* -e,--excel <arg>

	検査シートパスを指定します。デフォルトは、./チェックシート.xlsx です。

* -h,--help

	ヘルプを出力します。

* -p,--parallel <arg>

	各サーバの検査の並列度を指定します。
	多重化したくないシナリオは設定ファイル内のtest.serialization.tasksパラメータで指定します。

* -r,--resource <arg>

	予行演習モード(DryRun)で使用するログファイルディレクトリパスを指定します。
	デフォルトは、./src/test/resources/log/ です。

* -s,--server <arg>

	検査対象のサーバを絞り込む場合に使用します。'-s svr1,svr2,...' の様に指定します。

* -t,--test <arg>

	検査項目を絞り込む場合に使用します。'-t vm,cpu,...' の様に指定します。

* -v,--verify

	検査ルールによる評価を無効にする場合に指定します。

コマンドの使用例
----------------

検査対象サーバを絞り込みたい場合

```
getconfig -s ostrich							# 検査対象サーバ名を指定
```

さらに検査IDを絞り込みたい場合

```
getconfig -s ostrich -t hostname,lsb			# 検査IDを指定
```

予行演習(DryRun)モードを用いて、一度検査したログを再度テストをする場合

```
getconfig -d -r ./build/log.20161028_090553/	# 日付付きログディレクトリを指定
```

実行結果の確認
--------------

getconfigを実行するとbuildの下に検査結果が出力されます。

* チェックシート_{日時}.xlsx

	各プラットフォームの検査シートに検査対象サーバの検査結果を記録します。
	各種デバイス情報を新規シートに記録します。

* log.{日時} ディレクトリ

	'log.{日時}' ディレクトリの下に、
	**'{プラットフォーム}/{検査対象サーバ}/{検査シナリオ}/{検査ID}'**のファイルパス形式で、
	検査結果をログ出力します。

以上、
