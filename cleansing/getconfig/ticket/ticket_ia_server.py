import sys
from dataset.types import Types
from getconfig.util import Util
from getconfig.singleton import singleton
from getconfig.ticket.ticket import Ticket
from getconfig.ticket.redmine_field import RedmineField

@singleton
class TicketIAServer(Ticket):
    # チケットの題名ヘッダ(必須項目)
    subject_header = None

    # トラッカー名(必須項目)
    tracker_name = 'IAサーバ'

    # DBキャッシュ用テーブル名,プライマリーキー名,タイプ(何れも必須項目)
    cache_table    = 'hosts'
    cache_key_name = 'hostname'
    # cache_key_type = Types.text
    cache_key_type = Types.string(256)

    # Redmineカスタムフィールド、CSVカラム名のマッピング
    # 'Redmineカスタムフィールド名' : 'CSVカラム名' の順に記述する
    # CSVカラム名が None 場合は、導出項目となり、
    # get_custom_field_default_value() から値を取得する
    custom_fields = {

        'オーナー情報'        : RedmineField('オーナー情報'),
        'プラットフォーム'    : RedmineField('プラットフォーム', Util().analogize_platform),
        'OS名'                : RedmineField('OS名'),
        'システム'            : RedmineField('用途'),
        '機種'                : RedmineField('機種'),
        '型番'                : RedmineField('型番'),
        'CPU数'               : RedmineField('CPU数'),
        'MEM容量'             : RedmineField('MEM容量'),
        # 'ディスク構成'        : RedmineField('ディスク構成'),
        'インベントリ'        : RedmineField('インベントリ名'),
        'ホストグループ'      : RedmineField('ホストグループ'),
        '監視テンプレート'    : RedmineField('テンプレート'),
        '搬入日'              : RedmineField('搬入日'),
        '引渡し日'            : RedmineField('納期'),
        '保守契約情報'        : RedmineField('保守契約情報'),
        'S/N'                 : RedmineField('S/N'),
        'システムコード'      : RedmineField('システムコード'),
        'ラック番号'          : RedmineField('ラック番号'),
        # # '個数'                : RedmineField('個数'),
        # '発番'                : RedmineField('発番'),
        # '品名'                : RedmineField('品名'),
        # '接続IP'              : RedmineField('接続IP'),
        # 'スイッチ名'          : RedmineField('スイッチ名'),
        # '上位/下位'           : RedmineField('上位/下位'),
        # 'ポート番号'          : RedmineField('ポート番号'),
        # 'ネットワーク分類'    : RedmineField('ネットワーク分類'),
        # 'MACアドレス'         : RedmineField('MACアドレス'),
        # 'MACアドレスベンダー' : RedmineField('MACアドレスベンダー'),
        # '台帳つき合わせ'      : RedmineField('台帳つき合わせ'),
        '設置場所'            : RedmineField('設置場所'),
        # '管理者パスワード'    : RedmineField('管理者パスワード'),


        # 'ホスト名':             RedmineField('接続機器', required = True),
        # '機種':                 RedmineField('機器名称'),
        # '製品型番':             RedmineField('機器型番'),
        # 's/n':                  RedmineField('機器シリアルNo'),
        # '固定資産番号':         RedmineField('固定資産番号'),
        # '管理部門':             RedmineField('管理部門', required = True),
        # '設置場所':             RedmineField('設置場所'),
        # 'ＯＳ':                 RedmineField('OS', required = True),
        # '上位IP':               RedmineField('上位LAN'),
        # '下位IP':               RedmineField('下位LAN'),
        # 'IAサーバ種別':         RedmineField(None, Util().analogize_ia_server_type),
        # '管理番号':             RedmineField('管理番号'),
        # '管理者初期パスワード': RedmineField('管理者初期パスワード'),
        # 'CPU数':                RedmineField('ＣＰＵコア数'),
        # 'メモリGB':             RedmineField('メモリGB'),
        # 'ストレージ構成':       RedmineField('物理ＤＩＳＫ容量'),
        # 'ホストグループ':       RedmineField('ホストグループ'),
        # 'ベンダー':             RedmineField('VENDOR'),
        # '引渡月':               RedmineField('搬入日'),
        # 'システムコード':       RedmineField('システムコード', required = True),
        # 'Racktables':           RedmineField('ラック番号'),
        # 'インベントリ情報':     RedmineField('接続機器'),
        # 'クラス分類':           RedmineField('標準化グループ名'),
        # '保守契約終了日':       RedmineField('保守契約終了日'),
        # 'CarePack保守':         RedmineField(None, Util().analogize_hp_care_pack),
        # '発報連絡':             RedmineField(None, Util().analogize_event_monitor_enable),
        # 'イベント監視元ホスト': RedmineField(None, Util().analogize_zabbix_server),
        # 'イベント監視設定':     RedmineField(None, Util().analogize_event_monitor_spec),
        # '傾向監視元ホスト':     RedmineField(None, Util().analogize_cacti_server),
        # '傾向監視設定':         RedmineField(None, Util().analogize_performance_monitor_spec),
    }

    def get_custom_field_default_value(self, field_name, row):
        pass