// 検査仕様シート定義

evidence.source = './src/test/resources/check_sheet.xlsx'
evidence.sheet_name_server = 'Target'
evidence.sheet_name_rule = 'Rule'
evidence.sheet_name_spec = [
    'Linux':   'Check(Linux)',
    'Windows': 'Check(Windows)',
]

// 検査結果ファイル出力先

evidence.target='./build/check_sheet_<date>.xlsx'
//evidence.target='./build/check_sheet.xlsx'

// 検査結果ログディレクトリ

evidence.staging_dir='./build/log.<date>'
//evidence.staging_dir='./build/log'

// 並列化しないタスク
test.serialization.tasks = ['vCenter']

// 検査モード

test.dry_run_staging_dir = './src/test/resources/log/'
test.Linux.dry_run   = false
test.Linux.timeout   = 30
test.Windows.dry_run = false
test.Windows.timeout = 30
test.vCenter.dry_run = false
test.vCenter.timeout = 30

// vCenter接続情報

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
