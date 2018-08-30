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
from getconfig_cleansing.singleton import singleton
from getconfig_cleansing.stat import Stat
from getconfig_cleansing.util import Util
import warnings
warnings.filterwarnings("ignore", 'This pattern has match groups') # uncomment to suppress the UserWarning

class MasterData(metaclass=ABCMeta):
    base_dir = 'tests/resources'
    """台帳ファイルを読み込むベースディレクトリ"""

    master_data_dir = 'master/shipping'
    """台帳ファイルディレクトリ"""

    header_row = 2
    """台帳シートのヘッダ行位置"""

    output_csv = None
    """変換データのCSV出力ファイル"""

    master_cache = None
    """台帳データのキャッシュ"""

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
            df = file.parse(sheet_name,header=self.header_row)
            df['シート'] = sheet_name
            df = self.load_setup(df)
            df = df.replace('-', np.NaN)
            df = df.fillna(method='ffill')
            master_list = pd.concat([master_list, df])
        return master_list

    def load_all(self):
        """指定ディレクトリ下の全台帳読み込み"""
        logger = logging.getLogger(__name__)
        if self.master_cache is None:
            df = pd.DataFrame()
            walk_dir = os.path.join(self.base_dir, self.master_data_dir)
            for root, dirs, files in os.walk(walk_dir):
                for file in files:
                    if  re.match('(.+)\.xlsx$', file):
                        excel_file = os.path.join(root, file)
                        df = pd.concat([df, self.load(excel_file)])
            df = self.load_all_setup(df)
            self.master_cache = df
        return self.master_cache
