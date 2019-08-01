import sys
from dataset.types import Types
from getconfig.util import Util
from getconfig.singleton import singleton
from getconfig.ticket.ticket import Ticket
from getconfig.ticket.redmine_field import RedmineField

@singleton
class TicketSoftware(Ticket):
    # チケットの題名ヘッダ(必須項目)
    subject_header = None

    # トラッカー名(必須項目)
    tracker_name = 'ソフトウェア'

    # DBキャッシュ用テーブル名,プライマリーキー名,タイプ(何れも必須項目)
    cache_table    = 'softwares'
    cache_key_name = 'hostname'
    # cache_key_type = Types.text
    cache_key_type = Types.string(256)

    # Redmineカスタムフィールド、CSVカラム名のマッピング
    # 'Redmineカスタムフィールド名' : 'CSVカラム名' の順に記述する
    # CSVカラム名が None 場合は、導出項目となり、
    # get_custom_field_default_value() から値を取得する
    custom_fields = {

        'オーナー情報'     : RedmineField('オーナー情報',   required = '運用'),
        'システム'         : RedmineField('用途',           required = '運用'),
        '型番'             : RedmineField('型番',           required = '運用'),
        '個数'             : RedmineField('個数',           required = '運用'),
        '品名'             : RedmineField('プロダクト名',   required = '運用'),
        'インベントリ'     : RedmineField('ホスト名',       required = '運用'),
    }

    def get_custom_field_default_value(self, field_name, row):
        pass
