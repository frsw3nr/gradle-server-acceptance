import re
import sys
import os
import pytest
import numpy as np
import pandas as pd
from pprint import pprint
from getconfig_cleansing.cli import cli
from getconfig_cleansing.config import Config
from getconfig_cleansing.merge_master import MergeMaster
from getconfig_cleansing.inventory.collector import InventoryCollector
from getconfig_cleansing.inventory.table import InventoryTableSet
from getconfig_cleansing.inventory.loader import InventoryLoader

# py.test tests/master_data/test_merge.py -v --capture=no -k test_merge1

def test_merge1():
    hosts = pd.read_csv('tests/resources/transfer/host_list.csv')
    ports = pd.read_csv('tests/resources/transfer/port_list.csv')
    arp_tables = pd.read_csv('tests/resources/transfer/arp_list.csv')

    df = MergeMaster().join_by_host(hosts, ports, 'ホスト名')
    df2 = MergeMaster().join_by_host(df, arp_tables, 'IP')
    print(df2[df2['ホスト名']=='ostrich'].T)

