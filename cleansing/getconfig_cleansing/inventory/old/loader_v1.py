import re
import sys
import os
import logging
import numpy as np
import pandas as pd
from abc import ABCMeta, abstractmethod
from getconfig_cleansing.util import Util
from getconfig_cleansing.merge_master import MergeMaster
from getconfig_cleansing.inventory.info import InventoryInfo
from getconfig_cleansing.inventory.table import InventoryTableSet
# from getconfig_cleansing.inventory.data import InventoryData
from getconfig_cleansing.inventory.old.evidence_v1 import GetconfigEvidenceV1

class InventoryLoaderV1(object):
    def import_inventory_sheet(self, inventory_info, inventory_tables):
        _logger = logging.getLogger(__name__)
        # print("■旧タイプインベントリロード", inventory_info.source)
        db = GetconfigEvidenceV1(inventory_info.source, export_dir='build/tmp')
        db.load()

        # Linux の OS 、アーキテクチャ、CPU、メモリ読み込み
        df_linux = self.make_linux_inventory(db)
        df_linux = df_linux.reset_index()
        # print("■Linux", df_linux)

        # Windows の OS 、アーキテクチャ、CPU、メモリ読み込み
        df_windows = self.make_windows_inventory(db)
        df_windows = df_windows.reset_index()
        # print("■Windows", df_windows)

        # iLO の 管理 LAN メモリ読み込み
        df_ilo = self.make_ilo_inventory(db)
        # print("■iLO", df_ilo)

        df = pd.merge(df_linux, df_windows, how='outer')
        df = MergeMaster().join_by_host(df_linux, df_windows, 'node_name')
        df = MergeMaster().join_by_host(df,       df_ilo,     'node_name')
        df.rename(columns={'node_name': 'ホスト名', 'domain': 'ドメイン'},
                  inplace=True)

        # ネットワーク構成情報から、IPアドレスを抽出
        port_list = pd.DataFrame()
        if pd.Series(['ネットワーク構成']).isin(df.columns).all():
            df2 = Util().expand_ip_address_list(df, 'ネットワーク構成')
            df2['AdminIP'] = False
            port_list = pd.concat([port_list, df2], axis=0)

        # 管理LAN情報から、IPアドレスを抽出
        if pd.Series(['管理LAN']).isin(df.columns).all():
            df2 = Util().expand_ip_address_list(df, '管理LAN')
            df2['AdminIP'] = True
            port_list = pd.concat([port_list, df2], axis=0)

        df['インベントリ名'] = inventory_info.name
        df['ジョブ名'] = inventory_info.project

        # db.export()
        # (df, port_list) = self.read_old_inventory(db)
        # df = pd.DataFrame()
        # port_list = pd.DataFrame()
        inventory_tables.add('host_list', df)
        inventory_tables.add('port_list', port_list, True)

    def make_linux_inventory(self, db):
        # Linux の OS 、アーキテクチャ、CPU、メモリ読み込み
        df = db.df_summary
        df = df[df['domain'] == 'Linux']
        if df.empty:
            return df
        cond = (df['test_id'].isin(['lsb', 'uname', 'cpu_total', 'mem_total']))
        df2 = df[['node_name', 'test_id', 'value']][cond]
        df2 = df2.set_index(['node_name', 'test_id'])
        # unstackのみのコードだと、unstack後のカラム定義がMultiindexになる。戻し方が不明なため、
        # 回避策としてunstack + T + reset_indexでカラムを再定義する
        df2 = df2.unstack(level=0).T.reset_index()
        del df2['level_0']
        df2 = df2.set_index('node_name')
        df2.rename(columns={'lsb': 'OS名', 'uname': 'アーキテクチャ', 
                            'cpu_total':'CPU数', 'mem_total':'MEM容量'},
                   inplace=True)

        # ディスク構成の読み込み
        df = db.df_devices['Linux_filesystem']
        df3 = df[df['mountpoint']!='NaN']
        df3['disk'] = df3['mountpoint'] + ':' + df3['size']
        df3 = df3.pivot(index='node_name', columns='mountpoint', values='disk')
        df3 = df3.apply(','.join, axis=1)
        df2['ディスク構成'] = df3

        # ネットワーク構成の読み込み
        df = db.df_devices['Linux_network']
        df3 = df[df['ip']!='NaN']
        df3['net'] = df3['device'] + ':' + df3['ip']
        df3 = df3.pivot(index='node_name', columns='device', values='net')
        df3 = df3.apply(','.join, axis=1)
        df2['ネットワーク構成'] = '[' + df3.astype(object) + ']'
        df2['domain'] = 'Linux'

        return df2.reset_index()

    def make_windows_inventory(self, db):
        # Linux の OS 、アーキテクチャ、CPU、メモリ読み込み
        df = db.df_summary
        df = df[df['domain'] == 'Windows']
        if df.empty:
            return df
        cond = (df['test_id'].isin(['os_caption', 'os_architecture', 'cpu_total', 'mem_total']))
        df2 = df[['node_name', 'test_id', 'value']][cond]
        df2 = df2.set_index(['node_name', 'test_id'])
        # 以下のコードだと、unstack後のカラム定義がMultiindexになる。戻し方が不明なため、
        # 回避策としてunstack + T + reset_indexでカラムを再定義する
        df2 = df2.unstack(level=0).T.reset_index()
        del df2['level_0']
        df2 = df2.set_index('node_name')
        df2.rename(columns={'os_caption': 'OS名', 'os_architecture': 'アーキテクチャ', 
                            'cpu_total':'CPU数', 'mem_total':'MEM容量'},
                   inplace=True)

        # ディスク構成の読み込み
        df = db.df_devices['Windows_filesystem']
        df3 = df[df['device_id']!='NaN']
        df3['disk'] = df3['device_id'] + ':' + df3['size_gb'].astype(int).astype(str)
        df3 = df3.pivot(index='node_name', columns='device_id', values='disk')
        df3 = df3.apply(','.join, axis=1)
        df2['ディスク構成'] = df3

        # # ネットワーク構成の読み込み
        df = db.df_devices['Windows_network']
        df3 = df[df['IPAddress']!='NaN']
        # IPアドレスのみを抽出する
        df3['net'] = df3['IPAddress'].str.extract('(?P<IP>\d{1,3}\.\d{1,3}\.\d{1,3}.\d{1,3})', expand=True)
        df3 = df3.pivot(index='node_name', columns='IPAddress', values='net')
        df3 = df3.apply(','.join, axis=1)
        df2['ネットワーク構成'] = '[' + df3.astype(object) + ']'
        df2['domain'] = 'Windows'

        return df2.reset_index()

    def make_ilo_inventory(self, db):
        # HP iLO の IP アドレス読み込み
        df = db.df_summary
        df = df[df['domain'] == 'iLO']
        if df.empty:
            return df
        cond = (df['test_id'].isin(['Nic']))
        df2 = df[['node_name', 'value']][cond]
        # df2 = df2.set_index(['node_name', 'test_id'])
        # df2 = df2.unstack()
        df2.rename(columns={'value': '管理LAN'}, inplace=True)
        df2['domain'] = 'iLO'
        # print(df2)
        # df2 = df2.set_index(['node_name'])

        return df2
