import re
import sys
import os
import pytest

from getconfig_cleansing import cli
from getconfig_cleansing.evidence.collector import InventoryCollector
from getconfig_cleansing.evidence.loader import InventoryLoader

# py.test tests/evidence/test_collector.py -v --capture=no -k test_parse_evidence_from_excel

def test_parse_evidence_from_excel_file1():
    inventory = InventoryCollector().make_inventory_from_excel_path('')
    assert inventory == None

@pytest.mark.parametrize('path,name,project,timestamp', [
    ('/project1/build/サーバチェックシート_20180818_101734.xlsx',
        'サーバチェックシート', 'project1', '20180818_101734'),
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
    path = 'tests/resources/project1/build/サーバチェックシート_20180818_101734.xlsx'
    inventorys = collector.scan_excel_inventory_files(path)
    assert len(inventorys) == 1

def test_parse_evidence_from_excel_dir2():
    collector = InventoryCollector()
    path = 'tests/resources'
    inventorys = collector.scan_excel_inventory_files(path)
    assert len(inventorys) > 1

def test_parse_evidence_from_excel_dir3():
    collector = InventoryCollector()
    path = 'hoge'
    inventorys = collector.scan_excel_inventory_files(path)
    print(inventorys)
    assert 1 == 1

def test_load1():
    collector = InventoryCollector()
    path = 'tests/resources/project1/build/サーバチェックシート_20180818_101734.xlsx'
    inventory = collector.make_inventory_from_excel_path(path)
    df = InventoryLoader().read_inventory_excel(inventory)
    print(df[['ホスト名','IP']])
    assert 1 == 1

# def test_parse_evidence_from_excel_dir():
#     collector = InventoryCollector()
#     evidence = collector.make_inventory_from_source('tests/resources/')
#     assert 1 == 1

# def test_parse_evidence_from_excel1(collector):
#     path = '/project1/build/サーバチェックシート_20180818_101734.xlsx'
#     evidence = collector.make_inventory_from_source(path)

#     assert evidence.source    == path
#     assert evidence.name      == 'サーバチェックシート'
#     assert evidence.project   == 'project1'
#     assert evidence.timestamp == '20180818_101734'

def test_import_single_evidence():
    collector = InventoryCollector('tests/resources/import/project1/build/サーバチェックシート_20180818_101734.xlsx')
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


