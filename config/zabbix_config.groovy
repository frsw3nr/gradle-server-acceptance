// 検査仕様シート定義

evidence.source = './Zabbix監視設定チェックシート.xlsx'

// 検査結果ファイル出力先

//evidence.target='./build/Zabbix監視設定チェックシート_<date>.xlsx'
evidence.target='./build/zabbix_check_sheet.xlsx'

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

// 並列化しないタスク

test.serialization.tasks = []

// DryRunモードログ保存先

test.dry_run_staging_dir = './src/test/resources/log/'

// コマンド採取のタイムアウト
test.Zabbix.timeout = 300

// Zabbix接続情報

account.Zabbix.Test.server   = '192.168.0.20'
account.Zabbix.Test.user     = 'Admin'
account.Zabbix.Test.password = 'zabbix'

account.Remote.Test2.server   = 'yps4za'
account.Remote.Test2.user     = 'Admin'
account.Remote.Test2.password = 'zabbix'

// Redmine フィルター設定

redmine.default_values = [
	'remote_account_id' : 'Test',
	'verify_id' : 'RuleAP',
]
redmine.fixed_items    = [
    'platform' : 'Zabbix'
]
redmine.required_items = ['server_name', 'platform', 'remote_account_id']

redmine.default_filter_options = [
    'project': '構成管理検証サイト',
    'status': '%',
    'version': '%',
    'tracker': 'IAサーバー',
]


