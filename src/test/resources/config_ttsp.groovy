// 検査仕様シート定義

evidence.source = './src/test/resources/check_ttsp.xlsx'
evidence.sheet_name_server = 'Target'
evidence.sheet_name_rule = 'Rule'
evidence.sheet_name_spec = [
    'StorageACS':   'Check(TTSP)',
]

// 検査結果ファイル出力先

evidence.target='./build/check_sheet_<date>.xlsx'
//evidence.target='./build/check_sheet.xlsx'

// 検査結果ログディレクトリ

evidence.staging_dir='./build/log.<date>'
//evidence.staging_dir='./build/log'

// 並列化しないタスク

// test.serialization.tasks = []

// DryRunモードログ保存先

test.dry_run_staging_dir = './src/test/resources/log/'

// コマンド採取のタイムアウト
// Windows,vCenterの場合、全コマンドをまとめたバッチスクリプトのタイムアウト値

test.StorageACS.timeout   = 300

// vCenter接続情報

account.Remote.Test.server   = '192.168.10.100'
account.Remote.Test.user     = 'test_user'
account.Remote.Test.password = 'P@ssword'

