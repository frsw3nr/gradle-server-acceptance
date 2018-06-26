// 検査仕様シート定義

evidence.source = './iLOチェックシート.xlsx'

// 検査結果ファイル出力先

//evidence.target='./build/iLOチェックシート_<date>.xlsx'
evidence.target='./build/ilo_check_sheet.xlsx'

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

report.item_map.platform.iLO = [
]

// 並列化しないタスク
// 並列度を指定をしても、指定したドメインタスクはシリアルに実行する

test.serialization.tasks = ['iLO']

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
test.iLO.debug  = true

// iLO 接続情報

account.iLO.Test.user      = 'toshiba'
account.iLO.Test.password  = 'Toshiba1048'
