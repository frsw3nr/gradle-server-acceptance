import re
import sys
import os
import math
from enum import Enum
import datetime
import logging
import numpy as np
import pandas as pd
from abc import ABCMeta, abstractmethod
from getconfig.singleton import singleton
from getconfig.merge_master import MergeMaster
from getconfig.inventory.info import InventoryInfo

@singleton
class Util(object):
    INVENTORY_DIR = 'build'

    def expand_ip_address_list(self, df, column_name, target_name = 'IP'):
        """
        ネットワーク結果の解析。IPアドレスを分離/抽出して置換する。
        複数行に分割したデータフレームを返す
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
            cond1 = (df[column].str.contains(suffix) & \
                               ~(df[column].isnull()))
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

    def save_data(self, data_frame, target_dir, filename):
        """ベース処理用結果ファイルの書き込み。出力先を指定して利用する"""
        logger = logging.getLogger(__name__)
        if not os.path.isdir(target_dir):
            os.makedirs(target_dir)
        target_path = "%s/%s" % (target_dir, filename)
        filename, ext = os.path.splitext(filename)
        data_frame.to_csv(target_path, encoding='utf-8-sig', index=False)
        logger.info("結果を '%s' に書き込み %d件" % (target_path, len(data_frame)))

    def save_transfer_data(self, data_frame, filename):
        """変換処理の結果ファイルの書き込み。'build'ディレクトリ下に保存します"""
        target_dir = "build/%s" % (self.fab)
        self.save_data(data_frame, target_dir, filename)

    def save_classify_data(self, data_frame, filename):
        """分類処理の結果ファイルの書き込み。'build/classify'ディレクトリ下に保存します"""
        target_dir = "build/classify"
        self.save_data(data_frame, target_dir, filename)

    def save_asset_data(self, data_frame, filename, fab):
        """保守情報変換処理の結果ファイルの書き込み。'build/{fab} ディレクトリ下に保存します"""
        target_dir = "build/%s" % (fab)
        self.save_data(data_frame, target_dir, filename)

    # "YYYY年MM月"の日付を"YYYY-MM-DD"形式で変換する
    def reform_date(self, in_date):
        if in_date == None:
            return
        if (isinstance(in_date, float) and math.isnan(in_date)):
            return

        if (isinstance(in_date, (int, float))):
            # return(datetime(1899, 12, 30) + datetime.timedelta(days=in_date))
            in_date = datetime.datetime(1899, 12, 30)+ datetime.timedelta(days=in_date)
            return in_date.strftime('%Y-%m-%d')

        if (isinstance(in_date, datetime.datetime) or isinstance(in_date, datetime.time)):
            if str(in_date) == "NaT":
                return
            return in_date.strftime('%Y-%m-%d')

        yymm = re.findall(r'^(\d{2,4})\/(\d+)\/(\d+)$' , in_date)
        if yymm:
            return("%s-%s-%s" % (yymm[0]))
        yymm2 = re.findall(r'^(\d{2,4})年(\d+)月$' , in_date)
        if yymm2:
            return("%s-%s-1" % (yymm2[0]))

    def analogize_tracker(self, domain):
        if re.search("(Linux|Windows)", domain):
            return 'IAサーバ'
        elif re.search("(Solaris)", domain):
            return 'SPARCサーバ'
        elif re.search("(AIX)", domain):
            return 'POWERサーバ'
        elif re.search("(HitachiVSP|Eternus)", domain):
            return 'ストレージ'
        elif re.search("(Router)", domain):
            return 'ネットワーク'
        elif re.search("(Oracle)", domain):
            return 'ソフトウェア'
        else:
            return None

    def analogize_site(self, site, default_site = '場所不明'):
        return site if isinstance(site, str) else default_site
        # return 'Y6'
        # :
        #     return str
        # if site == None or math.isnan(site):
        #     site = '場所不明'
        # print("サイト：", site, ",", type(site))
        # return site

    def analogize_platform(self, field_name, row):
        return 'オンプレ'

    def analogize_master_check(self, field_name, row):
        return False

    def analogize_admin_ip(self, field_name, row):
        return row.get('AdminIP') == True

if __name__ == '__main__':
    # sqlite3 report.db "select * from jobs"
    print(Util().reform_date('1998年?月'))
    print(Util().reform_date('1998年12月'))
    print(Util().analogize_tracker("'Linux'"))
