import re
import sys
import os
import logging
import numpy as np
import pandas as pd
from getconfig.util import Util
from getconfig.stat import Stat
from getconfig.inventory.info import InventoryInfo
# from getconfig.inventory.data import InventoryData
from getconfig.inventory.table import InventoryTableSet
from getconfig.inventory.loader import InventoryLoader

class InventoryCollector(object):
    INVENTORY_DIR = 'build'

    def __init__(self, **kwargs):
        pass

    def set_envoronment(self, env):
        self.inventory_source = env.get_inventory_dir()
        self.result_dir       = env.get_result_dir()
        self.dry_run          = env.get_dry_run()
        self.filter_inventory = env.get_filter_inventory()

    def make_inventory_info(self, excel_path):
        ''' Excel インベントリソースパス名を解析してインベントリ情報を返す '''
        inventory_dir, excel_file = os.path.split(excel_path)
        if not excel_file.endswith('.xlsx') or excel_file.startswith('~'):
            return

        # 'tests/resource'ディレクトリは除外
        match_dir = re.search(r'tests[/|\\]resources', inventory_dir)
        if match_dir:
            return

        # Extract project from '.../{project}/build'
        match_dir = re.search(r'([^/|^\\]+?)[/|\\]build$', inventory_dir)
        if not match_dir:
            return
        project = match_dir.group(1)

        # Extract inventory,timestamp from '{inventory}_{YYYYMMDD_HHMI}.xlsx'
        inventory = excel_file[0:-5]
        match_file = re.match(r'^(.+)_(\d+_\d+)$', inventory)
        timestamp = ''
        if match_file:
            inventory  = match_file.group(1)
            timestamp = match_file.group(2)
        return InventoryInfo(excel_path, inventory, project, timestamp)

    def scan_inventorys(self, source = None):
        _logger = logging.getLogger(__name__)
        inventoris = []
        inventory_source = source or self.inventory_source
        if os.path.isfile(inventory_source):
            inventory = self.make_inventory_info(inventory_source)
            if inventory:
                inventoris.append(inventory)
        elif os.path.isdir(inventory_source):
            for inventory_dir, dirs, files in os.walk(inventory_source):
                for excel_file in files:
                    excel_path = os.path.join(inventory_dir, excel_file)
                    inventory = self.make_inventory_info(excel_path)
                    if inventory:
                        inventoris.append(inventory)
            inventoris.sort(key=lambda x: x.timestamp, reverse=True)
        return inventoris

    def load(self, inventorys):
        _logger = logging.getLogger(__name__)
        loader = InventoryLoader()
        inventory_tables = InventoryTableSet()
        for inventory in inventorys:
            _logger.info("Loading '{}'".format(inventory.source))
            loader.exec_getconfig_loader(inventory)
            loader.import_inventory_sheet(inventory, inventory_tables)
        inventory_tables.reset_host_domains()
        return inventory_tables

