// 検査仕様シート定義

evidence.source = './Solarisチェックシート.xlsx'

// 検査結果ファイル出力先

// evidence.target='./build/Solarisチェックシート.xlsx'
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
// 並列度を指定をしても、指定したドメインタスクはシリアルに実行する

// test.serialization.tasks = ['vCenter']

// DryRunモードログ保存先

test.dry_run_staging_dir = './src/test/resources/log/'

// コマンド採取のタイムアウト
// Windows,vCenterの場合、全コマンドをまとめたバッチスクリプトのタイムアウト値

test.Solaris.timeout = 300

// コマンド採取のデバッグモード

// test.Solaris.debug  = false

// Solaris 接続情報

// account.Solaris.Test.user      = 'root'
// account.Solaris.Test.password  = 'root0000'
account.Solaris.Test.user      = 'guest'
account.Solaris.Test.password  = 'guest000'
account.Solaris.Test.work_dir  = '/tmp/gradle_test'
//account.Solaris.Test.logon_test = [['user':'someuser', 'password':'P@ssword'],
//                                   ['user':'root'  , 'password':'P@ssw0rd']]

 
