// Configuration Management Database connection setting

cmdb.dataSource.username = "root"
cmdb.dataSource.password = "getperf"
cmdb.dataSource.url = "jdbc:mysql://localhost:3306/redmine?useUnicode=true&characterEncoding=utf8"
cmdb.dataSource.driver = "com.mysql.jdbc.Driver"

// Redmine custome fields map

cmdb.redmine.custom_fields = [
    'ホスト名' :         'server_name',
    'IPアドレス' :       'ip',
    'プラットフォーム' : 'platform',
    'OSアカウント' :     'os_account_id',
    'vCenterアカウント': 'remote_account_id',
    'VMエイリアス名' :   'remote_alias',
    '検証ID' :           'verify_id',
    '比較対象' :         'compare_server',
    'CPU割り当て' :      'NumCpu',
    'メモリ割り当て' :   'MemoryGB',
    'ESXiホスト' :       'ESXiHost',
    'ストレージ構成' :   'HDDtype',
]
