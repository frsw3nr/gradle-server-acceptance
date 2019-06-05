// 検査仕様シート定義

evidence.source = './template/Hitachi_VSP/HitachiVSPチェックシート.xlsx'

// 検査結果ファイル出力先

evidence.target='./build/HitachiVSPチェックシート_<date>.xlsx'
// evidence.target='./build/vsp_check_sheet.xlsx'

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

test.HitachiVSP.timeout = 300

// コマンド採取のデバッグモード

 test.HitachiVSP.debug  = false

// DryRun 予行演習モード

test.HitachiVSP.dry_run   = false

// HitachiVSP 接続情報

account.HitachiVSP.Test.report_dir = './src/hitachi_vsp_report'
account.HitachiVSP.Test.user     = 'administrator'
account.HitachiVSP.Test.password = 'P@ssw0rd'
