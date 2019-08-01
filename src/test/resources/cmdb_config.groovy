
// cmdb検索条件

// getconfig 実行オプションで検索状況を指定する
// 実行例：
//   getconfig --filter_tracker 'Linux' --filter_status '構築前' --filter_version '5/9の払出し'
// 実行オプションの指定がない場合は以下条件で検索する

cmdb.search_condition.tracker = '%'
cmdb.search_condition.status  = '構築前'
cmdb.search_condition.version = '%'

// cmdb チェック対象フィールドとRedmineカスタムフィールドの関連名

cmdb.target_server.fields = [
'ホスト名'           : 'server_name',
'IPアドレス'         : 'ip',
'OSアカウント'       : 'os_account_id',
'リモートアカウント' : 'remote_account_id',
'リモートエイリアス' : 'remote_alias',
'検証ID'             : 'verify_id',
'比較対象'           : 'compare_server',
'CPU割り当て'        : 'NumCpu',
'メモリ割り当て'     : 'MemoryGB',
'ESXiホスト'         : 'ESXiHost',
'ストレージ構成'     : 'HDDType',
]
