// 検査仕様シート定義

//evidence.source = './サーバチェックシート.xlsx'
evidence.source = './build/サーバチェックシート.xls'

// 検査結果ファイル出力先

//evidence.target='./build/サーバチェックシート_<date>.xlsx'
evidence.target='./build/check_sheet.xlsx'

// 検査結果ログディレクトリ
evidence.staging_dir='./build/log'

// 検査、検証結果ディレクトリ
evidence.json_dir='./src/test/resources/json'

// レポート変換マップ
report.item_map.target = [
    'server'        : 'name',
    'domain'        : 'domain',
    'ip'            : 'ip',
    'successrate'   : 'success_rate',
    'verifycomment' : 'verify_comment',
]

report.item_map.platform.Linux = [
    'os'   : 'os',
    'os2'  : 'arch',
    'cpu'  : 'cpu_total',
    'mem'  : 'mem_total',
    'disk' : 'filesystem',
    'net'  : 'net_ip',
]

report.item_map.platform.Windows = [
    'os'   : 'os_caption',
    'os2'  : 'os_architecture',
    'cpu'  : 'cpu_total',
    'mem'  : 'visible_memory',
    'disk' : 'filesystem',
    'net'  : 'network',
]

report.item_map.platform.iLO = [
    'net_mng' : 'Nic',
]

report.item_map.platform.PRIMERGY = [
    'net_mng' : 'nic',
]

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

test.Linux.dry_run   = false
test.Windows.dry_run = false
test.vCenter.dry_run = false

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

