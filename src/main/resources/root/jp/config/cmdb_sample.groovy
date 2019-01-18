// Configuration Management Database connection setting

// cmdb.dataSource.username = "sa"
// cmdb.dataSource.password = "sa"
// cmdb.dataSource.url = "jdbc:h2:mem:"
// cmdb.dataSource.driver = "org.h2.Driver"

cmdb.dataSource.username = "redmine"
cmdb.dataSource.password = "getperf"
cmdb.dataSource.url = "jdbc:mysql://redmine:3306/redmine?useUnicode=true&characterEncoding=utf8"
cmdb.dataSource.driver = "com.mysql.jdbc.Driver"

// // Redmine custome fields map

// cmdb.redmine.custom_fields = [
//     'ホスト名' :         'server_name',
//     'IPアドレス' :       'ip',
//     'プラットフォーム' : 'platform',
//     'OSアカウント' :     'os_account_id',
//     '固有パスワード' :   'os_specific_password',
//     'vCenterアカウント': 'remote_account_id',
//     'Zabbixアカウント':  'remote_account_id',
//     'VMエイリアス名' :   'remote_alias',
//     '検証ID' :           'verify_id',
//     '比較対象' :         'compare_server',
//     'CPU割り当て' :      'NumCpu',
//     'メモリ割り当て' :   'MemoryGB',
//     'ESXiホスト' :       'ESXiHost',
//     'ストレージ構成' :   'HDDtype',
// ]

// インベントリ用チケットカスタムフィールド名

ticket.custom_field.inventory = 'インベントリ'

// ポートリストトラッカー名

port_list.tracker = 'ポートリスト'

// ポートリスト運用ステータスID
// Redmine チケットステータスから検索

port_list.in_operation_status_id = 10

// ポートリストチケットカスタムフィールドリスト

port_list.custom_fields = [
    'ポート番号' : 'port_no',
    'ポートデバイス' : 'description',
    'MACアドレス' : 'mac',
    'MACアドレスベンダー' : 'vendor',
    'スイッチ名' : 'switch_name',
    'ネットマスク' : 'netmask',
    '機器種別' : 'device_type',
    '台帳つき合わせ' : 'lookup',
    '管理用' : 'managed',
]
