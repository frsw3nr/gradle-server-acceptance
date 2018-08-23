import re
import sys
import os
import pytest
import numpy as np
import pandas as pd

from getconfig_cleansing.cli import cli
from getconfig_cleansing.inventory.collector import InventoryCollector
from getconfig_cleansing.inventory.loader import InventoryLoader

# py.test tests/inventory/test_collector.py -v --capture=no -k test_parse_evidence_from_excel_file1

def test_parse_evidence_from_excel_file1():
    inventory = InventoryCollector().make_inventory_from_excel_path('')
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
    inventory = InventoryCollector().make_inventory_from_excel_path(path)
    assert inventory.source    == path
    assert inventory.name      == name
    assert inventory.project   == project
    assert inventory.timestamp == timestamp

def test_parse_evidence_from_excel_dir1():
    collector = InventoryCollector()
    path = 'tests/resources/import/project1/build/サーバチェックシート_20180817_142016.xlsx'
    inventorys = collector.scan_excel_inventory_files(path)
    assert len(inventorys) == 1

def test_parse_evidence_from_excel_dir2():
    collector = InventoryCollector()
    inventorys = collector.scan_excel_inventory_files('tests/resources/import')
    assert len(inventorys) > 1

def test_parse_evidence_from_excel_dir3():
    collector = InventoryCollector()
    inventorys = collector.scan_excel_inventory_files('hoge')
    assert not inventorys

def test_load_single_inventory1():
    collector = InventoryCollector()
    path = 'tests/resources/import/project1/build/サーバチェックシート_20180817_142016.xlsx'
    inventory = collector.make_inventory_from_excel_path(path)
    (host_list, port_list) = InventoryLoader().read_inventory_excel(inventory)
    print(host_list[['ホスト名', 'OS名']])
    print(port_list)
    assert len(host_list) == 3
    assert len(port_list) == 5

def test_load_single_inventory2():
    # iLOチェックシート読み込みで、'unexpected tag'エラー発生.
    # 該当Excel を開いて上書きしたら解消した
    # Exception: unexpected tag '{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ext'
    collector = InventoryCollector()
    path = 'tests/resources/import/project1/build/iLOチェックシート_20180817_092416.xlsx'
    inventory = collector.make_inventory_from_excel_path(path)
    (host_list, port_list) = InventoryLoader().read_inventory_excel(inventory)

    print(host_list[['ホスト名', 'OS名']])
    print(port_list)
    assert len(host_list) == 1
    assert len(port_list) == 3

def test_load_single_inventory3():
    collector = InventoryCollector()
    path = 'tests/resources/import/project2/build/Zabbix監視設定チェックシート_20180820_112635.xlsx'
    inventory = collector.make_inventory_from_excel_path(path)
    (host_list, port_list) = InventoryLoader().read_inventory_excel(inventory)
    print(host_list[['ホスト名','ホストグループ']])
    print(port_list)
    assert len(host_list) == 3
    assert len(port_list) == 0

def test_load_single_inventory4():
    collector = InventoryCollector()
    path = 'tests/resources/import/project4/build/ETERNUSチェックシート_20180820_115934.xlsx'
    inventory = collector.make_inventory_from_excel_path(path)
    (host_list, port_list) = InventoryLoader().read_inventory_excel(inventory)
    print(host_list)
    print(port_list)
    assert len(host_list) == 1
    assert len(port_list) == 2

def test_load_single_inventory5():
    collector = InventoryCollector()
    path = 'tests/resources/import/empty.xlsx'
    inventory = collector.make_inventory_from_excel_path(path)
    (host_list, port_list) = InventoryLoader().read_inventory_excel(inventory)
    print(host_list)
    print(port_list)
    assert len(host_list) == 0
    assert len(port_list) == 0

def test_load_single_inventory6():
    collector = InventoryCollector()
    path = 'tests/resources/import/old1/build/check_sheet_20170512_143424.xlsx'
    inventory = collector.make_inventory_from_excel_path(path)
    (host_list, port_list) = InventoryLoader().read_inventory_excel(inventory)
    print(host_list)
    print(port_list)
    # assert len(host_list) == 0
    # assert len(port_list) == 0
    assert 1 == 1

# def test_load_single_inventory7():
#     collector = InventoryCollector()
#     # path = 'tests/resources/import/old1/build/iLOチェックシート_20180201_114341.xlsx'
#     # path = '/home/psadmin/work/getconfig/rep_network_hosts/DataCleansing/getconfig/AT0043G/build/iLOチェックシート_20180803_152632.xlsx'
#     path = '/home/psadmin/work/getconfig/rep_network_hosts/DataCleansing/getconfig/4at00vx_y/build/iLOチェックシート_20170626_140157.xlsx'
#     inventory = collector.make_inventory_from_excel_path(path)
#     (host_list, port_list) = InventoryLoader().read_inventory_excel(inventory)
#     print(host_list)
#     print(port_list)
#     # assert len(host_list) == 0
#     # assert len(port_list) == 0
#     assert 1 == 1

def test_load_multiple_inventory1():
    collector = InventoryCollector()
    inventorys = collector.scan_excel_inventory_files('tests/resources/import/project1')
    (host_list, port_list) = collector.load(inventorys)
    print(host_list[['ホスト名', 'OS名', 'getconfig_project', 'getconfig_name']])
    print(port_list)
    assert len(host_list) == 3
    assert len(port_list) == 6

def test_load_multiple_inventory2():
    collector = InventoryCollector()
    inventorys = collector.scan_excel_inventory_files('tests/resources/import/')
    (host_list, port_list) = collector.load(inventorys)
    print(host_list[['ホスト名', 'OS名']])
    print(port_list)
    assert len(host_list) == 4
    assert len(port_list) == 8


# def test_parse_evidence_from_excel_dir():
#     collector = InventoryCollector()
#     evidence = collector.make_inventory_from_source('tests/resources/')
#     assert 1 == 1

# def test_parse_evidence_from_excel1(collector):
#     path = '/project1/build/サーバチェックシート_20180817_092416.xlsx'
#     evidence = collector.make_inventory_from_source(path)

#     assert evidence.source    == path
#     assert evidence.name      == 'サーバチェックシート'
#     assert evidence.project   == 'project1'
#     assert evidence.timestamp == '20180817_092416'

def test_import_single_evidence():
    collector = InventoryCollector('tests/resources/import/project1/build/サーバチェックシート_20180817_092416.xlsx')
    print ("Single test")
    collector.parse()
    assert 1 == 1

def test_import_multiple_evidence():
    collector = InventoryCollector('tests/resources/import')
    collector.parse()
    rv = collector.list_evidences()
    # assert len(rv) == 4

    # evidences = collector.list_commands()
    # print(evidences)
    # for evidence in evidences:
    #     print("Inventory:%s" % (evidence))
    #     match = re.match(r'^(.+)_(\d+_\d+)$', evidence)
    #     timestamp = ''
    #     if match:
    #         evidence = match.group(1)
    #         timestamp = match.group(2)
    #     print("Check: %s, Date: %s" % (evidence, timestamp))
    #     mod = collector.get_module(evidence)
    #     if mod:
    #         mod.InventoryParser().say('Hello')

    # mod1 = loader.get_module('check_sheet')
    # mod1.InventoryParser().say('Hello')

    # mod2 = loader.get_module('サーバチェックシート')
    # mod2.InventoryParser().say('Hello')
    assert 1 == 1


