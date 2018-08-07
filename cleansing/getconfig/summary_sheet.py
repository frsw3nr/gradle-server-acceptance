import re
import sys
import os
import math
import logging
from enum import Enum
from datetime import datetime
from abc import ABCMeta, abstractmethod
import json
import numpy as np
import pandas as pd
import dataset as ds
from dataset.types import Types
from cleansing.config import Config

"""
Getconfig エビデンスサマリーシート検索

使用例：

    python cleansing/getconfig/summary_sheet.py \
        ~/work/gradle/gradle-server-acceptance/build/check_sheet.xlsx
"""

class GetconfigSummarySheet(metaclass=ABCMeta):

    export_dir = 'build'

    def __init__(self, excel_file, **kwargs):
        _logger = logging.getLogger(__name__)
        self.export_dir = kwargs.get('export_dir')
        self.excel_file = excel_file
        self.xls = pd.ExcelFile(excel_file)

    def expand_ip_address_list(self, df, column_name, target_name = 'IP'):
        """
        ネットワーク結果の解析。IPアドレスを分離/抽出し、複数行に分割
        """

        # 先頭、末尾の括弧を取り除く
        df['ip_tmp'] = df[column_name].str.extract('\[(?P<ips>.+)\]', expand=False)

        # カンマ区切りのIPリストを分割して、複数行に展開
        df_lan = pd.DataFrame()
        df_lan[target_name] = df['ip_tmp'].str.split(',', expand=True).stack().str.strip().reset_index(level=1,drop=True)

        # 先頭、末尾の文字列を削除。例： 'eth0:192.168.10.1/24' -> '192.168.10.1'
        df_lan[target_name] = df_lan[target_name].replace('.+:',  '', regex=True)
        df_lan[target_name] = df_lan[target_name].replace('\/.+', '', regex=True)

        # 一時的に作成したipリストを削除してジョインする
        df = df.drop(['ip_tmp'], axis=1).join(df_lan).reset_index(drop=True)

        # ローカルIPを除く
        df = df[df[target_name] != '127.0.0.1']
        # IP欠損行を除く
        df = df.dropna(subset=[target_name])
        return df

    def parse_excel_summary_sheet(self, sheet_name = '検査レポート'):
        """
        検査エビデンスの検査レポートシート読み込み
        """
        _logger = logging.getLogger(__name__)
        _logger.debug("sheet : ", sheet_name)
        df = self.xls.parse(sheet_name, skiprows=range(0,2))
        # 先頭のマスクの掛かった箇所を削除する
        df.drop(range(8))
        df = df.dropna(subset=['ホスト名'])
        # ネットワーク構成情報から、IPアドレスを抽出
        df2 = self.expand_ip_address_list(df, 'ネットワーク構成')
        df2['ManagementLAN'] = False
        df3 = self.expand_ip_address_list(df, '管理LAN')
        df3['ManagementLAN'] = True
        df = pd.concat([df2, df3], axis=0)

        return df

    def load(self):
        """
        Excel の各シートを読み込む
        """
        _logger = logging.getLogger(__name__)
        df_hosts   = pd.DataFrame()
        df_summary = pd.DataFrame()
        df_devices = dict()
        read_phase = None
        _logger.info("Read sheet : {}".format(self.excel_file))
        hw_job, excel_name = db.check_excel_path(self.excel_file)
        print(hw_job)
        print(excel_name)
        df = self.parse_excel_summary_sheet('検査レポート')

        self.df_hosts   = df

    def check_excel_path(self, excel_path):
        match = re.match(r'.+/(.+?)(/build/)(.+\.xlsx)$', excel_path)
        if not match:
            print('Usage python %s filename' % argvs[0])
            quit()
        hw_job = match.group(1)
        excel_name = match.group(3)
        return hw_job, excel_name

if __name__ == '__main__':
    # ログの初期化
    logging.basicConfig(
        level=getattr(logging, 'INFO'),
        format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
        datefmt='%Y/%m/%d %H:%M:%S',
    )
    logger = logging.getLogger(__name__)

    argvs = sys.argv
    if len(argvs) < 2:
        print('Usage python %s filename' % argvs[0])
        quit()
    argvs.pop(0)

    df = pd.DataFrame()
    for excel_path in argvs:
        db = GetconfigSummarySheet(os.path.abspath(excel_path))
        db.load()
        df = pd.concat([db.df_hosts, df])
    print(list(df['ネットワーク構成']))
    df.to_csv('build/getconfig_hosts.csv', encoding='utf-8-sig')
    # # excel_file = 'getconfig/4at00vx_y/build/iLOチェックシート_20170626_140157.xlsx'
    # excel_path = argvs[1]
    # print(excel_path)

    # db = GetconfigGetHosts(excel_path)
    # db.load()
    # db.export()
    # db.analyze_metrics()
    # print(db.df_hosts)
    # print(db.nodes)
    # print(db.export_dir)
    # db.export_json_evidence_summary()
    # db.export_json_evidence_device()
