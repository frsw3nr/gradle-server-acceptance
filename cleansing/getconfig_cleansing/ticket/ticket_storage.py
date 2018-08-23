import sys
from dataset.types import Types
from getconfig_cleansing.util import Util
from getconfig_cleansing.singleton import singleton
from getconfig_cleansing.ticket.ticket import Ticket
from getconfig_cleansing.ticket.redmine_field import RedmineField

@singleton
class TicketStorage(Ticket):
    # チケットの題名ヘッダ(必須項目)
    subject_header = 'ストレージ'

    # トラッカー名(必須項目)
    tracker_name = 'ストレージ'

    # DBキャッシュ用テーブル名,プライマリーキー名,タイプ(何れも必須項目)
    cache_table    = 'storages'
    cache_key_name = 'hostname'
    # cache_key_type = Types.text
    cache_key_type = Types.string(256)

    # Redmineカスタムフィールド、CSVカラム名のマッピング
    # 'Redmineカスタムフィールド名' : 'CSVカラム名' の順に記述する
    # CSVカラム名が None 場合は、導出項目となり、
    # get_custom_field_default_value() から値を取得する
    custom_fields = {
        'ホスト名':             RedmineField('接続機器', required = True),
        '機種':                 RedmineField('機器名称'),
        '製品型番':             RedmineField('機器型番'),
        's/n':                  RedmineField('機器シリアルNo'),
        '固定資産番号':         RedmineField('固定資産番号'),
        '管理部門':             RedmineField('管理部門', required = True),
        '設置場所':             RedmineField('設置場所'),
        'ＯＳ':                 RedmineField('OS', required = True),
        '上位IP':               RedmineField('上位LAN'),
        '下位IP':               RedmineField('下位LAN'),
        '管理番号':             RedmineField('管理番号'),
        'システム名メモ':       RedmineField('システム名メモ'),
        '管理者初期パスワード': RedmineField('管理者初期パスワード'),
        'メモリGB':             RedmineField('メモリGB'),
        '引渡月':               RedmineField('搬入日'),
        'システムコード':       RedmineField('システムコード', required = True),
        'Racktables':           RedmineField('ラック番号'),
        'ホストグループ':       RedmineField('ホストグループ'),
        'インベントリ情報':     RedmineField('接続機器'),
        'クラス分類':           RedmineField('標準化グループ名'),
        '保守契約終了日':       RedmineField('保守契約終了日'),
        '発報連絡':             RedmineField(None, Util().analogize_event_monitor_enable),
        'イベント監視元ホスト': RedmineField(None, Util().analogize_zabbix_server),
        'イベント監視設定':     RedmineField(None, Util().analogize_event_monitor_spec),
        '傾向監視元ホスト':     RedmineField(None, Util().analogize_cacti_server),
        '傾向監視設定':         RedmineField(None, Util().analogize_performance_monitor_spec),
    }

    def get_custom_field_default_value(self, field_name, row):
        pass
