// 検査仕様シート定義

evidence.source = './template/AIX/AIXチェックシート.xlsx'

// 検査結果ファイル出力先

// evidence.target='./build/AIXチェックシート_<date>.xlsx'
evidence.target='./build/aix_check_sheet.xlsx'

// 検査結果ログディレクトリ
evidence.staging_dir='./build/log'

// 検査、検証結果ディレクトリ
evidence.json_dir='./src/test/resources/json'

// 並列化しないタスク
// 並列度を指定をしても、指定したドメインタスクはシリアルに実行する

test.serialization.tasks = ['']

// DryRunモードログ保存先
test.dry_run_staging_dir = './src/test/resources/log/'

// コマンド採取のタイムアウト
// Windows,vCenterの場合、全コマンドをまとめたバッチスクリプトのタイムアウト値

test.AIX.timeout = 300

// コマンド採取のデバッグモード

 test.AIX.debug  = false

// DryRun 予行演習モード

test.AIX.dry_run   = false

// AIX 接続情報

account.AIX.Test.user      = 'guest'
account.AIX.Test.password  = 'guest0001'
account.AIX.Test.work_dir  = '/tmp/gradle_test'
