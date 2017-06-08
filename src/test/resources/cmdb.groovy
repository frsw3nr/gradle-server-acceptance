// Configuration Management Database connection setting

cmdb.dataSource.username = "sa"
cmdb.dataSource.password = "sa"
cmdb.dataSource.url = "jdbc:h2:mem:"
cmdb.dataSource.driver = "org.h2.Driver"

// Redmine custome fields map

cmdb.redmine.custom_fields = [
    'ホスト名' :         'server_name',
    'IPアドレス' :       'ip',
    'プラットフォーム' : 'platform',
    'OSアカウント' :     'os_account_id',
    '固有パスワード' :   'os_specific_password',
    'vCenterアカウント': 'remote_account_id',
    'VMエイリアス名' :   'remote_alias',
    '検証ID' :           'verify_id',
    '比較対象' :         'compare_server',
    'CPU割り当て' :      'NumCpu',
    'メモリ割り当て' :   'MemoryGB',
    'ESXiホスト' :       'ESXiHost',
    'ストレージ構成' :   'HDDtype',
]
