// 検査仕様シート定義

evidence.source = './template/Zabbix/ZabbixServer監視設定チェックシート.xlsx'

// 検査結果ファイル出力先

evidence.target='./build/ZabbixServer監視設定チェックシート_<date>.xlsx'
// evidence.target='./build/zabbix_server_check_sheet.xlsx'

// 検査結果ログディレクトリ

evidence.staging_dir='./build/log'

// 検査、検証結果ディレクトリ

evidence.json_dir='./src/test/resources/json'

// 並列化しないタスク
// 並列度を指定をしても、指定したドメインタスクはシリアルに実行する

test.serialization.tasks = []

// DryRunモードログ保存先

test.dry_run_staging_dir = './src/test/resources/log/'

// コマンド採取のタイムアウト
// Windows,vCenterの場合、全コマンドをまとめたバッチスクリプトのタイムアウト値

test.ZabbixServer.timeout = 300

// コマンド採取のデバッグモード

test.ZabbixServer.debug  = false

// DryRun 予行演習モード

test.ZabbixServer.dry_run   = false

// ZabbixServer接続情報

account.ZabbixServer.Test.user     = 'Admin'
account.ZabbixServer.Test.password = 'zabbix'
