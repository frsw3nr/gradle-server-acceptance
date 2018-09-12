import re
import sys
import os
import shutil
import logging
import numpy as np
import pandas as pd
import dataset as ds
from argparse import ArgumentParser
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
from getconfig.ticket.template.ticket_network   import TicketNetwork
from getconfig.ticket.template.ticket_software  import TicketSoftware
from getconfig.ticket.template.ticket_port_list import TicketPortList
from getconfig.ticket.template.ticket_relation  import TicketRelation

'''
テスト方法

事前準備

bundle exec bin/rake db:migrate:reset
bundle exec bin/rake redmine:plugins:migrate RAILS_ENV=production

redmine/plugins 下の README.rdoc の手順の通りRedmine 初期設定

test1 プロジェクトを作成して、登録スクリプト実行

python getconfig/job/template/scheduler_software1.py  --default-site test1 project3 -s

'''


class Scheduler(SchedulerBase):

    def __init__(self, inventory_source = None, **kwargs):
        self.inventory_source = inventory_source

    def parser(self):
        """
        実行オプション解析
        """
        usage = 'python {} [-d <site>] [-s] <inventorys>'\
                .format(__file__)
        argparser = ArgumentParser(usage=usage)
        argparser.add_argument('inventory_names', type=str, nargs='*',
                               help='inventory source filename')
        argparser.add_argument('-d', '--default-site', type=str,
                               dest='default_site',
                               help='Redmine default project(site)')
        argparser.add_argument('-s', '--skip-regist', action='store_true',
                               dest='skip_regist', 
                               help='Skip regist')
        argparser.set_defaults(skip_regist=False)
        return argparser.parse_args()

    def load(self):
        '''データロード'''

    def test(self):
        print('SCHEDULER_TEST1')

    def read_csv(self, csv_file, metric_name):
        csv_path = os.path.join('data/transfer', csv_file)
        df = pd.read_csv(csv_path) if os.path.exists(csv_path) else pd.DataFrame()
        Stat().regist(metric_name, len(df), self.module_name)
        return df

    def transfer(self, inventory_names):
        # '''データ変換'''
        # # ネットワークと、v1.24 のインベントリを読み込む（サーバ、ストレージ）
        logger = logging.getLogger(__name__)
        Stat().create_report_id()
        collector = InventoryCollector()
        inventorys = list()
        for inventory_name in inventory_names:
            inventory_path = os.path.join('data/import', inventory_name)
            inventorys.extend(collector.scan_inventorys(inventory_path))
        # inventorys = collector.scan_inventorys('data/import/project1')
        # inventorys.extend(collector.scan_inventorys('data/import/net1'))
        Stat().regist('0.インベントリソース', len(inventorys), self.module_name)
        inventory_tables = collector.load(inventorys)
        inventory_tables.save_csv('data/transfer')

        soft_list = MasterDataSoftwareList().load_all()
        hosts = self.read_csv('host_list.csv', '1.インベントリMWインスタンス総数')
        # ports = self.read_csv('port_list.csv', '2.インベントリ抽出IP数')
        # arp_tables = self.read_csv('arp_list.csv', '3.インベントリARPテーブル数')

        # # ネットワーク機器台帳の読込み
        # net_list  = MasterDataNetworkList().load_all()

        # # ネットワークインベントリとのつき合わせ
        # # 'スイッチ名'をキーに'ネットワーク機器管理台帳'とつき合わせ
        # hosts.rename(columns={'ホスト名': 'スイッチ名'}, inplace=True)
        # hosts = MergeMaster().join_by_host(hosts, net_list, 'スイッチ名', 'left')

        # # ARPテーブル(IPリスト)インベントリとのつき合わせ
        # # 'スイッチ名'をキーに'ARPテーブル'とつき合わせ
        # # ports = MergeMaster().join_by_host(ports, arp_tables, 'IP', 'left')
        # # ports = MergeMaster().join_by_host(ports, net_list, 'スイッチ名', 'left')

        # # # MACアドレスの先頭6桁をキーにベンダー情報をルックアップ
        # # # ports['MAC6'] = ports['MACアドレス'].str.replace('.','').str[:6]
        # # # ports = pd.merge( ports, mac_vendor, on='MAC6', how='left' )

        # ホストとポートをつき合わせ
        hosts = MergeMaster().join_by_host(hosts, soft_list, 'ジョブ名', 'outer')
        # print(hosts[hosts['ホスト名'] == 'ostrich'].T)
        Util().save_data(hosts, 'data/classify', 'softwares.csv')
        Stat().regist('1.ミドルウェア管理台帳行数', len(soft_list),
                      self.module_name)
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
        elif filter == 'software':
            if tracker == 'ソフトウェア':
                return TicketSoftware()

    def clear_work_dir(self):
        for build_dir in ['transfer', 'classify']:
            path = "data/{}".format(build_dir)
            if os.path.exists(path):
                shutil.rmtree(path)
            os.makedirs(path, exist_ok=True)

    def regist(self, **kwargs):
        '''データ登録'''
        logger = logging.getLogger(__name__)
        redmine_stat = RedmineStatistics()
        redmine_stat.reset()
        port_list = pd.read_csv('data/classify/softwares.csv')
        port_list_sets = port_list.groupby(by='ホスト名')

        # 指定キーでグルーピングしてホストを登録
        for hostname, host_list_set in port_list_sets.first().iterrows():
            host_list_set['ホスト名'] = hostname
            tracker = Util().analogize_tracker(host_list_set['ドメイン'])
            print(hostname, tracker)
            ticket_manager = self.get_ticket_manager(tracker, 'software')
            if not ticket_manager:
                continue
            # host = self.regist_host(hostname, portListSet, **kwargs)
            site = Util().analogize_site(host_list_set.get('サイト'),
                                         kwargs.get('default_site', '場所不明'))
            print("サイト：", site, ",", type(site))
            # site = 'test1'
            host = ticket_manager.regist(site, hostname, host_list_set)
            logger.info ("Regist {} : {}({})".format(tracker, hostname, host['id']))
            # # print(port_list_set)
        redmine_stat.show()

if __name__ == '__main__':
    # ログの初期化
    logging.basicConfig(
        level=getattr(logging, 'INFO'),
        format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
        datefmt='%Y/%m/%d %H:%M:%S',
    )
    logger = logging.getLogger(__name__)

    args = Scheduler().parser()

    Scheduler().clear_work_dir()
    Scheduler().transfer(args.inventory_names)
    if not args.skip_regist:
        Scheduler().regist(default_site = args.default_site)

