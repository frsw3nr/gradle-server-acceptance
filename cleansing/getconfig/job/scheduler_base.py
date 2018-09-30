import re
import sys
import os
import logging
import shutil
import numpy as np
import pandas as pd
import dataset as ds
from argparse import ArgumentParser
from abc import ABCMeta, abstractmethod
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
from getconfig.ticket.redmine_stat import RedmineStatistics
from getconfig.ticket.template.ticket_ia_server import TicketIAServer
from getconfig.ticket.template.ticket_sparc     import TicketSparc
from getconfig.ticket.template.ticket_power     import TicketPower
from getconfig.ticket.template.ticket_storage   import TicketStorage
from getconfig.ticket.template.ticket_network   import TicketNetwork
from getconfig.ticket.template.ticket_software  import TicketSoftware
from getconfig.ticket.template.ticket_port_list import TicketPortList
from getconfig.ticket.template.ticket_relation  import TicketRelation

class SchedulerBase(metaclass=ABCMeta):
    module_name = '変換処理'
    """ジョブモジュール名"""

    def set_envoronment(self, env):
        """
        環境変数の初期化。以下のコードで初期化する

            Config().accept(scheduler)

        :param Config env: パラメータ管理オブジェクト
        """
        self.inventory_dir = env.get_inventory_dir()
        self.master_dir    = env.get_master_dir()

    def parser(self):
        """
        コマンド実行オプションの解析
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

    def set_report(self, metric_name, value):
        """
        統計レポート登録
        """
        Stat().regist(metric_name, value, self.module_name)

    def clear_work_directory(self):
        """
        ワーク用ディレクトリ下のファイルを削除する
        """
        for build_dir in ['transfer', 'classify', 'regist']:
            path = "data/work/{}".format(build_dir)
            if os.path.exists(path):
                shutil.rmtree(path)
            os.makedirs(path, exist_ok=True)

    def extract_inventory_data(self, inventory_names):
        """
        複数のインベントリソースからデータを抽出する。
        各インベントリソース下の Excel ファイルから必要なデータを抽出し、
        変換ディレクトリ data/transfer 下に以下の CSV ファイル名でデータ
        を書き込む

            host_list.csv …「検査シート」内の検査対象ホストサマリデータ
            port_list.csv …「検査シート」内のIPアドレス抽出データ
            arp_list.csv  …「ARPテーブルインベントリ」のIPアドレス抽出データ

        :param list inventory_names: インベントリディレクトリリスト
            'data/import' 下のディレクトリをリスト形式で指定する
        """
        logger = logging.getLogger(__name__)
        collector = InventoryCollector()
        inventorys = list()
        for inventory_name in inventory_names:
            inventory_path = inventory_name
            if not os.path.isdir(inventory_name):
                inventory_path = os.path.join(self.inventory_dir, inventory_name)
            inventorys.extend(collector.scan_inventorys(inventory_path))
        # inventorys = collector.scan_inventorys('data/import/project1')
        # inventorys.extend(collector.scan_inventorys('data/import/net1'))
        Stat().regist('0.インベントリソース', len(inventorys), self.module_name)
        inventory_tables = collector.load(inventorys)
        inventory_tables.save_csv('data/work/transfer')

    def read_inventory_dat(self, csv_file, metric_name):
        """
        インベントリ変換データの読込み
        変換ディレクトリ data/transfer 下の CSV ファイルを読み込む

        :param str csv_file: 入力CSVファイル名
        :param str metric_name: 読込み行数統計値のメトリック名
        """
        csv_path = os.path.join('data/work/transfer', csv_file)
        df = pd.read_csv(csv_path) if os.path.exists(csv_path) else pd.DataFrame()
        Stat().regist(metric_name, len(df), self.module_name)
        return df

    def get_ticket_manager(self, tracker, filter = 'server'):
        """
        指定した Redmine トラッカー名のチケット登録マネージャを取得する

        :param str tracker: トラッカー名
        :param str metric_name: 読込み行数統計値のメトリック名
        """
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

    def transfer():
        """
        インベントリ変換データと台帳のつき合わせをする。

        入力データ：'data/transfer'下のインベントリデータ(前処理で実施)
        出力データ：'data/classify'下のCSV

        前処理で抽出したインベントリデータと、台帳データを読み込み、
        データのつき合わせ(マージ)を行う。
        つき合わせをした結果は'data/classify'下に保存する。
        """
        pass

    def classify():
        """
        前処理のつき合わせ結果からデータの分類をする

        入力データ：'data/classify'下のデータつき合わせ結果
        出力データ：'data/regist 下のCSV
        """
        pass

    def regist():
        """
        前処理のデータ分類結果をデータベースに登録する

        入力データ：'data/work/regist'下のデータ分類結果
        出力データ： Redmine データベース
        """
        pass

    def main(self):
        """
        メイン処理。インベントリデータの変換とデータベース登録
        """

        logging.basicConfig(
            level=getattr(logging, 'INFO'),
            format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
            datefmt='%Y/%m/%d %H:%M:%S',
        )
        logger = logging.getLogger(__name__)

        args = self.parser()

        Stat().create_report_id()
        self.clear_work_directory()
        self.extract_inventory_data(args.inventory_names)
        self.transfer()
        self.classify()
        Stat().show()
        if args.skip_regist:
            return
        try:
            redmine_stat = RedmineStatistics()
            redmine_stat.reset()
            self.regist(default_site = args.default_site)
            redmine_stat.show()
        except:
              print("Database registration error")

