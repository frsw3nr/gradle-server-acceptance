"""Redmine リポジトリ管理

Redmine に接続し、Redmine リポジトリ情報を検索します。

* インスタンス生成時にRedmine に接続し、Redmine リポジトリ情報を検索します。
* 検索結果をハッシュに格納します。
* 検索した結果は、get_XXX() メソッドで検索します。

Example:

.. highlight:: python
.. code-block:: python

    from cleansing.redmine.redmine_repository import RedmineRepository

    print (db.get_tracker_fields_by_id(1, 27))
    print (db.project_ids)
"""
import math
import logging
import sys
from enum import Enum
from datetime import datetime
import dataset as ds
from redminelib import Redmine
from redminelib.exceptions import AuthError
from redminelib.exceptions import ResourceNotFoundError
from redminelib.exceptions import ValidationError
from redminelib.exceptions import UnknownError
from getconfig.config import Config
from getconfig.singleton import singleton

@singleton
class RedmineRepository():

    def __init__(self):
        _logger = logging.getLogger(__name__)

        # Redmine の初期化
        _logger.info("Init RedmineRepository")
        try:
            # redmine_key = Config().get('Redmine','API_KEY')
            # redmine_url = Config().get('Redmine','URL')
            redmine_key = Config().get_redmine_api_key()
            redmine_url = Config().get_redmine_url()
            self.redmine = Redmine(redmine_url, key=redmine_key)
        except KeyError as e:
            _logger.error(e)
            sys.exit("Could not read configfile")
        except AuthError as e:
            _logger.error(e)
            sys.exit("Redmine login failed")

        # プロジェクトリストを取得。使用例: print (self.project_ids['PJ1'])
        self.set_project_ids()
        # トラッカーを取得。使用例: print (self.tracker_ids['IAサーバー'])
        self.set_tracker_ids()
        # トラッカーのカスタムフィールドを取得
        # 使用例: print (self.tracker_fields[('IAサーバー', 'ホストグループ')])
        self.set_tracker_fields()
        # Redmine チケットステータスを取得
        self.set_issue_statuses()

    def set_project_ids(self):
        """Redmine プロジェクトを検索し、プロジェクト名をキーにした配列 project_ids に格納する"""

        _logger = logging.getLogger(__name__)
        self.project_ids = dict()
        try:
            projects = self.redmine.project.all()
            for project in projects:
                # "場所不明"のプロジェクトが登録できない問題あり
                # セットするキーをプロジェクト表示名から、 project.identifier に変更
                identifier = project.identifier
                self.project_ids[project.name] = identifier
        except Exception as e:
            _logger.error('Connection error')
            _logger.error(e)
            raise ValueError("Redmine Connect error")

    def set_tracker_fields(self):
        """Redmine トラッカー、カスタムフィールドを検索し、配列に格納する"""

        self.tracker_fields = dict()
        self.tracker_field_ids = dict()
        fields = self.redmine.custom_field.all()
        for field in fields:
            trackers = field['trackers']
            field_dict = dict(field)
            del field_dict['trackers']
            for tracker in trackers:
                self.tracker_fields[(tracker['name'], field['name'])]  = field_dict
                self.tracker_field_ids[(tracker['id'], field['id'])] = field_dict

    def set_issue_statuses(self):
        """Redmine チケットステータスを検索し、配列に格納する"""

        self.issue_statuses = dict()
        statuses = self.redmine.issue_status.all()
        for status in statuses:
            self.issue_statuses[status.name] = status.id

    def set_tracker_ids(self):
        """Redmine トラッカーを検索し、トラッカー名をキーにした配列 tracker_ids に格納する"""

        self.tracker_ids = dict()
        trackers = self.redmine.tracker.all()
        for tracker in trackers:
            self.tracker_ids[tracker['name']] = tracker['id']

    def get_project_id(self, project_name):
        """プロジェクト名からプロジェクトを検索する"""

        return self.project_ids.get(project_name, None)

    def get_tracker_id(self, tracker_name):
        """トラッカー名からトラッカーを検索する"""

        return self.tracker_ids.get(tracker_name, None)

    def get_tracker_fields(self, tracker_name, field_name):
        """トラッカー名、フィールド名からカスタムフィールドを検索する"""

        return self.tracker_fields.get((tracker_name, field_name), None)

    def get_tracker_fields_by_id(self, tracker_id, field_id):
        """トラッカーID、フィールドIDからカスタムフィールドを検索する"""

        return self.tracker_field_ids.get((tracker_id, field_id), None)

    def get_issue_status(self, status_name):
        """ステータスの初期値を取得する"""
        return self.issue_statuses.get(status_name, None)

if __name__ == '__main__':
    # ログの初期化
    logging.basicConfig(
        level=getattr(logging, 'INFO'),
        format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
        datefmt='%Y/%m/%d %H:%M:%S',
    )
    logger = logging.getLogger(__name__)

    db = RedmineRepository()
    print (db.get_tracker_id('IAサーバ'))
    # print (db.get_tracker_fields('IAサーバー', 'ＯＳ'))
    # print (db.get_tracker_fields_by_id(1, 27))
    # db.set_project_ids()
    print(db.project_ids)
    # db.set_issue_statuses()
    # project_id = RedmineRepository().get_project_id(fab)
    print(db.get_issue_status('計画'))
    # print()