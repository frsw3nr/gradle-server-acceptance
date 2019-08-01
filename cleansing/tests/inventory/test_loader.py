import re
import sys
import os
import pytest
import numpy as np
import pandas as pd
from pprint import pprint
from getconfig.config import Config
from getconfig.inventory.collector import InventoryCollector
from getconfig.inventory.table import InventoryTableSet
from getconfig.inventory.loader import InventoryLoader

# py.test tests/inventory/test_loader.py -v --capture=no -k test_load_single_inventory2

@pytest.mark.parametrize('path,n_host_list,n_port_list,n_arp_list', [
(
    'data/import/project1/build/サーバチェックシート_20180817_142016.xlsx',
    3,
    5,
    0
),
(
    'data/import/project1/build/iLOチェックシート_20180817_092416.xlsx',
    1,
    3,
    0
),
(
    'data/import/project2/build/Zabbix監視設定チェックシート_20180820_112635.xlsx',
    3,
    0,
    0
),
(
    'data/import/project4/build/ETERNUSチェックシート_20180820_115934.xlsx',
    1,
    2,
    0
),
(
    'data/import/old1/build/check_sheet_20170512_143424.xlsx',
    2,
    1,
    0
),
(
    'data/import/net1/build/rtx_check_sheet.xlsx',
    1,
    0,
    14
),
(
    'data/import/project3/build/Oracle設定チェックシート_20180830_090010.xlsx',
    2,
    0,
    0
)
])
def test_load_single_inventory(path, n_host_list, n_port_list, n_arp_list):
    collector = InventoryCollector()
    # path = 'data/import/project1/build/サーバチェックシート_20180817_142016.xlsx'
    inventory = collector.make_inventory_info(path)
    inventory_tables = InventoryTableSet()
    InventoryLoader().import_inventory_sheet(inventory, inventory_tables)
    inventory_tables.print()

    assert len(inventory_tables.get('host_list')) == n_host_list # 3
    assert len(inventory_tables.get('port_list')) == n_port_list # 5
    assert len(inventory_tables.get('arp_list'))  == n_arp_list  # 0

def test_load_single_inventory2():
    # iLOチェックシート読み込みで、'unexpected tag'エラー発生.
    # 該当Excel を開いて上書きしたら解消した
    # Exception: unexpected tag '{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ext'
    collector = InventoryCollector()
    path = 'data/import/project3/build/Oracle設定チェックシート_20180830_090010.xlsx'
    inventory = collector.make_inventory_info(path)
    inventory_tables = InventoryTableSet()
    InventoryLoader().import_inventory_sheet(inventory, inventory_tables)
    inventory_tables.print()
    df = inventory_tables.get('host_list')
    print(df[df['ホスト名'] == 'ora11'].T)
    assert len(inventory_tables.get('host_list')) == 2
    assert len(inventory_tables.get('port_list')) == 0

# def test_load_single_inventory3():
#     collector = InventoryCollector()
#     path = 'data/import/project2/build/Zabbix監視設定チェックシート_20180820_112635.xlsx'
#     inventory = collector.make_inventory_info(path)
#     inventory_tables = InventoryTableSet()
#     InventoryLoader().import_inventory_sheet(inventory, inventory_tables)
#     inventory_tables.print()

#     assert len(inventory_tables.get('host_list')) == 3
#     assert len(inventory_tables.get('port_list')) == 0

# def test_load_single_inventory4():
#     collector = InventoryCollector()
#     path = 'data/import/project4/build/ETERNUSチェックシート_20180820_115934.xlsx'
#     inventory = collector.make_inventory_info(path)
#     inventory_tables = InventoryTableSet()
#     InventoryLoader().import_inventory_sheet(inventory, inventory_tables)
#     inventory_tables.print()

#     assert len(inventory_tables.get('host_list')) == 1
#     assert len(inventory_tables.get('port_list')) == 2

# def test_load_single_inventory5():
#     collector = InventoryCollector()
#     path = 'data/import/empty.xlsx'
#     inventory = collector.make_inventory_info(path)
#     inventory_tables = InventoryTableSet()
#     InventoryLoader().import_inventory_sheet(inventory, inventory_tables)
#     inventory_tables.print()

#     assert len(inventory_tables.get('host_list')) == 0
#     assert len(inventory_tables.get('port_list')) == 0

# def test_load_single_inventory6():
#     # 古いインベントリシートの読み込み
#     collector = InventoryCollector()
#     path = 'data/import/old1/build/check_sheet_20170512_143424.xlsx'
#     inventory = collector.make_inventory_info(path)
#     inventory_tables = InventoryTableSet()
#     InventoryLoader().import_inventory_sheet(inventory, inventory_tables)
#     inventory_tables.print()

#     assert len(inventory_tables.get('host_list')) == 2
#     assert len(inventory_tables.get('port_list')) == 1

# def test_load_single_inventory7():
#     collector = InventoryCollector()
#     path = 'data/import/net1/build/rtx_check_sheet.xlsx'
#     inventory = collector.make_inventory_info(path)
#     inventory_tables = InventoryTableSet()
#     InventoryLoader().import_inventory_sheet(inventory, inventory_tables)
#     inventory_tables.print()

#     assert len(inventory_tables.get('host_list')) == 1
#     assert len(inventory_tables.get('port_list')) == 0
#     assert len(inventory_tables.get('arp_list'))  == 14

