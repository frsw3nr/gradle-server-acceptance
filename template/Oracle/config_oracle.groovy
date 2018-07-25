// 検査仕様シート定義

evidence.source = './template/Oracle/Oracle設定チェックシート.xlsx'

// 検査結果ファイル出力先

// evidence.target='./build/Oracle設定チェックシート_<date>.xlsx'
evidence.target='./build/Oracle_check_sheet.xlsx'

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

test.Oracle.timeout = 300

// コマンド採取のデバッグモード

test.Oracle.debug  = false

// DryRun 予行演習モード

test.Oracle.dry_run   = false

// Oracle接続情報

account.Oracle.Test.user     = 'zabbix'
account.Oracle.Test.password = 'zabbix'
account.Oracle.Test.port     = 1521
