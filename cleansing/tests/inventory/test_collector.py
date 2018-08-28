import re
import sys
import os
import pytest
import numpy as np
import pandas as pd
from pprint import pprint
from getconfig_cleansing.cli import cli
from getconfig_cleansing.config import Config
from getconfig_cleansing.inventory.collector import InventoryCollector
from getconfig_cleansing.inventory.table import InventoryTableSet
from getconfig_cleansing.inventory.loader import InventoryLoader

# py.test tests/inventory/test_collector.py -v --capture=no -k test_parse_evidence_from_excel_file1

def test_parse_evidence_from_excel_file1():
    inventory = InventoryCollector().make_inventory_info('')
    assert inventory == None

@pytest.mark.parametrize('path,name,project,timestamp', [
    ('/project1/build/サーバチェックシート_20180817_092416.xlsx',
        'サーバチェックシート', 'project1', '20180817_092416'),
    ('/project1/build/check_sheet.xlsx',
        'check_sheet', 'project1', ''),
    ('/check_sheet.xlsx',
        'check_sheet', '_Default', ''),
])
def test_parse_evidence_from_excel_file2(path, name, project, timestamp):
    inventory = InventoryCollector().make_inventory_info(path)
    assert inventory.source    == path
    assert inventory.name      == name
    assert inventory.project   == project
    assert inventory.timestamp == timestamp

def test_parse_evidence_from_excel_dir1():
    collector = InventoryCollector()
    path = 'tests/resources/import/project1/build/サーバチェックシート_20180817_142016.xlsx'
    inventorys = collector.scan_inventorys(path)
    assert len(inventorys) == 1

def test_parse_evidence_from_excel_dir2():
    collector = InventoryCollector()
    inventorys = collector.scan_inventorys('tests/resources/import')
    assert len(inventorys) > 1

def test_parse_evidence_from_excel_dir3():
    collector = InventoryCollector()
    inventorys = collector.scan_inventorys('hoge')
    assert not inventorys

def test_load_multiple_inventory1():
    collector = InventoryCollector()
    inventorys = collector.scan_inventorys('tests/resources/import/project1')
    inventory_tables = collector.load(inventorys)
    # inventory_tables.print()
    assert len(inventory_tables.get('host_list')) == 3# n_host_list # 3
    assert len(inventory_tables.get('port_list')) == 6# n_port_list # 5
    # assert inventory_dat.count() == 3
    # assert inventory_dat.port_count() == 6

def test_load_multiple_inventory2():
    collector = InventoryCollector()
    inventorys = collector.scan_inventorys('tests/resources/import/')
    inventory_tables = collector.load(inventorys)
    inventory_tables.print()
    assert len(inventory_tables.get('host_list')) == 5
    assert len(inventory_tables.get('port_list')) == 10
    assert len(inventory_tables.get('arp_list'))  == 14
    # assert inventory_dat.count() == 4
    # assert inventory_dat.port_count() == 10

def test_load_multiple_inventory3():
    Config().set_inventory_dir('tests/resources/import/project1')
    Config().set_result_dir('/tmp')
    collector = InventoryCollector()
    Config().accept(collector)
    inventorys = collector.scan_inventorys()
    inventory_tables = collector.load(inventorys)
    # inventory_tables.print()
    assert len(inventory_tables.get('host_list')) == 3# n_host_list # 3
    assert len(inventory_tables.get('port_list')) == 6# n_port_list # 5

def test_export_inventory1():
    Config().set_result_dir('/tmp')
    collector = InventoryCollector()
    Config().accept(collector)
    collector.export('tests/resources/import/')

def test_export_inventory2():
    collector = InventoryCollector()
    inventorys = collector.scan_inventorys('tests/resources/import/project1')
    inventorys.extend(collector.scan_inventorys('tests/resources/import/project2'))
    inventorys.extend(collector.scan_inventorys('tests/resources/import/net1'))
    inventory_tables = collector.load(inventorys)
    hosts = inventory_tables.get('host_list')
    # print(hosts[hosts['ホスト名'] == 'ostrich'].T)
    print(inventory_tables.get_domains())
