import logging
from dataset.types import Types
from redminelib import Redmine
from redminelib.exceptions import ResourceNotFoundError
from redminelib.exceptions import ValidationError
from getconfig_cleansing.singleton import singleton
from getconfig_cleansing.util import Util
from getconfig_cleansing.ticket.ticket import Ticket
from getconfig_cleansing.ticket.redmine_field import RedmineField

@singleton
class TicketRelation(Ticket):
    # チケットの題名ヘッダ(必須項目)
    subject_header = 'リレーション'

    # トラッカー名(必須項目)
    tracker_name = 'リレーション'

    # DBキャッシュ用テーブル名,プライマリーキー名,タイプ(何れも必須項目)
    cache_table    = 'relations'
    cache_key_name = 'from_to_ticket_id'
    # cache_key_type = Types.text
    cache_key_type = Types.string(256)

    def regist_relation(self, issue_id, issue_to_id, **kwargs):
        _logger = logging.getLogger(__name__)
        force_update = kwargs.get('force_update', False)
        issue_id     = int(issue_id)
        issue_to_id  = int(issue_to_id)
        if not issue_id > 0:
            raise ValueError("Regist relation invalid issue_id:{}".format(issue_id))
        if not issue_to_id > 0:
            raise ValueError("Regist relation invalid issue_to_id:{}".format(issue_to_id))

        relation = self.cache_db.exist_relation(issue_id, issue_to_id)
        if relation == None or force_update:
            try:
                relation = self.redmine.issue_relation.create(
                    issue_id=issue_id,
                    issue_to_id=issue_to_id,
                    relation_type='relates')
            except ValidationError as e:
                _logger.info('Regist Relation:Validation %d,%d:%s' % (issue_id, issue_to_id, e.args))
            except ResourceNotFoundError as e:
                _logger.error('Regist Relation:NotFound %d,%d:%s' % (issue_id, issue_to_id, e.args))

            self.cache_db.set_relation(issue_id, issue_to_id)

    def get_custom_field_default_value(self, field_name, row):
        pass
