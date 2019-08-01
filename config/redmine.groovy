/*
変更仕様
--------

Redmine Issue抽出
    Redmineチケットから検査対象のプロパティ(カスタムフィールド)を読み込む
        標準シートのみ
        getconfig -m --status='構築前' --tracker='DB' --version='17上'
    設定ファイル
        config/redmine.config
            redmine.filter.status  = '構築前'
            redmine.filter.tracker = 'DB'
            redmine.filter.version = '17上'
            redmine.custom_items = ['ホスト名' : 'server_name']
    フィルター
        select * from issues
            where
                issue_statuses.name
                trackers.name
                versions.name
        カスタムフィールド検索

変更箇所
--------

変更フロー
    TestRunner
        パーサー(--redmine(-m)オプション)
    TestScheduler
        テストスケジューラ
            EvidenceSheet の読込み(検査対象も読み込む)
            一旦、検査対象をリセット
            Redmineチケットから検査対象読み込み
モジュール
    EvidenceSheet
        Execel管理(読み込み、作成)
            変更なし
    CMDBModel
        Redmine検索SQLメソッド追加
            Redmine 設定ファイル読み込み
        どのクラスからでも呼べるようにする
            TestSchedulerの初期化でCMDBも初期化する
            STANDALONEモードでない場合
    EvidenceManager
        CMDB接続情報など設定セット
            変更なし

ToDo
----

 - [ ] redmine dbテストデータ登録
 - [ ] CMDBModel変更
 - [ ] TestRunner パーサ
 - [ ] TestScheduler run()
*/


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

// redmine dbテストデータ登録
// --------------------------

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
