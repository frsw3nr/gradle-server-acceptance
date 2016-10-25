// 検査仕様シート定義

evidence.source = './サーバーチェックシート.xlsx'
evidence.sheet_name_server = 'チェック対象VM'
evidence.sheet_name_rule = '検査ルール'
evidence.sheet_name_spec = [
    'Linux':   'ゲストOS払出しチェックシート(Linux)',
    'Windows': 'ゲストOS払出しチェックシート(Windows)',
]

// 検査結果ファイル出力先

evidence.target='./build/チェックシート_<date>.xlsx'

// 検査結果ログディレクトリ

evidence.staging_dir='./build/log.<date>'

// 並列化しないタスク
// 並列度を指定をしても、指定したドメインタスクはシリアルに実行する

test.serialization.tasks = ['vCenter']

// DryRunモードログ保存先

test.dry_run_staging_dir = './src/test/resources/log/'

// コマンド採取のタイムアウト
// Windows,vCenterの場合、全コマンドをまとめたバッチスクリプトのタイムアウト値

test.Linux.timeout   = 30
test.Windows.timeout = 300
test.vCenter.timeout = 300

// vCenter接続情報

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
