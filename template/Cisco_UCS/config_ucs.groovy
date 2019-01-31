// 検査仕様シート定義

evidence.source = './template/Cisco_UCS/UCSチェックシート.xlsx'

// 検査結果ファイル出力先

evidence.target='./build/UCSチェックシート_<date>.xlsx'
//evidence.target='./build/ucs_check_sheet.xlsx'

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

test.CiscoUCS.timeout = 300

// コマンド採取のデバッグモード

 test.CiscoUCS.debug  = false

// DryRun 予行演習モード

test.CiscoUCS.dry_run   = false

// CiscoUCS 接続情報

account.CiscoUCS.Test.use_emulator = false    // エミュレータソフトを使用する場合はtrue
account.CiscoUCS.Test.user         = 'admin'
account.CiscoUCS.Test.password     = 'P@ssw0rd'
