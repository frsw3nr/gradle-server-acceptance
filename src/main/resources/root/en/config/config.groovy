// Test specification sheet definition

evidence.source = './check_sheet.xlsx'
evidence.sheet_name_server = 'Target'
evidence.sheet_name_rule = 'Rule'
evidence.sheet_name_spec = [
    'Linux':   'Check(Linux)',
    'Windows': 'Check(Windows)',
]

// Test result file output destination

evidence.target='./build/check_sheet_<date>.xlsx'

// Test results log directory

evidence.staging_dir='./build/log.<date>'

// Tasks that do not parallel
// Even if the specified the degree of parallelism, the specified domain task is to run to the serial

test.serialization.tasks = ['vCenter']

// DryRun mode log destination

test.dry_run_staging_dir = './src/test/resources/log/'

// Time out of command collection
// If the configuration is Windows or vCenter, to set a time-out value of PowerShell batch script

test.Linux.timeout   = 30
test.Windows.timeout = 300
test.vCenter.timeout = 300

// vCenter session accout

account.Remote.Test.server   = '192.168.10.100'
account.Remote.Test.user     = 'test_user'
account.Remote.Test.password = 'P@ssword'

// Linux session account

account.Linux.Test.user      = 'someuser'
account.Linux.Test.password  = 'P@ssword'
account.Linux.Test.work_dir  = '/tmp/gradle_test'

// Windows session account

account.Windows.Test.user     = 'administrator'
account.Windows.Test.password = 'P@ssword'
