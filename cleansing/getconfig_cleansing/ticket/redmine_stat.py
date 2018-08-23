"""Redmine 登録バッチスクリプト用処理統計管理

Redmine 登録処理件数をカウントし、レポートします。

* Redmine 登録バッチスクリプト regist.py の後処理で、Redmine 登録処理件数をレポートします
* 各Redmine 登録クラスで処理件数をカウントします

Example:

.. highlight:: python
.. code-block:: python

    from cleansing.redmine.redmine_stat import RedmineStatistics

    db = RedmineStatistics()
    db.reset()
    db.count_up('IAサーバー', '成功行数', 1)
    record_statistics = {
        '成功行数' : 1,
        '失敗行数' : 0,
        'スキップ数' : 0,
        'カラム総数' : 5,
        'カラム登録' : 5,
    }
    db.dictionary_count_up('Windowsサーバー', record_statistics)
    db.show()
"""
import math
import logging
import sys
import pandas as pd
import sqlite3
from cleansing.config import Config
from cleansing.singleton import singleton

@singleton
class RedmineStatistics():

    def __init__(self):
        _logger = logging.getLogger(__name__)
        self.redmine_statistics = dict()

    def reset(self):
        for (ticket_name, metric_name), row in self.redmine_statistics.items():
            self.redmine_statistics[(ticket_name, metric_name)] = 0

    def show(self):
        _logger = logging.getLogger(__name__)
        con = sqlite3.connect('report.db')
        _logger.info("-" * 10 + " Redmine Registration Summary " + "-" * 10)
        df = pd.DataFrame(self.redmine_statistics, index=[0]).T
        if df.empty:
            _logger.info("Empty DataFrame")
            return
        df = df.reset_index()
        df.columns = ['種別', '項目', '値']
        df = pd.pivot_table(df, 
                             index=['種別'],
                             columns = ['項目'],
                             values='値',
                             fill_value=0)
        df=df.reset_index()
        df['詳細度'] = df[u'カラム登録']/df[u'カラム総数']
        _logger.info("\n{}\n".format(df))
        df.to_sql('regist_summary', con, index=False, if_exists='replace')

    def count_up(self, ticket_name, metric_name, value):
        if (ticket_name, metric_name) in self.redmine_statistics:
            self.redmine_statistics[(ticket_name, metric_name)] += value
        else:
            self.redmine_statistics[(ticket_name, metric_name)] = value

    def dictionary_count_up(self, ticket_name, statistics):
        for metric_name, value in statistics.items():
            self.count_up(ticket_name, metric_name, value)

if __name__ == '__main__':
    # ログの初期化
    logging.basicConfig(
        level=getattr(logging, 'INFO'),
        format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
        datefmt='%Y/%m/%d %H:%M:%S',
    )
    logger = logging.getLogger(__name__)

    db = RedmineStatistics()
    db.reset()
    db.count_up('IAサーバー', '成功行数', 1)
    db.count_up('Windowsサーバー', 'カラム総数', 5)
    db.count_up('Windowsサーバー', '成功行数', 1)

    record_statistics = {
        '成功行数' : 1,
        '失敗行数' : 0,
        'スキップ数' : 0,
        'カラム総数' : 5,
        'カラム登録' : 5,
    }
    db.dictionary_count_up('Windowsサーバー', record_statistics)
    db.show()
