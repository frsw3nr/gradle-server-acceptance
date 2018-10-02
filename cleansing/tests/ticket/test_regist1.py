import pytest
import re
import sys
import os
import pytest
import numpy as np
import pandas as pd
import dataset as ds
from getconfig.util import Util
from getconfig.config import Config
from getconfig.merge_master import MergeMaster
from getconfig.ticket.redmine_repository import RedmineRepository
from getconfig.ticket.redmine_cache      import RedmineCache
from getconfig.ticket.redmine_stat       import RedmineStatistics
from getconfig.ticket.template.ticket_ia_server import TicketIAServer

# export REDMINE_API_KEY=作成したAPIキー
# export REDMINE_URL=作成したRedmine環境のURL
# py.test tests/ticket/test_regist1.py -v --capture=no -k test_regist_server1

@pytest.fixture
def redmine_repository():
    redmine_api_key = Config().get_redmine_api_key()
    print("REDMINE : {}".format(redmine_api_key))
    if Config().get_redmine_api_key() == '0000000000000000000000000000000000000000':
        return
    return RedmineRepository()

# def test_regist_group1(redmine_repository):
#     db = redmine_repository.redmine
#     print("REDMINE : ", db)
#     # db.group.create(name='Developers')
#     groups = db.group.all()
#     """Redmine プロジェクトを検索し、プロジェクト名をキーにした配列 project_ids に格納する"""

#     self.user_ids = dict()
#     users = self.redmine.user.all()
#     for user in users:
#         # "場所不明"のプロジェクトが登録できない問題あり
#         # セットするキーをプロジェクト表示名から、 user.identifier に変更
#         identifier = user.identifier
#         self.user_ids[user.name] = identifier
#     print("GROUP : ", groups)
#     """
#     Redmineスキップしない場合はRedmineからチケット検索
#     """
#     issue = None
#     user_updated_fields = {}
#     if skip_redmine == False:
#         issues = self.redmine.issue.filter(subject=subject,
#                                            tracker_id=self.tracker_id,
#                                            status_id='*')
#         _logger.info('Subject=%s,Exist=%d' % (subject, len(issues)))
#         if len(issues) > 0:
#             issue = issues[0]
#             user_updated_fields = self.get_user_updated_fields(issue)

#     """
#     Redmineにチケットがないなら、チケットを一時的に生成。
#     Redmine登録するかは後のコードで判断
#     """
#     if issue == None:
#         issue = self.redmine.issue.new()

#     assert 1 == 1

# def test_regist_server1(redmine_repository):
#     # マージテスト(test_merge.py)で生成した、host_list.csv を読込み、Redmine に登録する
#     if not redmine_repository:
#         return

#     print (redmine_repository.get_tracker_id('IAサーバ'))
#     hosts = pd.read_csv('data/classify/host_list.csv')
#     # 「ホスト名」でグルーピングする
#     host_sets = hosts.groupby(by='ホスト名')
#     for hostname, host_set in host_sets.first().iterrows():
#         if hostname == 'ostrich':
#             print(dict(host_set))
#             TicketIAServer().regist('test1', 'ostrich', host_set)
#             break
#         # row = host_sets.get_group(hostname)
#     #     tracker = Util().analogize_tracker(str(row['ドメイン']))
#     #     print("サーバ登録 {},{}".format(hostname,tracker))

#     # row = host_sets.get_group('ostrich')
#     # print(row.head(1))
#     #  「インベントリ名」から種別を類推する

#     # print(hosts)
#     # db = RedmineCache('networks', 'hostname', Types.text)
#     # row = dict(text1='text1', num1 = 1)
#     # db.regist('host1', row)
#     # row2 = [("test1", 'test2'), ("num1", 5)]
#     # db.regist('host2', dict(row2))
    
#     # host1 = db.get('host1')
#     # host2 = db.get('host2')

#     # assert host1.get('text1') == 'text1'
#     # assert host1.get('num1')  == '1'
#     # assert host2.get('test1') == 'test2'
#     # assert host2.get('num1')  == '5'
#     assert 1 == 1

# def test_repos1():
#     db = RedmineRepository()
#     print (db.get_tracker_id('IAサーバ'))
#     print (db.get_tracker_fields('IAサーバ', 'OS名'))
#     # print (db.get_tracker_fields_by_id(1, 27))
#     # db.set_project_ids()
#     print(db.project_ids)
