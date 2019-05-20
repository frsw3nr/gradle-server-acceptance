// 検査仕様シート定義

evidence.source = './template/FJ_Primergy/PRIMERGYチェックシート.xlsx'

// 検査結果ファイル出力先

evidence.target='./build/PRIMERGYチェックシート_<date>.xlsx'
// evidence.target='./build/primergy_check_sheet.xlsx'

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
test.Primergy.timeout = 300

// コマンド採取のデバッグモード

test.Primergy.debug  = false

// DryRun 予行演習モード

test.Primergy.dry_run   = false

// Primergy接続情報

account.Primergy.Test.irmc     = 5
account.Primergy.Test.user     = 'admin'
account.Primergy.Test.password = 'admin'
