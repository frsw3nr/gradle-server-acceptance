// 検査仕様シート定義

evidence.source = './template/Router/CiscoIOSチェックシート.xlsx'

// 検査結果ファイル出力先

// evidence.target='./build/CiscoIOSチェックシート_<date>.xlsx'
evidence.target='./build/ios_check_sheet.xlsx'

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

test.RouterCisco.timeout = 30

// コマンド採取のデバッグモード

test.RouterCisco.debug  = false

// DryRun 予行演習モード

test.RouterCisco.dry_run   = false

// RouterCisco 接続情報

account.RouterCisco.Test.user      = 'admin'
account.RouterCisco.Test.password  = 'P@ssw0rd'
account.RouterCisco.Test.work_dir  = '/tmp/gradle_test'

