// 検査仕様シート定義

evidence.source = './src/test/resources/check_zabbix.xlsx'
evidence.sheet_name_server = 'Target'
evidence.sheet_name_rule = 'Rule'
evidence.sheet_name_spec = [
    'Zabbix':   'Check(Zabbix)',
]

// 検査結果ファイル出力先

evidence.target='./build/check_sheet_<date>.xlsx'
//evidence.target='./build/check_sheet.xlsx'

// 検査結果ログディレクトリ

evidence.staging_dir='./build/log.<date>'
//evidence.staging_dir='./build/log'

// 並列化しないタスク

test.serialization.tasks = ['Zabbix']

// DryRunモードログ保存先

test.dry_run_staging_dir = './src/test/resources/log/'

// コマンド採取のタイムアウト
// Windows,vCenterの場合、全コマンドをまとめたバッチスクリプトのタイムアウト値

test.StorageACS.timeout   = 300

// Zabbix接続情報

account.Zabbix.Test.user      = 'someuser'
account.Zabbix.Test.password  = 'P@ssword'
account.Zabbix.Test.work_dir  = '/tmp/gradle_test'
