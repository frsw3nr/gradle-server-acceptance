import sys
from dataset.types import Types
from getconfig.util import Util
from getconfig.singleton import singleton
from getconfig.ticket.ticket import Ticket
from getconfig.ticket.redmine_field import RedmineField

@singleton
class TicketPortList(Ticket):
    # チケットの題名ヘッダ(必須項目)
    subject_header = None

    # トラッカー名(必須項目)
    tracker_name = 'ポートリスト'

    # DBキャッシュ用テーブル名,プライマリーキー名,タイプ(何れも必須項目)
    cache_table    = 'port_list'
    cache_key_name = 'ip_address'
    # cache_key_type = Types.text
    cache_key_type = Types.string(256)

    # Redmineカスタムフィールド、CSVカラム名のマッピング
    # 'Redmineカスタムフィールド名' : 'CSVカラム名' の順に記述する
    # CSVカラム名が None 場合は、導出項目となり、
    # get_custom_field_default_value() から値を取得する
    custom_fields = {
        '上位/下位'           : RedmineField('上位／下位／etc', required = '運用'),
        'スイッチ名'          : RedmineField('スイッチ名',      required = '運用'),
        'ポート番号'          : RedmineField('ポート名',        required = '運用'),
        'MACアドレス'         : RedmineField('MACアドレス',     required = '運用'),
        'MACアドレスベンダー' : RedmineField('VENDOR',          required = '運用'),
        '管理用'              : RedmineField('AdminIP', Util().analogize_admin_ip,
                                             required = '運用'),
        '台帳つき合わせ'      : RedmineField('台帳つき合わせ', Util().analogize_master_check,
                                             required = '運用'),
        '設置場所'            : RedmineField('設置場所',        required = '運用'),
    }

    def get_custom_field_default_value(self, field_name, row):
        pass
