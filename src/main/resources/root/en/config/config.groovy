// Check sheet definition.

evidence.source = './check_sheet.xlsx'
evidence.sheet_name_server = 'Target'
evidence.sheet_name_rule = 'Rule'
evidence.sheet_name_spec = [
    'Linux':   'CheckSheet(Linux)',
    'Windows': 'CheckSheet(Windows)',
    'VMHost':  'CheckSheet(VMHost)',
]

// Check sheet output path.

evidence.target='./build/check_sheet_<date>.xlsx'

// Test result log directory.

evidence.staging_dir='./build/log'

// CSV transform map

evidence.csv_item_map = [
    'サーバ名' :            'server_name',
    'IPアドレス' :          'ip',
    'Platform' :            'platform',
    'OSアカウントID' :      'os_account_id',
    'vCenterアカウントID' : 'remote_account_id',
    'VMエイリアス名' :      'remote_alias',
    '検査ID' :              'verify_id',
    '比較対象サーバ名' :    'compare_server',
    'CPU数' :               'NumCpu',
    'メモリ量' :            'MemoryGB',
    'ESXi名' :              'ESXiHost',
    'HDD' :                 'HDDtype',
]

// Tasks not parallelized
// Even if the degree of parallelism is specified, the specified domain task is executed serially

test.serialization.tasks = ['vCenter', 'VMHost']

// DryRun mode log direcory.

test.dry_run_staging_dir = './src/test/resources/log/'

// Command timeout
// In case of Windows,vCenter, it is a whole timeout value of PowerShell script.

test.Linux.timeout   = 300
test.Windows.timeout = 300
test.VMHost.timeout  = 300

// Debug mode

// test.Linux.debug   = false
// test.Windows.debug = false
// test.VMHost.debug  = false

// vCenter session

account.Remote.Test.server   = '192.168.10.100'
account.Remote.Test.user     = 'test_user'
account.Remote.Test.password = 'P@ssword'

// Linux session

account.Linux.Test.user      = 'someuser'
account.Linux.Test.password  = 'P@ssword'
account.Linux.Test.work_dir  = '/tmp/gradle_test'
// account.Linux.Test.logon_test = [['user':'test1' , 'password':'test1'],
//                                  ['user':'root'  , 'password':'P@ssw0rd']]

// Windows session

account.Windows.Test.user     = 'administrator'
account.Windows.Test.password = 'P@ssword'
// account.Windows.Test.logon_test = [['user':'test1' , 'password':'test1'],
//                                    ['user':'test2' , 'password':'test2']]

// VMHost session

account.VMHost.Test.user      = 'root'
account.VMHost.Test.password  = 'P@ssword'
