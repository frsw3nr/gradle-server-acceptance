import sys
from dataset.types import Types
from getconfig.util import Util
from getconfig.singleton import singleton
from getconfig.ticket.ticket import Ticket
from getconfig.ticket.redmine_field import RedmineField

@singleton
class TicketNetwork(Ticket):
    # チケットの題名ヘッダ(必須項目)
    subject_header = None

    # トラッカー名(必須項目)
    tracker_name = 'ネットワーク'

    # DBキャッシュ用テーブル名,プライマリーキー名,タイプ(何れも必須項目)
    cache_table    = 'networks'
    cache_key_name = 'hostname'
    # cache_key_type = Types.text
    cache_key_type = Types.string(256)

    # Redmineカスタムフィールド、CSVカラム名のマッピング
    # 'Redmineカスタムフィールド名' : 'CSVカラム名' の順に記述する
    # CSVカラム名が None 場合は、導出項目となり、
    # get_custom_field_default_value() から値を取得する
    custom_fields = {

        'OS名'                : RedmineField('OS名',             required = '運用'),
        'システム'            : RedmineField('用途',             required = '運用'),
        '機種'                : RedmineField('機種',             required = '運用'),
        '型番'                : RedmineField('型番',             required = '運用'),
        'インベントリ'        : RedmineField('インベントリ名',   required = '運用'),
        '監視テンプレート'    : RedmineField('テンプレート',     required = '運用'),
        '搬入日'              : RedmineField('搬入日',           required = '運用'),
        '引渡し日'            : RedmineField('納期',             required = '運用'),
        '保守契約情報'        : RedmineField('保守契約情報',     required = '運用'),
        'S/N'                 : RedmineField('S/N',              required = '運用'),
        'システムコード'      : RedmineField('システムコード',   required = '運用'),
        'ラック番号'          : RedmineField('ラック番号',       required = '運用'),
        '上位/下位'           : RedmineField('上位/下位',        required = '運用'),
        'ネットワーク分類'    : RedmineField('ネットワーク分類', required = '運用'),
        '設置場所'            : RedmineField('設置場所',         required = '運用'),
    }

    def get_custom_field_default_value(self, field_name, row):
        pass
