import re
import sys
import os
import logging
import numpy as np
import pandas as pd
import dataset as ds
from getconfig.util         import Util
from getconfig.config       import Config
from getconfig.stat         import Stat
from getconfig.merge_master import MergeMaster
from getconfig.inventory.info      import InventoryInfo
from getconfig.inventory.loader    import InventoryLoader
from getconfig.inventory.collector import InventoryCollector
from getconfig.inventory.table     import InventoryTableSet
from getconfig.master_data.template.job_list  import MasterDataJobList
from getconfig.master_data.template.ship_list import MasterDataShipList
from getconfig.master_data.template.mw_list   import MasterDataSoftwareList
from getconfig.master_data.template.net_list  import MasterDataNetworkList
from getconfig.job.scheduler_base  import SchedulerBase
from getconfig.ticket.redmine_stat import RedmineStatistics
from getconfig.ticket.template.ticket_ia_server import TicketIAServer
from getconfig.ticket.template.ticket_sparc     import TicketSparc
from getconfig.ticket.template.ticket_power     import TicketPower
from getconfig.ticket.template.ticket_storage   import TicketStorage
# from getconfig.ticket.template.ticket_network import TicketNetwork
from getconfig.ticket.template.ticket_port_list import TicketPortList
from getconfig.ticket.template.ticket_relation  import TicketRelation

'''
テスト方法

事前準備

bundle exec bin/rake db:migrate:reset
bundle exec bin/rake redmine:plugins:migrate RAILS_ENV=production

redmine/plugins 下の README.rdoc の手順の通りRedmine 初期設定

test1 プロジェクトを作成して、登録スクリプト実行

python getconfig/job/template/scheduler_test1.py
'''


class Scheduler(SchedulerBase):

    def __init__(self, inventory_source = None, **kwargs):
        self.inventory_source = inventory_source

    def load(self):
        '''データロード'''

    def test(self):
        print('SCHEDULER_TEST1')

    def read_csv(self, csv_file, metric_name):
        csv_path = os.path.join('data/transfer', csv_file)
        df = pd.read_csv(csv_path)
        Stat().regist(metric_name, len(df), self.module_name)
        return df

    def transfer(self):
        # '''データ変換'''
        # # ネットワークと、v1.24 のインベントリを読み込む（サーバ、ストレージ）
        logger = logging.getLogger(__name__)
        Stat().create_report_id()
        collector = InventoryCollector()
        inventorys = collector.scan_inventorys('data/import/project1')
        inventorys.extend(collector.scan_inventorys('data/import/project2'))
        inventorys.extend(collector.scan_inventorys('data/import/net1'))
        Stat().regist('0.インベントリソース', len(inventorys), self.module_name)
        inventory_tables = collector.load(inventorys)
        inventory_tables.save_csv('data/transfer')

        hosts = self.read_csv('host_list.csv', '1.インベントリ機器総数')
        # hosts = pd.read_csv('data/transfer/host_list.csv')
        # Stat().regist('1.インベントリ:機器', len(hosts), 'transfer')
        # ports = pd.read_csv('data/transfer/port_list.csv')
        ports = self.read_csv('port_list.csv', '2.インベントリ抽出IP数')
        # Stat().regist('1.インベントリ:IP', len(ports), 'transfer')
        # arp_tables = pd.read_csv('data/transfer/arp_list.csv')
        arp_tables = self.read_csv('arp_list.csv', '3.インベントリARPテーブル数')
        # Stat().regist('1.インベントリ:ARPテーブル', len(arp_tables), 'transfer')

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
        # hosts = hosts[hosts['ホスト名'] == 'ostrich']
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
        # print(df[df['ホスト名'] == 'ostrich'].T)
        Util().save_data(df, 'data/classify', 'hosts.csv')
        df_host = df.groupby(by=['ホスト名'])
        Stat().regist('5.出荷機器つき合わせ台数', len(df_host), self.module_name)
        Stat().regist('6.出荷機器つき合わせIP数', len(df), self.module_name)
        Stat().show()

    def classify(self):
        '''データ分類'''

    def get_ticket_manager(self, tracker, filter = 'server'):
        if filter == 'server':
            if tracker == 'IAサーバ':
                return TicketIAServer()
            elif tracker == 'SPARCサーバ':
                return TicketSparc()
            elif tracker == 'POWERサーバ':
                return TicketPower()
            elif tracker == 'ストレージ':
                return TicketStorage()
        elif filter == 'network':
            if tracker == 'ネットワーク':
                return TicketNetwork()

    def regist(self):
        '''データ登録'''
        logger = logging.getLogger(__name__)
        redmine_stat = RedmineStatistics()
        redmine_stat.reset()
        port_list = pd.read_csv('data/classify/hosts.csv')
        port_list_sets = port_list.groupby(by='ホスト名')

        # 指定キーでグルーピングしてホストを登録
        for hostname, host_list_set in port_list_sets.first().iterrows():
            host_list_set['ホスト名'] = hostname
            tracker = Util().analogize_tracker(host_list_set['ドメイン'])
            ticket_manager = self.get_ticket_manager(tracker)
            if not ticket_manager:
                continue
            # host = self.regist_host(hostname, portListSet, **kwargs)
            # site = Util().analogize_site(host_list_set['サイト'])
            site = 'test1'
            host = ticket_manager.regist(site, hostname, host_list_set)
            logger.info ("Regist {} : {}({})".format(tracker, hostname, host['id']))
            # # print(port_list_set)

            # 接続しているポートを登録
            for port_id, port_list_set in port_list_sets.get_group(hostname).iterrows():
                ip_address = port_list_set['IP']
                port = TicketPortList().regist(site, ip_address, port_list_set)
                logger.info ("Regist IP : {}, HOST : {}".format(ip_address, hostname))
                # print("IP:", ip_address, ",HOST:", hostname)
                # print(port)
                TicketRelation().regist_relation(host['id'], port['id'])
        redmine_stat.show()

if __name__ == '__main__':
    # ログの初期化
    logging.basicConfig(
        level=getattr(logging, 'INFO'),
        format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
        datefmt='%Y/%m/%d %H:%M:%S',
    )
    logger = logging.getLogger(__name__)

    Scheduler().transfer()
    Scheduler().regist()
