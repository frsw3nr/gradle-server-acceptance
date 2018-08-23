"""各種台帳のロードモジュールのテンプレートクラス。各種台帳ロード処理クラスは本クラスを基底とします
"""

import re
import os
import logging
import datetime
import numpy as np
import pandas as pd
import dataset as ds
from getconfig_cleansing.singleton import singleton
from getconfig_cleansing.stat import Stat
from getconfig_cleansing.util import Util
import warnings
warnings.filterwarnings("ignore", 'This pattern has match groups') # uncomment to suppress the UserWarning

class MasterData():
    base_dir = 'dat'
    """台帳ファイルを読み込むベースディレクトリ"""

    def _save_data(self, data_frame, target_dir, filename):
        """ベース処理用結果ファイルの書き込み。出力先を指定して利用する"""
        logger = logging.getLogger(__name__)
        if not os.path.isdir(target_dir):
            os.makedirs(target_dir)
        target_path = "%s/%s" % (target_dir, filename)
        filename, ext = os.path.splitext(filename)
        data_frame.to_csv(target_path, encoding='utf-8-sig')
        logger.info("結果を '%s' に書き込み %d件" % (target_path, len(data_frame)))

    def save_transfer_data(self, data_frame, filename):
        """変換処理の結果ファイルの書き込み。'build'ディレクトリ下に保存します"""
        target_dir = "build/%s" % (self.fab)
        self._save_data(data_frame, target_dir, filename)

    def save_classify_data(self, data_frame, filename):
        """分類処理の結果ファイルの書き込み。'build/classify'ディレクトリ下に保存します"""
        target_dir = "build/classify"
        self._save_data(data_frame, target_dir, filename)

    def save_asset_data(self, data_frame, filename, fab):
        """保守情報変換処理の結果ファイルの書き込み。'build/{fab} ディレクトリ下に保存します"""
        target_dir = "build/%s" % (fab)
        self._save_data(data_frame, target_dir, filename)

