// Inventory Database connection setting

inventory_db.dataSource.username = "sa"
inventory_db.dataSource.password = "sa"
inventory_db.dataSource.url = "jdbc:h2:mem:"
inventory_db.dataSource.driver = "org.h2.Driver"

// inventory_db.dataSource.username = "redmine"
// inventory_db.dataSource.password = "getperf"
// inventory_db.dataSource.url = "jdbc:mysql://redmine:3306/redmine?useUnicode=true&characterEncoding=utf8"
// inventory_db.dataSource.driver = "com.mysql.jdbc.Driver"

// Configuration Management Database connection setting

cmdb.dataSource.username = "sa"
cmdb.dataSource.password = "sa"
cmdb.dataSource.url = "jdbc:h2:mem:"
cmdb.dataSource.driver = "org.h2.Driver"

// cmdb.dataSource.username = "redmine"
// cmdb.dataSource.password = "getperf"
// cmdb.dataSource.url = "jdbc:mysql://redmine:3306/redmine?useUnicode=true&characterEncoding=utf8"
// cmdb.dataSource.driver = "com.mysql.jdbc.Driver"

// インベントリ用チケットカスタムフィールド名

ticket.custom_field.inventory = 'インベントリ'

// ポートリストトラッカー名

port_list.tracker = 'ポートリスト'

// ポートリストチケットカスタムフィールドリスト

port_list.custom_fields = [
    'ポート番号' : 'description',
    'MACアドレス' : 'mac',
    'MACアドレスベンダー' : 'vendor',
    'スイッチ名' : 'switch_name',
    'ネットマスク' : 'netmask',
    '機器種別' : 'device_type',
]
