// 検査仕様シート定義

evidence.source = './PRIMERGYチェックシート.xlsx'

// 検査結果ファイル出力先

// evidence.target='./build/PRIMERGYチェックシート_<date>.xlsx'
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

// 並列化しないタスク
// 並列度を指定をしても、指定したドメインタスクはシリアルに実行する

test.serialization.tasks = []

// DryRunモードログ保存先

test.dry_run_staging_dir = './src/test/resources/log/'

// コマンド採取のタイムアウト
test.Primergy.timeout = 300

// Zabbix接続情報

account.Primergy.Test.user     = 'admin'
account.Primergy.Test.password = 'admin'

