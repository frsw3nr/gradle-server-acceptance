"""統計処理管理

SQLite3 を用いて処理統計を管理します。
指定したメトリック名に対して SQLite3 に値を登録します。
Stat().show() メソッドで、登録したメトリックをレポートします。

Example:

.. highlight:: python
.. code-block:: python

    from cleansing.stat import Stat

    Stat().create_report_id()
    # Stat().set_current_report_id()
    Stat().regist('test1', 10)
    print(Stat().get('test1'))
    Stat().regist('test2', 40, 'Module2')
    Stat().show()
"""

import sys
import os
import logging
import datetime
import pandas as pd
import sqlite3
import dataset as ds
from getconfig_cleansing.singleton import singleton

@singleton
class Stat():
    report_id = None

    def __init__(self):
        logger = logging.getLogger(__name__)
        db = ds.connect('sqlite:///report.db')

        jobs = db.create_table('jobs')
        jobs.create_column('created', db.types.datetime)

        metrics = db.create_table('metrics')
        metrics.create_column('job_id', db.types.integer)
        metrics.create_column('metric_name', db.types.text)
        metrics.create_index(['job_id', 'metric_name'])

        self.db = db
        self.set_current_report_id()
        logger.info("Statistic report initialized, report_id=%d" % self.report_id)

    def create_report_id(self):
        logger = logging.getLogger(__name__)
        row = dict(created=datetime.datetime.now())
        self.report_id = self.db['jobs'].insert(row)
        logger.info("Statistic report_id created : %d" % self.report_id)
        return self.report_id

    def set_current_report_id(self):
        logger = logging.getLogger(__name__)
        rows = self.db.query('select max(id) from jobs')
        report_id = None
        for row in rows:
            report_id = row['max(id)']
        if report_id == None:
            report_id = self.create_report_id()
        self.report_id = report_id
        return report_id

    def regist(self, metric_name, value, module=None):
        logger = logging.getLogger(__name__)
        metrics = self.db['metrics']
        row = dict(job_id=self.report_id, module=module, metric_name=metric_name, value=value)
        if metrics.find_one(job_id=self.report_id,
                            module=module,
                            metric_name=metric_name):
            metrics.update(row, ['job_id', 'module', 'metric_name'])
        else:
            metrics.insert(row)
        if module:
            logger.info(" : ".join([module, metric_name, str(value)]))
        else:
            logger.info(" : ".join([metric_name, str(value)]))


    def get(self, metric_name, module=None):
        logger = logging.getLogger(__name__)
        metrics = self.db['metrics']
        row = metrics.find_one(job_id=self.report_id,
                               module=module,
                               metric_name=metric_name)
        if row:
            return row['value']
        else:
            return None

    def show(self, total_number = None, module = None):
        logger = logging.getLogger(__name__)
        logger.info("-" * 15 + " Report ID : "+ str(self.report_id) + "-" * 15)
        metrics = self.db['metrics']
        rows = metrics.find(job_id=self.report_id, order_by='id')
        logger.info("%-5s %-24s %-5s %-6s", 'Module','Metric','Value','Rate')
        for row in rows:
            if row['module'] == None:
                continue
            if module != None and row['module'] != module:
                continue
            values = [row['module'], row['metric_name'], row['value']]
            if total_number:
                rate = 100.0 * row['value'] / total_number
                values.append(rate)
                logger.info("%-5s, %-24s, %5d, %5.2f %%", *values)
            else:
                logger.info("%-5s, %-24s, %5d", *values)

    def show_summary(self, regist_db = False):
        logger = logging.getLogger(__name__)
        logger.info("-" * 15 + " Summry report "+ "-" * 15)
        rows = self.db.query('''
            select metric_name,sum(value) as value
            from metrics
            where job_id = :id
            and module not null
            group by metric_name
        ''', dict(id = self.report_id))
        df = pd.DataFrame()
        for row in rows:
            df = pd.concat([df, pd.DataFrame(row, index=[0])])
            logger.info("%-19s, %5d", row['metric_name'], row['value'])

        if not df.empty and regist_db == True:
            con = sqlite3.connect('report.db')
            df.to_sql('transfer_summary', con, index=False, if_exists='replace')
        return df

if __name__ == '__main__':
    # ログの初期化
    logging.basicConfig(
        level=getattr(logging, 'INFO'),
        format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
        datefmt='%Y/%m/%d %H:%M:%S',
    )
    logger = logging.getLogger(__name__)

    # sqlite3 report.db "select * from jobs"
    Stat().create_report_id()
    # Stat().set_current_report_id()
    Stat().regist('test1', 10)
    Stat().regist('test2', 20)
    Stat().regist('test1', 30, 'Module1')
    Stat().regist('test2', 40, 'Module2')
    print(Stat().get('test1'))
    print(Stat().get('test3'))
    print(Stat().get('test1', 'Module1'))
    print(Stat().get('test3', 'Module2'))
    Stat().show()
    Stat().show(90)
    Stat().show(90, 'Module2')
    Stat().show_summary()

