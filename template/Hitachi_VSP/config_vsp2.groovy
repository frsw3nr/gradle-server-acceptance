// 検査仕様シート定義

evidence.source = './template/Hitachi_VSP/HitachiVSP2チェックシート.xlsx'

// 検査結果ファイル出力先

//evidence.target='./build/HitachiVSP2チェックシート_<date>.xlsx'
evidence.target='./build/vsp2_check_sheet.xlsx'

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

test.HitachiVSP2.timeout = 300

// コマンド採取のデバッグモード

 test.HitachiVSP2.debug  = false

// DryRun 予行演習モード

test.HitachiVSP2.dry_run   = false

// HitachiVSP2 接続情報

account.HitachiVSP2.Test.report_dir = './src/hitachi_vsp_report'
account.HitachiVSP2.Test.user     = 'administrator'
account.HitachiVSP2.Test.password = 'P@ssw0rd'
