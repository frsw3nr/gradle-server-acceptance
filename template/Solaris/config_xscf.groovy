// 検査仕様シート定義

evidence.source = './template/Solaris/XSCFチェックシート.xlsx'

// 検査結果ファイル出力先

 evidence.target='./build/XSCFチェックシート_<date>.xlsx'
// evidence.target='./build/xscf_check_sheet.xlsx'

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

test.XSCF.timeout = 30

// コマンド採取のデバッグモード

test.XSCF.debug  = false

// DryRun 予行演習モード

test.XSCF.dry_run   = false

// XSCF 接続情報

account.XSCF.Test.user      = 'console'
account.XSCF.Test.password  = 'console0'
account.XSCF.Test.work_dir  = '/tmp/gradle_test'

