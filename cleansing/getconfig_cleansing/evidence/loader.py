
import re
import sys
import os
from enum import Enum
from datetime import datetime
import logging
import numpy as np
import pandas as pd
from abc import ABCMeta, abstractmethod
from getconfig_cleansing.evidence.util import Util
from getconfig_cleansing.evidence.loader_v1 import GetconfigEvidenceV1
from getconfig_cleansing.evidence.inventory import InventoryInfo

class InventoryLoader(object):
    INVENTORY_DIR = 'build'

    def read_inventory_excel(self, inventory):
        _logger = logging.getLogger(__name__)
        xls = pd.ExcelFile(inventory.source)
        if 'チェック対象' in xls.sheet_names or 'Target' in xls.sheet_names:
            return self.read_old_inventory_excel(inventory)

        if not '検査レポート' in xls.sheet_names:
            return (pd.DataFrame(), pd.DataFrame())

        df = xls.parse('検査レポート', skiprows=range(0,2))
        # 先頭列の'No'が整数の行のみを抽出する
        df = df[(df['No'].str.contains('^\d+$', na=False))]
        
        # ネットワーク構成情報から、IPアドレスを抽出
        port_list = pd.DataFrame()
        if pd.Series(['ネットワーク構成']).isin(df.columns).all():
            df2 = Util().expand_ip_address_list(df, 'ネットワーク構成')
            df2['AdminIP'] = False
            port_list = pd.concat([port_list, df2], axis=0)

        # 管理LAN情報から、IPアドレスを抽出
        if pd.Series(['管理LAN']).isin(df.columns).all():
            df2 = Util().expand_ip_address_list(df, '管理LAN')
            df2['AdminIP'] = True
            port_list = pd.concat([port_list, df2], axis=0)

        return df, port_list

    def read_old_inventory_excel(self, inventory):
        _logger = logging.getLogger(__name__)
        db = GetconfigEvidenceV1(inventory.source, export_dir='build/tmp')
        print ("READ_OLD_INVENTORY_EXCEL")
        db.load()
        df = pd.DataFrame()
        port_list = pd.DataFrame()

        return df, port_list
