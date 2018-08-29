import re
import sys
import os
import logging
import numpy as np
import pandas as pd
from abc import ABCMeta, abstractmethod
from getconfig_cleansing.util import Util
from getconfig_cleansing.merge_master import MergeMaster
from getconfig_cleansing.inventory.info import InventoryInfo
from getconfig_cleansing.inventory.table import InventoryTableSet
# from getconfig_cleansing.inventory.data import InventoryData
# from getconfig_cleansing.inventory.data_frame import InventoryDataFrame
from getconfig_cleansing.inventory.old.loader_v1 import InventoryLoaderV1

class InventoryLoader(object):
    INVENTORY_DIR = 'build'

    def import_inventory_sheet(self, inventory_info, inventory_tables):
        _logger = logging.getLogger(__name__)
        # print("■チェック対象", inventory_info.source)
        # 旧バージョンのインベントリシート読み込み
        xls = pd.ExcelFile(inventory_info.source)
        if 'チェック対象' in xls.sheet_names or 'Target' in xls.sheet_names:
            InventoryLoaderV1().import_inventory_sheet(inventory_info, inventory_tables)
            return

        if not '検査レポート' in xls.sheet_names:
            return

        df = xls.parse('検査レポート', skiprows=range(0,2))
        df = df.replace('No data', np.NaN)
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

        df['getconfig_name']    = inventory_info.name
        df['getconfig_project'] = inventory_info.project

        inventory_tables.add('host_list', df)
        inventory_tables.add('port_list', port_list, True)

        # ネットワーク ARP 情報インベントリがある場合は、APRインベントリを付加
        sheetname_arp = self.check_sheet_arp_list(xls)
        if sheetname_arp:
            arp_list = self.read_arp_inventory_sheet(xls, sheetname_arp)
            inventory_tables.add('arp_list', arp_list)

    def check_sheet_arp_list(self, xls):
        sheet_name_arp = None
        for sheet_name in xls.sheet_names:
            match = re.match(r"^(.+?)_arp$", sheet_name)
            if match:
                sheet_name_arp = sheet_name
                break

        return sheet_name_arp

    def read_arp_inventory_sheet(self, xls, sheet_name):
        df = xls.parse(sheet_name)
        df.rename(columns = {'target': 'スイッチ名', 'ip': 'IP',
                  'interface': 'ポート名','mac': 'MACアドレス',
                  'vendor': 'ベンダー'
                  }, inplace=True)
        return df
