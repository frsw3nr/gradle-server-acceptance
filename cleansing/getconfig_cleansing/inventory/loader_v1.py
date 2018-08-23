import re
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
from getconfig_cleansing.util import Util

"""
Getconfig エビデンスデータ登録用テンプレートクラス
"""

class GetconfigEvidenceV1(metaclass=ABCMeta):

    export_dir = 'build'

    def __init__(self, excel_file, **kwargs):
        _logger = logging.getLogger(__name__)
        self.export_dir = kwargs.get('export_dir')
        self.excel_file = excel_file
        self.xls = pd.ExcelFile(excel_file)

    def parse_excel_summary_sheet(self, sheet_name):
        """
        検査エビデンスサマリーシート読み込み
        """
        _logger = logging.getLogger(__name__)
        _logger.info("sheet : %s" % (sheet_name))
        df = self.xls.parse(sheet_name, skiprows=range(0,3))
        columns = list(df.columns.values)
        column_heads = []
        for index in range(0, 5):
            column_heads.append(columns.pop(0))
        columns.pop(0)
        hosts = columns
        df = pd.melt(df, id_vars=column_heads, value_vars=hosts)
        df.rename(columns={
                  'ID': 'test_id', 'variable': 'node_name', '分類': 'domain',
                  '項目': 'test_name', 'デバイス': 'device'},
                  inplace=True)
        df = df.dropna(subset=['value'])
        df = Util().delete_the_synonym_hostname_suffix(df, column='node_name', 
                                                             nocategory=True)
        return df

    def analyze_metrics(self):
        """
        読み込んだシート結果から、ドメイン、ノードリスト等のマスターを抽出する
        """
        _logger = logging.getLogger(__name__)
        domains = self.df_summary.groupby(by='domain')
        self.domains = list(domains.count().index)
        nodes = self.df_summary.groupby(by='node_name')
        self.nodes = list(nodes.count().index)

    def parse_excel_device_sheet(self, sheet_name):
        """
        検査エビデンスデバイス(詳細)シート読み込み
        """
        _logger = logging.getLogger(__name__)
        df = self.xls.parse(sheet_name, skiprows=range(0,3))
        del df['Unnamed: 0']
        del df['Unnamed: 1']
        df.rename(columns={'server_name': 'node_name'}, inplace=True)
        df = df.fillna("NaN")
        df = Util().delete_the_synonym_hostname_suffix(df, column='node_name', 
                                                             nocategory=True)
        return df

    def load(self):
        """
        Excel の各シートを読み込む
        """
        _logger = logging.getLogger(__name__)
        df_summary = pd.DataFrame()
        df_devices = dict()
        read_phase = None
        _logger.info("Read sheet : {}".format(self.excel_file))
        for sheet_name in self.xls.sheet_names:
            if sheet_name == 'チェック対象' or sheet_name == 'Target':
                read_phase = 'Summary'
                continue
            if sheet_name == '検査ルール' or sheet_name == 'Rule':
                read_phase = 'Device'
                continue
            if read_phase == 'Summary':
                _logger.debug("サマリシート読み込み:{}".format(sheet_name))
                df = self.parse_excel_summary_sheet(sheet_name)
                df_summary = pd.concat([df_summary, df])
            elif read_phase == 'Device':
                _logger.debug("詳細シート読み込み:{}".format(sheet_name))
                df = self.parse_excel_device_sheet(sheet_name)
                if not df_devices.get(sheet_name):
                    df_devices[sheet_name] = pd.DataFrame()
                df_devices[sheet_name] = pd.concat([df_devices[sheet_name], df])
        self.df_summary = df_summary
        self.df_devices = df_devices

    def write_json_evidence_summary(self, domain, node_name, dict):
        """
        エビデンスサマリシートのロード結果をJSONファイルに変換して保存する
        """
        _logger = logging.getLogger(__name__)
        target_dir = os.path.join(self.export_dir, domain)
        json_file  = "{}.json".format(node_name)
        json_path  = os.path.join(target_dir, json_file)
        if not os.path.isdir(target_dir):
            os.makedirs(target_dir)
        
        _logger.info("Put json : {}/{}".format(domain, json_file))
        with open(json_path,'w') as f:
            json.dump(dict, f, ensure_ascii=False, indent=4, sort_keys=True)

    def write_json_evidence_detail(self, domain, node_name, metric_name, dict):
        """
        エビデンス詳細シートのロード結果をJSONファイルに変換して保存する
        """
        _logger = logging.getLogger(__name__)
        target_dir = os.path.join(self.export_dir, domain, node_name)
        json_file  = "{}.json".format(metric_name)
        json_path  = os.path.join(target_dir, json_file)
        _logger.info("Put json : {}/{}/{}".format(domain, node_name, json_file))
        if not os.path.isdir(target_dir):
            os.makedirs(target_dir)
        
        with open(json_path, 'w') as f:
            json.dump(dict, f, ensure_ascii=False, indent=4, sort_keys=True)

    def export_json_evidence_summary(self):
        """
        エビデンスサマリシートの全ロード結果をJSONファイルに変換して保存する
        """
        df = self.df_summary.groupby(by=['domain', 'node_name'])
        for idx, set in df.first().iterrows():
            (domain, node_name) = idx
            metrics = df.get_group(idx)
            # 取得した index を再構築し、"Try using .loc" エラーメッセージ回避
            metrics = metrics.dropna(subset=['value'])
            metrics.reset_index()
            metrics['domain'] = domain
            metrics = metrics.loc[:, ['test_id', 'domain', 'value']]
            metrics_dict = metrics.to_dict('records')
            self.write_json_evidence_summary(domain, node_name, metrics_dict)

    def export_json_evidence_device(self):
        """
        デバイス詳細シートの全ロード結果をJSONファイルに変換して保存する
        """
        for sheet_name, df_summary in self.df_devices.items():
            match = re.match(r"^(.+?)_(.+)$", sheet_name)
            if not match:
                continue
            domain = match.group(1)
            metric_name = match.group(2)

            df = df_summary.groupby(by=['node_name'])
            for node_name, set in df.first().iterrows():
                metrics = df.get_group(node_name)
                metrics_dict = metrics.to_dict('records')
                self.write_json_evidence_detail(domain, node_name, metric_name,
                                                metrics_dict)

    def export(self):
        self.export_json_evidence_summary()
        self.export_json_evidence_device()

if __name__ == '__main__':
    # ログの初期化
    logging.basicConfig(
        level=getattr(logging, 'INFO'),
        format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
        datefmt='%Y/%m/%d %H:%M:%S',
    )
    logger = logging.getLogger(__name__)

    excel_file = 'tests/resources/import/old1/build/check_sheet_20170512_143424.xlsx'
    db = GetconfigEvidenceV1(excel_file, export_dir='build/tmp')
    db.load()
    db.export()
    # db.analyze_metrics()
    # print(db.domains)
    # print(db.nodes)
    # print(db.export_dir)
    # db.export_json_evidence_summary()
    # db.export_json_evidence_device()
