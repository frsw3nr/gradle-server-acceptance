"""各種台帳のロードモジュールのテンプレートクラス。各種台帳ロード処理クラスは本クラスを基底とします
"""

import re
import os
import logging
import datetime
import numpy as np
import pandas as pd
import dataset as ds
from abc import ABCMeta, abstractmethod
from getconfig.singleton import singleton
from getconfig.stat import Stat
from getconfig.util import Util
from getconfig.config import Config
import warnings
warnings.filterwarnings("ignore", 'This pattern has match groups') # uncomment to suppress the UserWarning

class MasterData(metaclass=ABCMeta):
    # master_dir = 'data'
    master_dir = 'data/master'
    """台帳ファイルを読み込むベースディレクトリ"""

    master_data_dir = 'shipping'
    """台帳ファイルディレクトリ"""

    header_row = 2
    """台帳シートのヘッダ行位置"""

    output_csv = None
    """変換データのCSV出力ファイル"""

    master_cache = None
    """台帳データのキャッシュ"""

    module_name = '台帳収集'

    def set_envoronment(self, env):
        """
        環境変数の初期化。以下のコードで初期化する

            Config().accept(scheduler)

        :param Config env: パラメータ管理オブジェクト
        """
        self.inventory_dir = env.get_inventory_dir()
        self.master_dir    = env.get_master_dir()

    def __init__(self):
        Config().accept(self)

    @abstractmethod
    def load_setup(self, df, **kwargs):
        """台帳読み込み時のデータクレンジング処理"""
        pass

    @abstractmethod
    def load_all_setup(self, df, **kwargs):
        """全ての台帳読み込み後のデータクレンジング処理"""
        pass

    def load(self, excel_file):
        """台帳読み込み"""
        logger = logging.getLogger(__name__)
        master_list = pd.DataFrame()
        file = pd.ExcelFile(excel_file)
        for sheet_name in file.sheet_names:
            logger.info("'{}' のシート '{}' を読み込み".format(excel_file, sheet_name))
            # df = file.parse(sheet_name,skiprows=3, header=self.header_row)
            df = file.parse(sheet_name, header=self.header_row)
            # df = file.parse(header=None)
            if not df.empty:
                df['シート'] = sheet_name
                df = df.replace('-', np.NaN)
                df = df.fillna(method='ffill')
                logger.info("{} 行の読み込み".format(len(df)))
                df = self.load_setup(df)
                if not df.empty:
                    master_list = pd.concat([master_list, df])
        return master_list

    def load_csv(self, csv_file, csv_name):
        """台帳読み込み"""
        logger = logging.getLogger(__name__)
        df = pd.read_csv(csv_file, encoding = 'cp932')
        logger.info("'{}' の読み込み".format(csv_file))
        if not df.empty:
            df['シート'] = csv_name
            df = self.load_setup(df)
            df = df.replace('-', np.NaN)
            df = df.fillna(method='ffill')
        return df

    def load_all(self):
        """指定ディレクトリ下の全台帳読み込み"""
        logger = logging.getLogger(__name__)
        if self.master_cache is None:
            df = pd.DataFrame()
            walk_dir = os.path.join(self.master_dir, self.master_data_dir)
            # print("WALK_DIR:",walk_dir)
            for root, dirs, files in os.walk(walk_dir):
                for file in files:
                    if  re.match('(.+)\.xlsx$', file):
                        excel_file = os.path.join(root, file)
                        df = pd.concat([df, self.load(excel_file)])
                    if  re.match('(.+)\.csv$', file):
                        csv_file = os.path.join(root, file)
                        df = pd.concat([df, self.load_csv(csv_file, file)])
            if not df.empty:
                df = self.load_all_setup(df)
            self.master_cache = df
        return self.master_cache
