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

# py.test tests/master_data/test_merge.py -v --capture=no -k test_merge3

def test_merge1():
    hosts = pd.read_csv('tests/resources/transfer/host_list.csv')
    ports = pd.read_csv('tests/resources/transfer/port_list.csv')
    arp_tables = pd.read_csv('tests/resources/transfer/arp_list.csv')

    df = MergeMaster().join_by_host(hosts, ports, 'ホスト名')
    df2 = MergeMaster().join_by_host(df, arp_tables, 'IP')
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
