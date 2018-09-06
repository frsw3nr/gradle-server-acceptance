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
from getconfig.master_data.template.job_list import MasterDataJobList
from getconfig.master_data.template.ship_list import MasterDataShipList
from getconfig.master_data.template.mw_list import MasterDataSoftwareList
from getconfig.master_data.template.net_list import MasterDataNetworkList
from getconfig.job.scheduler import Scheduler
from getconfig.ticket.ticket_ia_server import TicketIAServer
from getconfig.ticket.ticket_port_list import TicketPortList
from getconfig.ticket.ticket_relation import TicketRelation

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
        inventorys = collector.scan_inventorys('data/import/project1')
        inventorys.extend(collector.scan_inventorys('data/import/project2'))
        inventorys.extend(collector.scan_inventorys('data/import/net1'))
        inventory_tables = collector.load(inventorys)
        inventory_tables.save_csv('data/transfer')

        hosts = pd.read_csv('data/transfer/host_list.csv')
        ports = pd.read_csv('data/transfer/port_list.csv')
        arp_tables = pd.read_csv('data/transfer/arp_list.csv')

        # 案件情報、出荷台帳、ネットワーク台帳の読込み
        job_list   = MasterDataJobList().load_all()
        ship_list  = MasterDataShipList().load_all()
        soft_list  = MasterDataSoftwareList().load_all()
        net_list   = MasterDataNetworkList().load_all()
        # arp_tables = MasterDataPortList().load_all()
        # mac_vendor = MasterDataMacVendor().load_all()

        # ホストインベントリとのつき合わせ
        # 'ジョブ名'をキーに'案件情報'とつき合わせ
        # 'ホスト名'をキーに'出荷台帳'とつき合わせ
        hosts = MergeMaster().join_by_host(hosts, job_list, 'ジョブ名', 'left')
        hosts = MergeMaster().join_by_host(hosts, ship_list, 'ホスト名', 'left')

        # IP インベントリとのつき合わせ
        # 'IP'をキーに'オンラインIP情報'とつき合わせ
        # 'スイッチ名'をキーに'ネットワーク機器台帳'とつき合わせ
        ports = MergeMaster().join_by_host(ports, arp_tables, 'IP', 'left')
        ports = MergeMaster().join_by_host(ports, net_list, 'スイッチ名', 'left')

        # # MACアドレスの先頭6桁をキーにベンダー情報をルックアップ
        # # ports['MAC6'] = ports['MACアドレス'].str.replace('.','').str[:6]
        # # ports = pd.merge( ports, mac_vendor, on='MAC6', how='left' )

        # ホストとポートをつき合わせ
        df = MergeMaster().join_by_host(hosts, ports, 'ホスト名', 'left')
        print(df[df['ホスト名'] == 'ostrich'].T)
        Util().save_data(df, 'data/classify', 'hosts.csv')

    def classify(self):
        '''データ分類'''

    def regist(self):
        '''データ登録'''
        port_list = pd.read_csv('data/classify/hosts.csv')
        port_list_sets = port_list.groupby(by='ホスト名')

        # 指定キーでグルーピングしてホストを登録
        for hostname, host_list_set in port_list_sets.first().iterrows():
            host_list_set['ホスト名'] = hostname
            tracker = Util().analogize_tracker(host_list_set['ドメイン'])
            if tracker != 'IAサーバ':
                continue
            print("ホスト:{},{}".format(hostname, tracker))
            # host = self.regist_host(hostname, portListSet, **kwargs)
            host = TicketIAServer().regist('test1', hostname, host_list_set)
            logger.info ("Regist Host : {}({})".format(hostname, host['id']))
            # # print(port_list_set)

            # 接続しているポートを登録
            for port_id, port_list_set in port_list_sets.get_group(hostname).iterrows():
                ip_address = port_list_set['IP']
                port = TicketPortList().regist('test1', ip_address, port_list_set)
                print("IP:", ip_address, ",HOST:", hostname)
                print(port)
                TicketRelation().regist_relation(host['id'], port['id'])

if __name__ == '__main__':
    # ログの初期化
    logging.basicConfig(
        level=getattr(logging, 'INFO'),
        format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
        datefmt='%Y/%m/%d %H:%M:%S',
    )
    logger = logging.getLogger(__name__)

    SchedulerTest().transfer()
    SchedulerTest().regist()
