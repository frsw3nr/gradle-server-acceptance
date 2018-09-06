import re
import sys
import os
import logging
import numpy as np
import pandas as pd
from getconfig.util import Util
from getconfig.merge_master import MergeMaster
from getconfig.inventory.info import InventoryInfo

class InventoryData(object):

    # def __init__(self, host_list = pd.DataFrame(), port_list = pd.DataFrame(), **kwargs):
    #     self.host_list = host_list
    #     self.port_list = port_list

    # def print(self, columns = None):
    #     if columns:
    #         print("HOSTS : ", self.host_list[columns])
    #     else:
    #         print("HOSTS : ", self.host_list)
    #     print("PORTS : ", self.port_list)

    # def count(self):
    #     return len(self.host_list)

    # def port_count(self):
    #     return len(self.port_list)

    # def merge_inventory_data_frame(self, source, target, join_key):
    #     if not target.empty:
    #         if not source.empty:
    #             source = MergeMaster().join_by_host(source, target, join_key)
    #         else:
    #             source = target
    #     return source

    # def merge_inventory_data(self, target):
    #     self.host_list = self.merge_inventory_data_frame(self.host_list, 
    #                                                      target.host_list,
    #                                                      'ホスト名')
    #     self.port_list = self.merge_inventory_data_frame(self.port_list, 
    #                                                      target.port_list,
    #                                                      ['ホスト名', 'IP'])

    # def save_csv(self, target_dir):
    #     Util().save_data(self.host_list, target_dir, 'host_list.csv')
    #     Util().save_data(self.port_list, target_dir, 'host_ip_list.csv')

