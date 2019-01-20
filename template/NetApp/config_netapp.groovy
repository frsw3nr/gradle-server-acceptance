// 検査仕様シート定義

evidence.source = './template/NetApp/DataONTAPチェックシート.xlsx'

// 検査結果ファイル出力先

evidence.target='./build/DataONTAPチェックシート_<date>.xlsx'
// evidence.target='./build/eternus_check_sheet.xlsx'

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

test.NetAppDataONTAP.timeout = 300

// コマンド採取のデバッグモード

 test.NetAppDataONTAP.debug  = false

// DryRun 予行演習モード

test.NetAppDataONTAP.dry_run   = false

// NetAppDataONTAP 接続情報

account.NetAppDataONTAP.Test.user      = 'admin'
account.NetAppDataONTAP.Test.password  = 'password0'
