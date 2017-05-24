// 検査仕様シート定義

evidence.source = './サーバーチェックシート.xlsx'
evidence.sheet_name_server = 'チェック対象'
evidence.sheet_name_rule = '検査ルール'
evidence.sheet_name_spec = [
    'Linux':   'ゲストOSチェックシート(Linux)',
    'Windows': 'ゲストOSチェックシート(Windows)',
    'VMHost':  'ESXiホストチェックシート(VMHost)',
]

// 検査結果ファイル出力先

evidence.target='./build/チェックシート_<date>.xlsx'

// 検査結果ログディレクトリ

evidence.staging_dir='./build/log'

// CSV変換マップ

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

// 並列化しないタスク
// 並列度を指定をしても、指定したドメインタスクはシリアルに実行する

test.serialization.tasks = ['vCenter', 'VMHost']

// DryRunモードログ保存先

test.dry_run_staging_dir = './src/test/resources/log/'

// コマンド採取のタイムアウト
// Windows,vCenterの場合、全コマンドをまとめたバッチスクリプトのタイムアウト値

test.Linux.timeout   = 300
test.Windows.timeout = 300
test.VMHost.timeout  = 300

// コマンド採取のデバッグモード

// test.Linux.debug   = false
// test.Windows.debug = false
// test.VMHost.debug  = false

// vCenter接続情報

account.Remote.Test.server   = '192.168.10.100'
account.Remote.Test.user     = 'test_user'
account.Remote.Test.password = 'P@ssword'

// Linux 接続情報

account.Linux.Test.user      = 'someuser'
account.Linux.Test.password  = 'P@ssword'
account.Linux.Test.work_dir  = '/tmp/gradle_test'
// account.Linux.Test.logon_test = [['user':'test1' , 'password':'test1'],
//                                  ['user':'root'  , 'password':'P@ssw0rd']]

// Windows 接続情報

account.Windows.Test.user     = 'administrator'
account.Windows.Test.password = 'P@ssword'
// account.Windows.Test.logon_test = [['user':'test1' , 'password':'test1'],
//                                    ['user':'test2' , 'password':'test2']]

// VMHost 接続情報

account.VMHost.Test.user      = 'root'
account.VMHost.Test.password  = 'P@ssword'

// Redmine Default filter options

redmine.default_filter_options = [
    'project': '構成管理データベース',
    'status': '構築前',
    'version': '%',
    'tracker': '%',
]

// Package requirements list

package.requirements.oracle = [
    'compat-libcap1','compat-libstdc++-33','libstdc++-devel', 'gcc-c++','ksh','libaio-devel'
    ]
package.requirements.base   = [
    'sysstat','dmidecode','strace','net-snmp-libs','net-snmp-utils','busybox-anaconda',
    'alchemist','xinetd','tftp-server','system-config-netboot-cmd','system-config-netboot'
    ]
package.requirements.sophos = [
    'glibc', 'nss-softokn-freebl', 'libXau', 'libxcb', 'libX11', 'libXpm'
    ]
package.requirements.msm    = [
    'MegaRAID_Storage_Manager','Lib_Utils2','Lib_Utils','sas_snmp','sas_ir_snmp'
    ]
