import re
import sys
import os
from enum import Enum
from datetime import datetime
import logging
import numpy as np
import pandas as pd
from abc import ABCMeta, abstractmethod
from getconfig_cleansing.evidence.inventory import InventoryInfo
from getconfig_cleansing.evidence.merge_master import MergeMaster

class Util(object):
    INVENTORY_DIR = 'build'

    def expand_ip_address_list(self, df, column_name, target_name = 'IP'):
        """
        ネットワーク結果の解析。IPアドレスを分離/抽出し、複数行に分割
        """
        # 先頭、末尾の括弧を取り除く
        df_tmp = df[column_name].str.extract('\[(?P<ips>.+)\]', expand=False)
        # df_tmp = df[column_name]

        # カンマ区切りのIPリストを分割して、複数行に展開
        df_lan = pd.DataFrame()
        df_tmp = df_tmp.str.split(',', expand=True).stack()
        if df_tmp.empty:
            return df_lan
        df_lan[target_name] = df_tmp.str.strip().reset_index(level=1, drop=True)

        # 先頭、末尾の文字列を削除。例： 'eth0:192.168.10.1/24' -> '192.168.10.1'
        df_lan[target_name] = df_lan[target_name].replace('.+:',  '', regex=True)
        df_lan[target_name] = df_lan[target_name].replace('\/.+', '', regex=True)

        # 一時的に作成したipリストを削除してジョインする
        df_lan = df[['ホスト名']].join(df_lan).reset_index(drop=True)
        # ローカルIPを除く
        df_lan = df_lan[df_lan[target_name] != '127.0.0.1']
        # IP欠損行を除く
        df_lan = df_lan.dropna(subset=[target_name])

        return df_lan

        # # 一時的に作成したipリストを削除してジョインする
        # df = df.merge(df_lan, on='ホスト名', how='outer').reset_index(drop=True)

        # # ローカルIPを除く
        # df = df[df[target_name] != '127.0.0.1']
        # # IP欠損行を除く
        # df = df.dropna(subset=[target_name])
        # return df

    IT_EQUIPMENT_HOST_SUFFIX = {
        r'(|-|_)(ILO|iLO|ilo|iLO|iLo).*' : 'ilo',
        r'-crs.*' : 'crs',
        r'-rac.*' : 'rac',
        r'-FW'    : 'FW',
        r'-local' : 'local',
        r'-ce0'   : 'ce0',
        r'-ce1'   : 'ce1',
        r'-ce2'   : 'ce2',
        r'-bge0'  : 'bge0',
        r'-bge1'  : 'bge1',
        r'-bge2'  : 'bge2',
        r'-e1000g0': 'e1000g0',
        r'-e1000g1': 'e1000g1',
        r'-e1000g2': 'e1000g2',
        r'-sc'       : 'sc',
        r'-xscf'     : 'xscf', 
        r'(|-|_)iRMC': 'irmc',
        r'(|-|_)imm' : 'imm',
        r'(|-|_)ipmi': 'ipmi',
        r'(|-|_)(CON|CNT|CTL|con|cnt|ctl|C)0': 'cnt0',
        r'(|-|_)(CON|CNT|CTL|con|cnt|ctl|C)1': 'cnt1',
        r'(|-|_)(CON|CNT|CTL|con|cnt|ctl|C)2': 'cnt2',
        r'ctl0$': 'cnt0',
        r'ctl1$': 'cnt1',
        r'ctl2$': 'cnt2',
    }
    """ホスト名シノニムのサフィックスリスト。
    キーがホスト名のサフィックスで、値は 'IPカテゴリ'列の変換値を設定します。
    例えばホスト名が host01-iLO の場合は、host01 に変換し、'IPカテゴリ'列に(ilo)を登録します
    """

    def delete_the_synonym_hostname_suffix(self, df, column='ホスト名', nocategory=False):
        """
        指定したカラムの文字列のサフィックスを取り除く。
        """
        for suffix, category in self.IT_EQUIPMENT_HOST_SUFFIX.items():
            cond1 = df[column].str.contains(suffix) & \
                   ~(df[column].isnull())
            if not nocategory:
                df.loc[cond1,'IPカテゴリ'] = category
            df.loc[cond1, column] = df.loc[cond1, column].str.replace(suffix, '')
        return df

    def delete_unkown_column(self, df):
        """
        データフレームから未使用カラムを取り除く
        """
        for column in list(df.columns.values):
            if isinstance(column, str) and not re.match(r'Unnamed:(.+)$', column):
                continue
            del df[column]
        return df

    def _save_data(self, data_frame, target_dir, filename):
        """ベース処理用結果ファイルの書き込み。出力先を指定して利用する"""
        logger = logging.getLogger(__name__)
        if not os.path.isdir(target_dir):
            os.makedirs(target_dir)
        target_path = "%s/%s" % (target_dir, filename)
        filename, ext = os.path.splitext(filename)
        data_frame.to_csv(target_path, encoding='utf-8-sig')
        logger.info("結果を '%s' に書き込み %d件" % (target_path, len(data_frame)))

    def save_transfer_data(self, data_frame, filename):
        """変換処理の結果ファイルの書き込み。'build'ディレクトリ下に保存します"""
        target_dir = "build/%s" % (self.fab)
        self._save_data(data_frame, target_dir, filename)

    def save_classify_data(self, data_frame, filename):
        """分類処理の結果ファイルの書き込み。'build/classify'ディレクトリ下に保存します"""
        target_dir = "build/classify"
        self._save_data(data_frame, target_dir, filename)

    def save_asset_data(self, data_frame, filename, fab):
        """保守情報変換処理の結果ファイルの書き込み。'build/{fab} ディレクトリ下に保存します"""
        target_dir = "build/%s" % (fab)
        self._save_data(data_frame, target_dir, filename)
