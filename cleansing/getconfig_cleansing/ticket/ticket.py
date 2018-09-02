"""Redmine 登録処理テンプレート

各トラッカーの Redmine 登録処理モジュールのテンプレートクラス。

* Redmine チケットの更新と更新データのキャッシュを管理します
* Redmine チケット更新前にキャッシュデータを検索し変更がなければ、Redmine更新をスキップします
"""

import math
import logging
import types
from enum import Enum
from datetime import datetime
from abc import ABCMeta, abstractmethod
import numpy as np
import dataset as ds
from dataset.types import Types
from redminelib import Redmine
from redminelib.exceptions import ResourceNotFoundError
from redminelib.exceptions import ValidationError
from redminelib.exceptions import UnknownError
from redminelib.exceptions import ResourceAttrError
from redminelib.exceptions import ServerError
from getconfig_cleansing.config import Config
from getconfig_cleansing.ticket.redmine_repository import RedmineRepository
from getconfig_cleansing.ticket.redmine_cache      import RedmineCache
from getconfig_cleansing.ticket.redmine_stat       import RedmineStatistics

class Ticket(metaclass=ABCMeta):

    class Status(Enum):
        """Redmine チケットステータス。Redmin 環境に合わせてステータスIDをセットする"""
        PLANNNING    = 1
        IN_OPERATION = 4
        UNCLASSY     = 18
        CLASSIFIED   = 19

    subject_header = None
    """チケット題名に表示するヘッダ名(必須項目)。Linux など"""

    tracker_name = None
    """トラッカー名(必須項目)"""

    cache_table    = None
    """DBキャッシュ用テーブル名(必須項目)"""

    cache_key_name = None
    """プライマリーキー名(必須項目)"""

    # cache_key_type = Types.text
    cache_key_type = Types.string(256)
    """プライマリーキータイプ(必須項目)"""

    custom_fields = {}
    """カスタムフィールド(必須項目)。Redmine カスタムフィールドとCSVカラム名のマップリスト"""

    derived_custom_fields = []
    """カスタムフィールド既定値(任意)。Redmine カスタムフィールド規定値のリスト"""

    record_statistics = {
        '成功行数' : 0,
        '失敗行数' : 0,
        'スキップ数' : 0,
        'カラム総数' : 0,
        'カラム登録' : 0,
    }
    """レコード更新処理統計"""

    def __init__(self):
        """
        Redmine チケット登録セッションの初期化
        """

        _logger = logging.getLogger(__name__)
        if self.tracker_name == None or self.cache_table == None or \
           self.cache_key_name == None:
            raise TypeError("Ticket class member not set")
        db = RedmineRepository()
        self.tracker_id = db.get_tracker_id(self.tracker_name)
        _logger.info("Init '{}'".format(self.tracker_name))
        # Redmine カスタムフィールドを取得して、フィールドを初期化
        self.set_custom_fields()
        self.redmine = db.redmine
        self.auth_id = db.redmine.auth().id

        # 題名をキーとしたチケットキャッシュ初期化
        self.cache_db = RedmineCache(self.cache_table,
                                     self.cache_key_name,
                                     self.cache_key_type)
        self.cache_tickets = {}

    def set_custom_fields(self):
        """
        Redmine カスタムフィールドのIDリストと、カスタムフィールドからCSVカラム名の逆引きリストの生成
        """

        self.custom_field_ids = dict()
        self.csv_field_names = dict()
        for field_name, custom_field in self.custom_fields.items():
            field = RedmineRepository().get_tracker_fields(self.tracker_name, field_name)
            if field == None:
                raise TypeError("could not find redmine custom field '{}:{}'".format(self.tracker_name, field_name))
            self.custom_field_ids[field_name] = field['id']
            if isinstance(custom_field.column, str):
                self.csv_field_names[field_name] = custom_field.column

    def get_issue_cache(self, key, **kwargs):
        """
        SQLite3 キャッシュからキーを指定してチケット検索

        :param string key: チケットのキー。ホスト名、IPなど
        :param boolean use_cache: キャッシュを使用するか
        """

        issue_cache = dict()
        if kwargs.get('use_cache', False) and not kwargs.get('force_update', False):
            row = self.cache_db.get(key)
            if row:
                issue_cache = row
        return issue_cache

    def reset_record_statistics(self):
        """
        チケット登録処理統計の初期化
        """

        for metric_name in self.record_statistics.keys():
            if metric_name == 'カラム総数':
                self.record_statistics[metric_name] = len(self.custom_field_ids)
            else:
                self.record_statistics[metric_name] = 0

    def get_record_statistics(self):
        """
        チケット登録処理統計の取得
        """

        return record_statistics

    def validate_custom_fileds(self):
        """
        チケット登録処理統計から、全カスタムフィールドが設定されているかを返す
        """

        total_columns  = self.record_statistics['カラム総数']
        regist_columns = self.record_statistics['カラム登録']
        return (total_columns == regist_columns)

    def get_difference_with_cache(self, issue_cache, csv):
        """
        チケット属性のキャッシュ値とCSV値の比較をする。
        差があれば変更後のdictを返す。差がなければ空のdictを返す。
        キャッシュに差分をアップデートする。

        :param dict issue_cache: キャッシュデータ
        :param dict csv: CSV データ
        """
        diff_fields = dict()
        refresh_required = False
        """
        キャッシュのチケットIDが0の場合は、Redmine 未登録として
        全フィールドを更新対象とする
        """
        if issue_cache.get('id') == 0:
            refresh_required = True
        custom_field_count = 0
        for field_name, csv_field_name in self.csv_field_names.items():
            csv_value = csv.get(csv_field_name)
            if (isinstance(csv_value, float) and math.isnan(csv_value)):
                csv_value = None
            """
            Redmine キャッシュになくて、CSV値がある場合、更新対象とする
            また、CSV値と、キャッシュ値が違っていれば更新対象にする
            """
            is_difference = False
            print("CSV_FIELD_NAME:", csv_field_name)
            cache_value = issue_cache.get(csv_field_name)
            print("CACHE_VALUE:{},{}".format( cache_value, csv_value))
            if cache_value == None and csv_value != None:
                is_difference = True
            if csv_value != None and cache_value != csv_value:
                is_difference = True
            if is_difference:
                diff_fields[csv_field_name] = csv_value
                issue_cache[csv_field_name] = csv_value
            if cache_value or csv_value:
                custom_field_count += 1
        # カラム登録数統計をセット
        self.record_statistics['カラム登録'] = custom_field_count
        return diff_fields

    @abstractmethod
    def get_custom_field_default_value(self, field_name, row, **kwargs):
        pass

    def get_user_updated_fields(self, issue):
        """
        チケットカスタムフィールドのユーザ更新有無をチェックする。
        更新ありのフィールドを抽出し、カスタムフィールドIDをキーにした変更値
        のマップを返す。

        :param Redmine.issue issue: Redmine チケット
        """
        db = RedmineRepository()
        user_updated_fields = dict()
        for journal in issue.journals:
            if self.auth_id == journal.user.id:
                continue
            for detail in journal.details:
                if detail['property'] == 'cf':
                    field_id = int(detail['name'])
                    user_updated_fields[field_id] = detail['new_value']
        return user_updated_fields

    def make_custom_fields(self, row, csv, user_updated_fields = {}):
        """
        登録するチケットのカスタムフィールド値を設定

        :param dict row: CSV データ
        :param dict csv: CSV キャッシュデータ
        :param dict user_updated_fields: ユーザ更新フィールド
        """
        _logger = logging.getLogger(__name__)
        # カスタムフィールド既定値設定
        issue_custom_fields = []
        for field_name, custom_field in self.custom_fields.items():
            value = None
            if custom_field.func is not None:
                value = custom_field.func(field_name, row)
            elif custom_field.default is not None:
                value = custom_field.default

            if value is not None:
                # 導出項目で、CSVが空白のときのみ初期値を設定する
                if csv.get(field_name):
                    continue
                field_id = self.custom_field_ids.get(field_name)
                issue_custom_fields.append({'id': field_id, 'value': value})
                # キャッシュ登録のため、導出値を登録
                csv[field_name] = str(value)
        # カスタムフィールドの登録
        validated = True
        for field_name, custom_field_id in self.custom_field_ids.items():
            custom_field = self.custom_fields.get(field_name)
            csv_field_name = self.csv_field_names.get(field_name)
            value = csv.get(csv_field_name)
            # ユーザ更新ありのカスタムフィールドの場合は更新をスキップする
            if user_updated_fields.get(custom_field_id):
                continue
            # NaN Check
            updated = False
            if (value != None) and \
                (not (isinstance(value, float) and math.isnan(value))):
                issue_custom_fields.append({'id': custom_field_id, 'value': value})
                custom_field_name = self.custom_fields[field_name]
                updated = True
            if custom_field.required and not updated:
                validated = False

        _logger.debug("make field:{}".format(issue_custom_fields))
        return issue_custom_fields, validated

    def regist(self, fab, key, row, **kwargs):
        """
        Redmine チケットの登録。use_cache, skip_redmine オプションの条件により処理が変わる
        条件1 : {use_cache = Yes And skip_redmine = No}
          キャッシュ、Redmine 共に使用
          Redmine 検索。なければ issue 作成。Redmine 更新 して、キャッシュ登録
        条件2 : {use_cache = Yes And skip_redmine = Yes}
          キャッシュは使って、Redmine更新はしない
          Cache, Redmine の順に検索。なければ Redmine 更新はスキップして、キャッシュのみ登録
        条件3 : {use_cache = No  And skip_redmine = No}
          キャッシュは使用せずに、Redmine更新
          Redmine から検索。なければ issue 作成。Redmine 更新 して、キャッシュ登録
        条件4 : {use_cache = No  And skip_redmine = Yes}
          キャッシュ、Redmine 共に使用しない
          エラーとして例外をトラップ

        :param string fab: チケットプロジェクト名
        :param string key: チケットのキー名
        :param dict row: CSV データ
        :param boolean use_cache(Option): SQLite3 キャッシュを使用するか
        :param boolean skip_redmine(Option): Redmine 登録をスキップするか
        """
        _logger = logging.getLogger(__name__)
        use_cache    = kwargs.get('use_cache', False)
        skip_redmine = kwargs.get('skip_redmine', False)
        force_update = kwargs.get('force_update', False)
        if use_cache == False and skip_redmine == True:
            raise TypeError("Invalid parameter {USE_CACHE = No, SKIP_REDMINE = Yes}")
        self.reset_record_statistics()
        _logger.debug("skip_redmine:{}, use_cache:{}".format(skip_redmine, use_cache))
        """
        チケットのキャッシュを検索し、CSVデータと比較
        """
        issue_cache = self.get_issue_cache(key, **kwargs)
        diff_fields = self.get_difference_with_cache(issue_cache, row)
        _logger.debug("比較結果: {}".format(diff_fields))
        """
        更新がない場合はissue_cache を返す
        """
        subject = key
        if self.subject_header:
            subject = '%s - %s' % (self.subject_header, key)
        if not diff_fields and not force_update:
            self.record_statistics['スキップ数'] = 1
            RedmineStatistics().dictionary_count_up(self.tracker_name, self.record_statistics)
            _logger.info('Subject=%s,No update' % (subject))

            return issue_cache

        """
        Redmineスキップしない場合はRedmineからチケット検索
        """
        issue = None
        user_updated_fields = {}
        if skip_redmine == False:
            issues = self.redmine.issue.filter(subject=subject,
                                               tracker_id=self.tracker_id,
                                               status_id='*')
            _logger.info('Subject=%s,Exist=%d' % (subject, len(issues)))
            if len(issues) > 0:
                issue = issues[0]
                user_updated_fields = self.get_user_updated_fields(issue)

        """
        Redmineにチケットがないなら、チケットを一時的に生成。
        Redmine登録するかは後のコードで判断
        """
        if issue == None:
            issue = self.redmine.issue.new()

        """
        プロジェクトが存在しない場合はエラー
        """
        project_id = RedmineRepository().get_project_id(fab)
        if not project_id:
            self.record_statistics['失敗行数'] = 1
            raise ValueError("could not find fab in '{}':{}".format(subject, fab))

        """
        Redmine チケット属性を設定して登録
        チケット優先度は低め、チケットステータスは初期値とする
        """
        issue.project_id = project_id
        issue_cache['project_id'] = project_id
        issue.subject = subject
        issue.tracker_id = self.tracker_id
        issue.priority_id = kwargs.get('priority_id', 1)  # 優先度 低め(1)
        custom_fields, validated = self.make_custom_fields(row, issue_cache, user_updated_fields)
        if custom_fields:
            issue.custom_fields = custom_fields
        if validated:
            issue.status_id  = self.Status.IN_OPERATION.value
        else:
            issue.status_id  = self.Status.PLANNNING.value

        if skip_redmine == False:
            _logger.debug("チケット登録:{}".format(subject))
            is_succeed = False
            issue_cache['Error'] = ''
            try:
                res = issue.save()
                is_succeed = True
                issue_cache['id'] = issue.id
            except ValidationError as e:
                msg = 'Regist Validation %s:%s' % (subject, e.args)
                _logger.warning(msg)
                issue_cache['Error'] = msg
            except (UnknownError, ServerError, ResourceNotFoundError, ResourceAttrError) as e:
                msg = 'Regist Unkown %s:%s' % (subject, e.args)
                _logger.error(msg)
                _logger.error(custom_fields)
                issue_cache['Error'] = msg

            if is_succeed:
                self.record_statistics['成功行数'] = 1
                issue_cache['DML'] = ''
            else:
                self.record_statistics['失敗行数'] = 1
        else:
            """
            キャッシュのみ更新する場合。更新必要を示すDMLカラムを'Y'にする
            """
            issue_cache['DML'] = 'Y'
            self.record_statistics['スキップ数'] = 1

        """
        キャッシュと処理統計を更新
        """
        self.cache_db.regist(key, issue_cache)
        RedmineStatistics().dictionary_count_up(self.tracker_name, self.record_statistics)

        return issue_cache

