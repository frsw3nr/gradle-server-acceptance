// 検査対象サーバ定義

get_servers_sql ="""\
select
    issues.id,
    custom_fields.name,
    custom_values.value
from
    issues,
    custom_fields,
    custom_values,
    trackers,
    issue_statuses
where
    trackers.id = issues.tracker_id
and issues.id = custom_values.customized_id
and custom_fields.id = custom_values.custom_field_id
and issue_statuses.id = issues.status_id
and issue_statuses.name = '構築前'
and trackers.name = 'Linux'
order by
    issues.id
;
"""

// +----+-------------------------+-------+--------------------------------+--------------+
// | id | subject                 | name  | name                           | value        |
// +----+-------------------------+-------+--------------------------------+--------------+
// | 41 | ostrich(検証用Linux)    | Linux | 種別                           | 基幹         |
// | 41 | ostrich(検証用Linux)    | Linux | ホスト名                       | ostrich      |
// | 41 | ostrich(検証用Linux)    | Linux | IPアドレス                     | 192.168.10.1 |
// +----+-------------------------+-------+--------------------------------+--------------+

// CSV変換マップ

// evidence.csv_item_map = [
//     'サーバ名' :            'server_name',
//     'IPアドレス' :          'ip',
//     'Platform' :            'platform',
//     'OSアカウントID' :      'os_account_id',
//     'vCenterアカウントID' : 'remote_account_id',
//     'VMエイリアス名' :      'remote_alias',
//     '検査ID' :              'verify_id',
//     '比較対象サーバ名' :    'compare_server',
//     'CPU数' :               'NumCpu',
//     'メモリ量' :            'MemoryGB',
//     'ESXi名' :              'ESXiHost',
//     'HDD' :                 'HDDtype',
// ]

server_infos = []
