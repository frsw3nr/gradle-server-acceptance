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
from getconfig_cleansing.evidence.loader import InventoryLoader
from getconfig_cleansing.evidence.merge_master import MergeMaster

class InventoryCollector(object):
    INVENTORY_DIR = 'build'

    def __init__(self, inventory_source = INVENTORY_DIR, **kwargs):
        self.inventory_source = inventory_source

    def make_inventory_from_excel_path(self, excel_path):
        ''' Excel インベントリソースパス名を解析する '''
        inventory_dir, excel_file = os.path.split(excel_path)
        if not excel_file.endswith('.xlsx') or excel_file.startswith('~'):
            return

        # Extract project from '.../{project}/build'
        match_dir = re.search(r'([^/]+?)/build$', inventory_dir)
        project = match_dir.group(1) if match_dir else "_Default"

        # Extract inventory,timestamp from '{inventory}_{YYYYMMDD_HHMI}.xlsx'
        inventory = excel_file[0:-5]
        match_file = re.match(r'^(.+)_(\d+_\d+)$', inventory)
        timestamp = ''
        if match_file:
            inventory  = match_file.group(1)
            timestamp = match_file.group(2)
        return InventoryInfo(excel_path, inventory, project, timestamp)

    def scan_excel_inventory_files(self, inventory_source):
        _logger = logging.getLogger(__name__)
        inventoris = []
        if os.path.isfile(inventory_source):
            inventory = self.make_inventory_from_excel_path(inventory_source)
            if inventory:
                inventoris.append(inventory)
        elif os.path.isdir(inventory_source):
            for inventory_dir, dirs, files in os.walk(inventory_source):
                for excel_file in files:
                    excel_path = os.path.join(inventory_dir, excel_file)
                    inventory = self.make_inventory_from_excel_path(excel_path)
                    if inventory:
                        inventoris.append(inventory)
            inventoris.sort(key=lambda x: x.timestamp, reverse=True)
        return inventoris

    def read_excel_inventory(self, excel_source):
        xls = pd.ExcelFile(excel_source)
        df = self.xls.parse('検査シート', skiprows=range(0,2))
        # 先頭のマスクの掛かった箇所を削除する
        df.drop(range(8))
        df = df.dropna(subset=['ホスト名'])
        # ネットワーク構成情報から、IPアドレスを抽出
        port_list = pd.DataFrame()
        df2 = Util.expand_ip_address_list(df, 'ネットワーク構成')
        if not df2.empty:
            df2['ManagementLAN'] = False
            port_list = pd.concat([port_list, df2], axis=0)
        df3 = Util.expand_ip_address_list(df, '管理LAN')
        if not df3.empty:
            df3['ManagementLAN'] = True
            port_list = pd.concat([port_list, df3], axis=0)
        return df, port_list

    def merge_master_with_inventory(self, source, target, join_key):
        if not target.empty:
            if not source.empty:
                source = MergeMaster().join_by_host(source, target, join_key)
            else:
                source = target
        return source

    def load(self, inventorys, join_key = 'ホスト名'):
        _logger = logging.getLogger(__name__)
        loader = InventoryLoader()
        hosts = pd.DataFrame()
        ports = pd.DataFrame()
        for inventory in inventorys:
            (host_list, port_list) = loader.read_inventory_excel(inventory)
            hosts = self.merge_master_with_inventory(hosts, host_list, join_key)
            ports = self.merge_master_with_inventory(ports, port_list, [join_key, 'IP'])

        return hosts, ports

    def get_module(self, name):
        _logger = logging.getLogger(__name__)
        try:
            mod = __import__('getconfig_cleansing.inventory.parser_' + name,
                             None, None, ['InventoryParser'])
        except ImportError:
            return
        return mod

    def parse(self):
        _logger = logging.getLogger(__name__)
        inventorys = self.list_inventorys()
        for inventory in inventorys:
            match = re.match(r'^(.+)_(\d+_\d+)$', inventory)
            timestamp = ''
            if match:
                inventory = match.group(1)
                timestamp = match.group(2)
            _logger.warn("Check: %s, Date: %s" % (inventory, timestamp))
            mod = self.get_module(inventory)
            if mod:
                mod.InventoryParser().say('Hello')

