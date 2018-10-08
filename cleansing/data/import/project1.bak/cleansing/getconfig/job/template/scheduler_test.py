import re
import sys
import os
import logging
import numpy as np
import pandas as pd
import dataset as ds
from getconfig.util import Util
from getconfig.config import Config
from getconfig.merge_master import MergeMaster
from getconfig.inventory.info import InventoryInfo
from getconfig.inventory.loader import InventoryLoader
from getconfig.inventory.collector import InventoryCollector
from getconfig.inventory.table import InventoryTableSet
from getconfig.master_data.template.anken_jouhou import MasterDataAnkenJyouhou
from getconfig.master_data.template.systemcode_list import MasterDataSystemcodeList
from getconfig.master_data.template.mw_jouhou import MasterDataMiddlewareJyouhou
from getconfig.master_data.template.openview_nnm_list import MasterDataOpenviewNnmList
from getconfig.master_data.template.port_list import MasterDataPortList
from getconfig.job.scheduler import Scheduler

class SchedulerTest(Scheduler):
    INVENTORY_DIR = 'build'

    def __init__(self, inventory_source = INVENTORY_DIR, **kwargs):
        self.inventory_source = inventory_source

    def load(self):
        '''データロード'''

    def transfer(self):
        # '''データ変換'''
        # # ネットワークと、v1.24 のインベントリを読み込む（サーバ、ストレージ）
        collector = InventoryCollector()
        inventorys = collector.scan_inventorys('data2/import')
        inventory_tables = collector.load(inventorys)
        inventory_tables.save_csv('data/transfer')

        hosts = pd.read_csv('data/transfer/host_list.csv')
        ports = pd.read_csv('data/transfer/port_list.csv')
        # arp_tables = pd.read_csv('data/transfer/arp_list.csv')

        # 案件情報、出荷台帳、ネットワーク台帳の読込み
        job_list  = MasterDataAnkenJyouhou().load_all()
        ship_list = MasterDataSystemcodeList().load_all()
        net_list  = MasterDataOpenviewNnmList().load_all()
        # print(ship_list['搬入日'])

        # ARPテーブルとネットワーク台帳のつき合わせ
        # net_list2 = net_list.rename(columns={'ホスト名': 'スイッチ名'})
        arp_tables = MasterDataPortList().load_all()
        print(arp_tables.columns)
        print(net_list.columns)
        arp_tables2 = MergeMaster().join_by_host(arp_tables, net_list, 'スイッチ名')
        arp_tables2 = arp_tables2.dropna(subset=['IP'])
        Util().save_data(arp_tables2, '/tmp', 'arp_tables2.csv')

        # # サーバと出荷台帳のつき合わせ
        df = MergeMaster().join_by_host(hosts, ship_list, 'ホスト名')
        # 案件台帳とのつき合わせ
        df = MergeMaster().join_by_host(df, job_list, 'ジョブ名')
        # ネットワーク台帳とのつき合わせ
        # df = MergeMaster().join_by_host(df, net_list, 'ホスト名')
        # print(df[df['ホスト名'] == 'yaqdb61']['ホスト名','機種', 'スイッチ名'])
        # print(df['搬入日'])
        # 接続ポートとのつき合わせ
        df = MergeMaster().join_by_host(df, ports, 'ホスト名')
        df = df.dropna(subset=['IP'])
        Util().save_data(df, '/tmp', 'host2.csv')
        # ARPテーブルとのつき合わせ
        df = MergeMaster().join_by_host(df, arp_tables2, 'IP')
        # print(df.loc[df['ホスト名'] == 'yaqdb61',['ホスト名','用途', 'スイッチ名', 'IP']].T)
        # print(df.loc[df['ホスト名'] == 'yaqdb61',['ホスト名','用途']].T)
        # df = pd.DataFrame()
        return df

    def classify(self):
        '''データ分類'''

    def regist(self):
        '''データ登録'''

if __name__ == '__main__':
    # ログの初期化
    logging.basicConfig(
        level=getattr(logging, 'INFO'),
        format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
        datefmt='%Y/%m/%d %H:%M:%S',
    )
    logger = logging.getLogger(__name__)

    df = SchedulerTest().transfer()
    Util().save_data(df, '/tmp', 'mw_list.csv')
