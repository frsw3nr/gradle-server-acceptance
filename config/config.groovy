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

// CSV Export path.

evidence.csv_export='./build/export/compare_<date>.csv'

// Test result log directory.

evidence.staging_dir='./build/log'

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
account.Linux.Test.logon_test = [['user':'test1' , 'password':'test1'],
                                 ['user':'root'  , 'password':'P@ssw0rd']]

// Windows session

account.Windows.Test.user     = 'administrator'
account.Windows.Test.password = 'P@ssword'
account.Windows.Test.logon_test = [['user':'test1' , 'password':'test1'],
                                   ['user':'test2' , 'password':'test2']]

// VMHost session

account.VMHost.Test.user      = 'test_user'
account.VMHost.Test.password  = 'P@ssword'

// Redmine Default filter options

redmine.default_filter_options = [
    'project': '構成管理データベース',
    'status': '構築前',
    'version': '%',
    'tracker': '%',
]
