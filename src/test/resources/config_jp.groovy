// 検査仕様シート定義

evidence.source = './サーバーチェックシート.xlsx'
// evidence.sheet_name_server = 'Target'
// evidence.sheet_name_rule = 'Rule'
// evidence.sheet_name_spec = [
//     'Linux':   'CheckSheet(Linux)',
//     'Windows': 'CheckSheet(Windows)',
// ]

// 検査結果ファイル出力先

evidence.target='./build/check_sheet_<date>.xlsx'
//evidence.target='./build/check_sheet.xlsx'

// 検査結果ログディレクトリ
evidence.staging_dir='./build/log'

// 検査、検証結果ディレクトリ
evidence.json_dir='./src/test/resources/json'

// CSV変換マップ

// evidence.csv_item_map = [
//     'サーバ名' :            'server_name',
//     'IPアドレス' :          'ip',
//     'Platform' :            'platform',
//     'OSアカウントID' :      'os_account_id',
//     'vCenterアカウントID' : 'remote_account_id',
//     'VMエイリアス名' :      'remote_alias',
//     '検査ID' :              'verify_id',
//     'CPU数' :               'NumCpu',
//     'メモリ量' :            'MemoryGB',
//     'ESXi名' :              'ESXiHost',
//     'HDD' :                 'HDDtype',
// ]

// 並列化しないタスク

test.serialization.tasks = ['vCenter']

// DryRunモードログ保存先

test.dry_run_staging_dir = './src/test/resources/log/'

// コマンド採取のタイムアウト
// Windows,vCenterの場合、全コマンドをまとめたバッチスクリプトのタイムアウト値

test.Linux.timeout   = 30
test.Windows.timeout = 300
test.vCenter.timeout = 300

// コマンド採取のデバッグモード

test.Linux.debug   = false
test.Windows.debug = false
test.vCenter.debug = false

// vCenter接続情報

// account.Remote.Test.server   = '192.168.10.100'
// account.Remote.Test.user     = 'test_user'
// account.Remote.Test.password = 'P@ssword'

account.vCenter.Test.server   = '192.168.10.100'
account.vCenter.Test.user     = 'test_user'
account.vCenter.Test.password = 'P@ssword'

// Linux 接続情報

account.Linux.Test.user      = 'someuser'
account.Linux.Test.password  = 'P@ssword'
account.Linux.Test.work_dir  = '/tmp/gradle_test'

// Windows 接続情報

account.Windows.Test.user     = 'administrator'
account.Windows.Test.password = 'P@ssword'

// VMHost 接続情報

// account.VMHost.Test.user     = 'root'
// account.VMHost.Test.password = 'P@ssword'
