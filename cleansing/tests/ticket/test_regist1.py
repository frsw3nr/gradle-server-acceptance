import pytest
import re
import sys
import os
import pytest
import numpy as np
import pandas as pd
import dataset as ds
from getconfig_cleansing.util import Util
from getconfig_cleansing.config import Config
from getconfig_cleansing.merge_master import MergeMaster
from getconfig_cleansing.ticket.redmine_repository import RedmineRepository
from getconfig_cleansing.ticket.redmine_cache      import RedmineCache
from getconfig_cleansing.ticket.redmine_stat       import RedmineStatistics
from getconfig_cleansing.ticket.ticket_ia_server   import TicketIAServer

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

def test_regist_server1(redmine_repository):
    # マージテスト(test_merge.py)で生成した、host_list.csv を読込み、Redmine に登録する
    if not redmine_repository:
        return

    print (redmine_repository.get_tracker_id('IAサーバ'))
    hosts = pd.read_csv('tests/resources/classify/host_list.csv')
    # 「ホスト名」でグルーピングする
    host_sets = hosts.groupby(by='ホスト名')
    # for hostname, host_set in host_sets.first().iterrows():
    #     row = host_sets.get_group(hostname)
    #     tracker = Util().analogize_tracker(str(row['ドメイン']))
    #     print("サーバ登録 {},{}".format(hostname,tracker))

    row = host_sets.get_group('ostrich')
    print(row.head(1))
    # TicketIAServer().regist('A', 'ostrich', row.head(1))
    #  「インベントリ名」から種別を類推する

    # print(hosts)
    # db = RedmineCache('networks', 'hostname', Types.text)
    # row = dict(text1='text1', num1 = 1)
    # db.regist('host1', row)
    # row2 = [("test1", 'test2'), ("num1", 5)]
    # db.regist('host2', dict(row2))
    
    # host1 = db.get('host1')
    # host2 = db.get('host2')

    # assert host1.get('text1') == 'text1'
    # assert host1.get('num1')  == '1'
    # assert host2.get('test1') == 'test2'
    # assert host2.get('num1')  == '5'
    assert 1 == 1

def test_repos1():
    db = RedmineRepository()
    print (db.get_tracker_id('IAサーバ'))
    print (db.get_tracker_fields('IAサーバ', 'OS名'))
    # print (db.get_tracker_fields_by_id(1, 27))
    # db.set_project_ids()
    print(db.project_ids)
