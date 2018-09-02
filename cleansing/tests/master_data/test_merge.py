import re
import sys
import os
import pytest
import numpy as np
import pandas as pd
import dataset as ds
from getconfig_cleansing.util import Util
from getconfig_cleansing.config import Config
from getconfig_cleansing.merge_master import MergeMaster
from getconfig_cleansing.inventory.collector import InventoryCollector
from getconfig_cleansing.inventory.table import InventoryTableSet
from getconfig_cleansing.inventory.loader import InventoryLoader
from getconfig_cleansing.master_data.template.job_list import MasterDataJobList
from getconfig_cleansing.master_data.template.ship_list import MasterDataShipList
from getconfig_cleansing.master_data.template.mw_list import MasterDataSoftwareList
from getconfig_cleansing.master_data.template.net_list import MasterDataNetworkList

# py.test tests/master_data/test_merge.py -v --capture=no -k test_merge5

def test_merge1():
    hosts = pd.read_csv('tests/resources/transfer/host_list.csv')
    ports = pd.read_csv('tests/resources/transfer/port_list.csv')
    arp_tables = pd.read_csv('tests/resources/transfer/arp_list.csv')

    df = MergeMaster().join_by_host(hosts, ports, 'ホスト名')
    df2 = MergeMaster().join_by_host(df, arp_tables, 'IP')
    Util().save_data(df2, 'tests/resources/classify', 'host_list.csv')
    print(df2[df2['ホスト名']=='ostrich'].T)

def test_merge2():
    collector = InventoryCollector()
    inventorys = collector.scan_inventorys('tests/resources/import/project1')
    inventorys.extend(collector.scan_inventorys('tests/resources/import/net1'))
    inventory_tables = collector.load(inventorys)
    inventory_tables.save_csv('tests/resources/transfer')

    hosts = pd.read_csv('tests/resources/transfer/host_list.csv')
    ports = pd.read_csv('tests/resources/transfer/port_list.csv')
    arp_tables = pd.read_csv('tests/resources/transfer/arp_list.csv')

    job_list  = MasterDataJobList().load_all()
    ship_list = MasterDataShipList().load_all()

    df = MergeMaster().join_by_host(hosts, ship_list, 'ホスト名')
    df = MergeMaster().join_by_host(df, job_list , 'ジョブ名')

    print(df[df['ホスト名'] == 'ostrich'].T)
    Util().save_data(df, 'tests/resources/classify', 'host_list.csv')

def test_merge3():
    collector = InventoryCollector()
    inventorys = collector.scan_inventorys('tests/resources/import/project3')
    inventory_tables = collector.load(inventorys)
    inventory_tables.save_csv('tests/resources/transfer')

    hosts = pd.read_csv('tests/resources/transfer/host_list.csv')

    mw_list = MasterDataSoftwareList().load_all()

    df = MergeMaster().join_by_host(hosts, mw_list , 'ジョブ名')

    print(df[df['ホスト名'] == 'ora11'].T)
    Util().save_data(df, 'tests/resources/classify', 'host_list.csv')

def test_merge4():
    collector = InventoryCollector()
    inventorys = collector.scan_inventorys('tests/resources/import/net1')
    inventory_tables = collector.load(inventorys)
    inventory_tables.save_csv('tests/resources/transfer')

    hosts = pd.read_csv('tests/resources/transfer/host_list.csv')
    ports = pd.read_csv('tests/resources/transfer/port_list.csv')
    arp_tables = pd.read_csv('tests/resources/transfer/arp_list.csv')

    net_list = MasterDataNetworkList().load_all()

    df = MergeMaster().join_by_host(hosts, net_list, 'ホスト名')

    print(df[df['ホスト名'] == 'router1'].T)
    Util().save_data(df, 'tests/resources/classify', 'host_list.csv')

def test_merge5():
    '''サーバとネットワークインベントリを読み込み、各種台帳とつき合わせ'''

    # ネットワークと、v1.24 のインベントリを読み込む（サーバ、ストレージ）
    collector = InventoryCollector()
    inventorys = collector.scan_inventorys('tests/resources/import/net1')
    inventorys.extend(collector.scan_inventorys('tests/resources/import/v1.24'))
    inventorys.extend(collector.scan_inventorys('tests/resources/import/project2'))
    inventory_tables = collector.load(inventorys)
    inventory_tables.save_csv('tests/resources/transfer')

    hosts = pd.read_csv('tests/resources/transfer/host_list.csv')
    ports = pd.read_csv('tests/resources/transfer/port_list.csv')
    arp_tables = pd.read_csv('tests/resources/transfer/arp_list.csv')

    # 案件情報、出荷台帳、ネットワーク台帳の読込み
    job_list  = MasterDataJobList().load_all()
    ship_list = MasterDataShipList().load_all()
    net_list  = MasterDataNetworkList().load_all()
    print(ship_list['搬入日'])

    # ARPテーブルとネットワーク台帳のつき合わせ
    net_list2 = net_list.rename(columns={'ホスト名': 'スイッチ名'})
    arp_tables2 = MergeMaster().join_by_host(arp_tables, net_list2, 'スイッチ名')

    # サーバと出荷台帳のつき合わせ
    df = MergeMaster().join_by_host(hosts, ship_list, 'ホスト名')
    # 案件台帳とのつき合わせ。ジョブ名を v1.24 から、project1 に変更
    df['ジョブ名'] = 'project1'
    df = MergeMaster().join_by_host(df, job_list, 'ジョブ名')
    print(df['機種'])
    # ネットワーク台帳とのつき合わせ
    df = MergeMaster().join_by_host(df, net_list, 'ホスト名')
    print(df['機種'])
    # print(df['搬入日'])
    # 接続ポートとのつき合わせ
    df = MergeMaster().join_by_host(df, ports, 'ホスト名')
    # ARPテーブルとのつき合わせ
    df = MergeMaster().join_by_host(df, arp_tables2, 'IP')
    # 欠損地の穴埋め
    # df = df.fillna(method='ffill')
    # print(df[df['ホスト名'] == 'ostrich'].T)
    Util().save_data(df, 'tests/resources/classify', 'host_list.csv')
