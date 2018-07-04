// 検査仕様シート定義

evidence.source = './template/Zabbix/Zabbix監視設定チェックシート.xlsx'

// 検査結果ファイル出力先

//evidence.target='./build/Zabbix監視設定チェックシート_<date>.xlsx'
evidence.target='./build/zabbix_check_sheet.xlsx'

// 検査結果ログディレクトリ
evidence.staging_dir='./build/log'

// 検査、検証結果ディレクトリ
evidence.json_dir='./src/test/resources/json'

// 並列化しないタスク
test.serialization.tasks = []

// DryRunモードログ保存先

test.dry_run_staging_dir = './src/test/resources/log/'

// コマンド採取のタイムアウト
test.Zabbix.timeout = 300

// Zabbix接続情報

account.Zabbix.Test.server   = '192.168.0.20'
account.Zabbix.Test.user     = 'Admin'
account.Zabbix.Test.password = 'zabbix'

