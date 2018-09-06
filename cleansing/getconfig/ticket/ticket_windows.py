import sys
from dataset.types import Types
from cleansing.util.util import Util
from cleansing.redmine.ticket import Ticket
from cleansing.redmine.redmine_field import RedmineField
from cleansing.singleton import singleton

@singleton
class TicketWindows(Ticket):
    # チケットの題名ヘッダ(必須項目)
    subject_header = 'IAサーバ'

    # トラッカー名(必須項目)
    tracker_name = 'IAサーバー'

    # DBキャッシュ用テーブル名,プライマリーキー名,タイプ(何れも必須項目)
    cache_table    = 'windows'
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
        'IAサーバ種別':         RedmineField(None, default = 'Windows'),
        '管理番号':             RedmineField('管理番号'),
        '管理者初期パスワード': RedmineField('管理者初期パスワード'),
        'CPU数':                RedmineField('ＣＰＵコア数'),
        'メモリGB':             RedmineField('メモリGB'),
        'ストレージ構成':       RedmineField('物理ＤＩＳＫ容量'),
        'ホストグループ':       RedmineField('ホストグループ'),
        'ベンダー':             RedmineField('VENDOR'),
        '引渡月':               RedmineField('搬入日'),
        'システムコード':       RedmineField('システムコード', required = True),
        'Racktables':           RedmineField('ラック番号'),
        'インベントリ情報':     RedmineField('接続機器'),
        'クラス分類':           RedmineField('標準化グループ名'),
        '保守契約終了日':       RedmineField('保守契約終了日'),
        'CarePack保守':         RedmineField(None, Util().analogize_hp_care_pack),
        '発報連絡':             RedmineField(None, Util().analogize_event_monitor_enable),
        'イベント監視元ホスト': RedmineField(None, Util().analogize_zabbix_server),
        'イベント監視設定':     RedmineField(None, Util().analogize_event_monitor_spec),
        '傾向監視元ホスト':     RedmineField(None, Util().analogize_cacti_server),
        '傾向監視設定':         RedmineField(None, Util().analogize_performance_monitor_spec),
    }

    def get_custom_field_default_value(self, field_name, row):
        """
        Redmine カスタムフィールド値の導出
        指定したカスタムフィールド名に対し、導出値を計算する

        :param string field_name: カスタムフィールド名
        :param dict csv: CSV データキャッシュ
        """
        if field_name == '発報連絡':
            return Util().analogize_event_monitor_enable(field_name, row)
        elif field_name == 'CarePack保守':
            return Util().analogize_hp_care_pack(field_name, row)
        elif field_name == 'イベント監視元ホスト':
            return Util().analogize_zabbix_server(field_name, row)
        elif field_name == 'イベント監視設定':
            return Util().analogize_event_monitor_spec(field_name, row)
        elif field_name == '傾向監視元ホスト':
            return Util().analogize_cacti_server(field_name, row)
        elif field_name == '傾向監視設定':
            return Util().analogize_performance_monitor_spec(field_name, row)
        elif field_name == 'IAサーバ種別':
            return 'Windows'
