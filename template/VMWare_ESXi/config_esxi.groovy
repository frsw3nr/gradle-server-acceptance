// 検査仕様シート定義

evidence.source = './template/VMWare_ESXi/ESXiチェックシート.xlsx'

// 検査結果ファイル出力先

evidence.target='./build/ESXiチェックシート_<date>.xlsx'
// evidence.target='./build/esxi_check_sheet.xlsx'

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

test.VMHost.timeout = 300

// コマンド採取のデバッグモード

test.VMHost.debug  = false

// DryRun 予行演習モード

test.VMHost.dry_run   = false

// ESXi接続情報

account.VMHost.Test.vCenter  = '192.168.10.100'
account.VMHost.Test.user     = 'test_user'
account.VMHost.Test.password = 'P@ssword'

