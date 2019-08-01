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

python getconfig/job/template/scheduler_software1.py \
    --default-site test1 \
    project3
'''

class Scheduler(SchedulerBase):
    module_name = 'MWインベントリ登録'

    def transfer(self):
        """
        インベントリ変換データと台帳のつき合わせをする。

        入力データ：'data/work/transfer'下のインベントリデータ(前処理で実施)
        出力データ：'data/work/classify'下のCSV

        前処理で抽出したインベントリデータと、台帳データを読み込み、
        データのつき合わせ(マージ)を行う。
        つき合わせをした結果は'data/work/classify'下に保存する。
        """
        logger = logging.getLogger(__name__)

        # インベントリ読込み
        hosts = self.read_inventory_dat('host_list.csv', '1.インスタンス数')
        # 台帳読込み
        soft_list = MasterDataSoftwareList().load_all()
        # ホストと台帳のつき合わせ
        hosts = MergeMaster().join_by_host(hosts, soft_list, 'ジョブ名', 'outer')
        Util().save_data(hosts, 'data/work/classify', 'softwares.csv')
        self.set_report('1.ミドルウェア管理台帳行数', len(soft_list))

    def classify(self):
        """
        前処理のつき合わせ結果からデータの分類をする

        入力データ：'data/work/classify'下のデータつき合わせ結果
        出力データ：'data/work/regist 下のCSV
        """
        df = pd.read_csv('data/work/classify/softwares.csv')
        df['tracker'] = df.apply(lambda x: Util().analogize_tracker(x['ドメイン']),
                                 axis=1)
        df = df[(df['tracker'] == 'ソフトウェア')]
        # print(df.loc[:, ['tracker','ホスト名']])
        self.set_report('7.ミドルウェア数', len(df))
        Util().save_data(df, 'data/work/regist', 'softwares.csv')

    def regist(self, **kwargs):
        '''データ登録'''
        logger = logging.getLogger(__name__)
        port_list = pd.read_csv('data/work/regist/softwares.csv')
        port_list_sets = port_list.groupby(by='ホスト名')

        # 指定キーでグルーピングしてホストを登録
        for hostname, host_list_set in port_list_sets.first().iterrows():
            host_list_set['ホスト名'] = hostname
            tracker = host_list_set['tracker']
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

if __name__ == '__main__':
    Scheduler().main()
