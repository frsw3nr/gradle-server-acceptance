import re
import sys
import os
import shutil
import logging
import numpy as np
import pandas as pd
import dataset as ds
from getconfig.util         import Util
from getconfig.merge_master import MergeMaster
from getconfig.job.scheduler_base  import SchedulerBase
from getconfig.master_data.template.job_list  import MasterDataJobList
from getconfig.master_data.template.ship_list import MasterDataShipList
from getconfig.master_data.template.mw_list   import MasterDataSoftwareList
from getconfig.master_data.template.net_list  import MasterDataNetworkList
from getconfig.ticket.template.ticket_port_list import TicketPortList
from getconfig.ticket.template.ticket_relation  import TicketRelation

'''
各種インベントリ、台帳を読み込んで、データのつき合わせをし、
変換した結果をデータベースに登録する。
'data/import'ディレクトリ下の各種インベントリの読込み処理実行後、
以下の処理を順に行う

1. transfer() ・・・ 各種台帳の読込みとつき合わせ
2. classify() ・・・ つき合わせ結果の分類(絞込み)
3. regist() ・・・ データベース登録

テスト方法

bundle exec bin/rake db:migrate:reset
bundle exec bin/rake redmine:plugins:migrate RAILS_ENV=production

redmine/plugins 下の README.rdoc の手順の通りRedmine 初期設定

test1 プロジェクトを作成して、登録スクリプト実行

python getconfig/job/template/scheduler_shipping1.py \
    --default-site test1 \
    project1 project2 net1
'''

class Scheduler(SchedulerBase):
    module_name = '出荷機器登録'
    """ジョブモジュール名"""

    def transfer(self):
        """
        インベントリ変換データと台帳のつき合わせをする。

        入力データ：'data/work/transfer'下のインベントリデータ(前処理で実施)
        出力データ：'data/work/classify'下のCSV

        前処理で抽出したインベントリデータと、台帳データを読み込み、
        データのつき合わせ(マージ)を行う。
        つき合わせをした結果は'data/work/classify'下に保存する。
        """

        # 各種インベントリデータの読込み。存在しない場合はemptyを取得する。
        hosts = self.read_inventory_dat('host_list.csv', '1.インベントリ機器総数')
        ports = self.read_inventory_dat('port_list.csv', '2.インベントリ抽出IP数')
        arp_tables = self.read_inventory_dat('arp_list.csv',
                                   '3.インベントリARPテーブル抽出IP数')

        # 案件情報、出荷台帳、ネットワーク台帳の読込み
        job_list   = MasterDataJobList().load_all()      # サーバ案件管理台帳
        ship_list  = MasterDataShipList().load_all()     # サーバ出荷台帳
        soft_list  = MasterDataSoftwareList().load_all() # ソフトウェア管理台帳
        net_list   = MasterDataNetworkList().load_all()  # ネットワーク管理台帳
        # arp_tables = MasterDataPortList().load_all()   # ポートリスト
        # mac_vendor = MasterDataMacVendor().load_all()  # MACアドレスベンダリスト

        # 'ジョブ名'をキーに'ホストインベントリ'と'案件台帳'のつき合わせ
        hosts = MergeMaster().join_by_host(hosts, job_list, 'ジョブ名', 'left')
        # 'ホスト名名'をキーに'ホストインベントリ'と'出荷台帳'のつき合わせ
        hosts = MergeMaster().join_by_host(hosts, ship_list, 'ホスト名', 'left')
        # 'IP'をキーに'ARPテーブルIP'と'インベントリ抽出IP'のつき合わせ
        ports = MergeMaster().join_by_host(ports, arp_tables, 'IP', 'left')
        # 'スイッチ名'をキーに'ネットワーク台帳'と'インベントリ抽出IP'のつき合わせ
        ports = MergeMaster().join_by_host(ports, net_list, 'スイッチ名', 'left')

        # # MACアドレスの先頭6桁をキーにベンダー情報をルックアップ
        # # ports['MAC6'] = ports['MACアドレス'].str.replace('.','').str[:6]
        # # ports = pd.merge( ports, mac_vendor, on='MAC6', how='left' )

        # 'ホストインベントリ'と'インベントリ抽出IP'をマージ
        df = MergeMaster().join_by_host(hosts, ports, 'ホスト名', 'left')

        # 変換結果を保存。後のclassify()処理で読み込む
        Util().save_data(df, 'data/work/classify', 'hosts.csv')
        self.set_report('5.出荷機器つき合わせ台数', len(df.groupby(by=['ホスト名'])))
        self.set_report('6.出荷機器つき合わせIP数', len(df))

    def classify(self):
        """
        前処理のつき合わせ結果からデータの分類をする

        入力データ：'data/work/classify'下のデータつき合わせ結果
        出力データ：'data/work/regist 下のCSV
        """
        df = pd.read_csv('data/work/classify/hosts.csv')
        df['tracker'] = df.apply(lambda x: Util().analogize_tracker(x['ドメイン']),
                                 axis=1)
        df = df[(df['tracker'] == 'IAサーバ') | \
                (df['tracker'] == 'SPARCサーバ') | \
                (df['tracker'] == 'POWERサーバ') | \
                (df['tracker'] == 'ストレージ')]
        # print(df.loc[:, ['tracker','ホスト名']])
        self.set_report('7.サーバ機器つき合わせIP数', len(df))
        Util().save_data(df, 'data/work/regist', 'hosts.csv')

    def regist(self, **kwargs):
        """
        前処理のデータ分類結果をデータベースに登録する

        入力データ：'data/work/regist'下のデータ分類結果
        出力データ： Redmine データベース
        """
        logger = logging.getLogger(__name__)

        port_list = pd.read_csv('data/work/regist/hosts.csv')
        port_list_sets = port_list.groupby(by='ホスト名')

        # 指定キーでグルーピングしてホストを登録
        for hostname, host_list_set in port_list_sets.first().iterrows():
            host_list_set['ホスト名'] = hostname
            # tracker = Util().analogize_tracker(host_list_set['ドメイン'])
            tracker = host_list_set['tracker']
            ticket_manager = self.get_ticket_manager(tracker, 'server')
            if not ticket_manager:
                continue
            default_site = kwargs.get('default_site', '場所不明')
            site = Util().analogize_site(host_list_set['サイト'], default_site)

            host = ticket_manager.regist(site, hostname, host_list_set)
            logger.info ("Regist {} : {}({})".format(tracker, hostname, host['id']))

            # 接続しているポートを登録
            for port_id, port_list_set in port_list_sets.get_group(hostname).iterrows():
                ip_address = port_list_set['IP']
                port = TicketPortList().regist(site, ip_address, port_list_set)
                logger.info ("Regist IP : {}, HOST : {}".format(ip_address, hostname))
                TicketRelation().regist_relation(host['id'], port['id'])

if __name__ == '__main__':
    Scheduler().main()
