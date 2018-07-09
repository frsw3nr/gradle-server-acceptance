// 検査仕様シート定義

evidence.source = './日立VSPストレージチェックシート.xlsx'
evidence.sheet_name_server = 'チェック対象'
evidence.sheet_name_rule = '検査ルール'
evidence.sheet_name_spec = [
    'HitachiVSP':   '日立VSPチェックシート',
]

// 検査結果ファイル出力先

evidence.target='./build/日立VSPチェックシート_<date>.xlsx'

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

test.serialization.tasks = ['HitachiVSP']

// DryRunモードログ保存先

test.dry_run_staging_dir = './src/test/resources/log/'

// コマンド採取のタイムアウト

test.HitachiVSP.timeout   = 300

// コマンド採取のデバッグモード

//test.HitachiVSP.debug = false

// 日立VSP 接続情報

account.HitachiVSP.Test.report_dir = './src/hitachi_vsp_report'

account.HitachiVSP.Test.user     = 'administrator'
account.HitachiVSP.Test.password = 'P@ssw0rd'
account.HitachiVSP.Test2.user     = 'verification'
account.HitachiVSP.Test2.password = 'Hcc12676'
