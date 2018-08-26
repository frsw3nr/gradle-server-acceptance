import pytest
import re
import sys
import os
import logging
import numpy as np
import pandas as pd
from getconfig_cleansing.util import Util
from getconfig_cleansing.inventory.info import InventoryInfo
from getconfig_cleansing.inventory.data import InventoryDataFrame
from getconfig_cleansing.inventory.loader import InventoryLoader

# py.test tests/test_util.py -v --capture=no -k test_util1

def test_util1():
    df = pd.DataFrame([['ostrich',
                        '[lo:127.0.0.1/8, eth0:192.168.10.1/24, eth0:1:192.168.10.4/24]']],
                        columns=['ホスト名', 'ネットワーク構成'])
    df_lan = Util().expand_ip_address_list(df, 'ネットワーク構成')
    print(df_lan)
    # print(df)
    assert len(df_lan) == 2

