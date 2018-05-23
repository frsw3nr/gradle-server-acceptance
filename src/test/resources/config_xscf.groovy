// 検査仕様シート定義

evidence.source = './src/test/resources/check_sheet.xlsx'
evidence.sheet_name_server = 'Target'
evidence.sheet_name_rule = 'Rule'
evidence.sheet_name_spec = [
    'Linux':   'CheckSheet(Linux)',
    'Windows': 'CheckSheet(Windows)',
]

// 検査結果ファイル出力先

evidence.target='./build/check_sheet_<date>.xlsx'
//evidence.target='./build/check_sheet.xlsx'

// 検査結果ログディレクトリ
evidence.staging_dir='./build/log'

// 検査、検証結果ディレクトリ
evidence.json_dir='./src/test/resources/json'

// CSV変換マップ

evidence.csv_item_map = [
    'サーバ名' :            'server_name',
    'IPアドレス' :          'ip',
    'Platform' :            'platform',
    'OSアカウントID' :      'os_account_id',
    'vCenterアカウントID' : 'remote_account_id',
    'VMエイリアス名' :      'remote_alias',
    '検査ID' :              'verify_id',
    'CPU数' :               'NumCpu',
    'メモリ量' :            'MemoryGB',
    'ESXi名' :              'ESXiHost',
    'HDD' :                 'HDDtype',
]

// 並列化しないタスク

test.serialization.tasks = ['vCenter']

// DryRunモードログ保存先

test.dry_run_staging_dir = './src/test/resources/log/'

// コマンド採取のタイムアウト
// Windows,vCenterの場合、全コマンドをまとめたバッチスクリプトのタイムアウト値

test.SparcXscf.timeout   = 5

// コマンド採取のデバッグモード

test.SparcXscf.debug   = false

// SparcXscf 接続情報

account.SparcXscf.Test.server   = '10.20.129.22'
account.SparcXscf.Test.user     = 'console'
account.SparcXscf.Test.password = 'console0'

