// 検査仕様シート定義

evidence.source = './check_sheet.xlsx'
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

// 検査モード

test.dry_run_staging_dir = './src/test/resources/log/'
test.Linux.dry_run   = true
test.Linux.timeout   = 30
test.Windows.dry_run = true
test.Windows.timeout = 30
test.vCenter.dry_run = true
test.vCenter.timeout = 30

// vCenter接続情報

account.Remote.Test.server   = '192.168.10.100'
account.Remote.Test.user     = 'test_user'
account.Remote.Test.password = 'P@ssword'

// Linux 接続情報

account.Linux.Test.user      = 'someuser'
account.Linux.Test.password  = 'P@ssword'
account.Linux.Test.work_dir  = '/tmp'

// Windows 接続情報

account.Windows.Test.user     = 'administrator'
account.Windows.Test.password = 'P@ssword'
