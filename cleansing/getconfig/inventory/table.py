import re
import sys
import os
import logging
import numpy as np
import pandas as pd
from getconfig.util import Util
from getconfig.merge_master import MergeMaster
from getconfig.inventory.info import InventoryInfo

class InventoryTableSet(object):

    def __init__(self, **kwargs):
        self.inventory_tables = dict()
        self.domains = dict()

    def get_all(self):
        return self.inventory_tables

    def get(self, name):
        return self.inventory_tables.get(name, pd.DataFrame())

    def update_domain_dict(self, df):
        if pd.Series(['ドメイン']).isin(df.columns).all():
            for index, row in df.iterrows():
                self.domains[row['ホスト名'], row['ドメイン']] = 1

    def get_domains(self):
        hosts = dict()
        for (host, domain) in self.domains.keys():
            domain_list = hosts.get(host, list())
            domain_list.append(domain)
            hosts[host] = domain_list
        return hosts

    def reset_host_domains(self):
        domains = self.get_domains()
        for name, df in self.inventory_tables.items():
            if not pd.Series(['ドメイン']).isin(df.columns).all():
                continue
            df['ドメイン'] = df.apply(lambda x: domains.get(x['ホスト名']), axis = 1)
            self.inventory_tables[name] = df

    def add(self, name, df, include_ip = False):
        target = self.inventory_tables.get(name, pd.DataFrame())
        if df.empty:
            return
        self.update_domain_dict(df)
        if target.empty:
            self.inventory_tables[name] = df
        else:
            join_key = ['ホスト名', 'IP'] if include_ip else 'ホスト名'
            target = MergeMaster().join_by_host(target, df, join_key)
            self.inventory_tables[name] = target

    def save_csv(self, target_dir):
        for name, inventory_table in self.inventory_tables.items():
           Util().save_data(inventory_table, target_dir, name + '.csv')

    def print(self, columns = None):
        for name, inventory_table in self.inventory_tables.items():
            if columns:
                print(name, " : ", inventory_table[columns])
            else:
                print(name, " : ", inventory_table)

# class InventoryTable(object):

#     def __init__(self, name, df = pd.DataFrame(), **kwargs):
#         self.name = name
#         self.df = df

#     def print(self, columns = None):
#         if columns:
#             print("HOSTS : ", self.df[columns])
#         else:
#             print("HOSTS : ", self.df)

#     def count(self):
#         return len(self.df)

#     def merge_data_frame(self, source, target, join_key):
#         if not target.empty:
#             if not source.empty:
#                 source = MergeMaster().join_by_host(source, target, join_key)
#             else:
#                 source = target
#         return source

#     def merge(self, target, include_ip = False):
#         join_key = ['ホスト名', 'IP'] if include_ip else 'ホスト名'
#         self.df = self.merge_data_frame(self.df, target.df, join_key)

#     def save_csv(self, target_dir):
#         Util().save_data(self.df, target_dir, self.name + '.csv')

