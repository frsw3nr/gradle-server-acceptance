"""``出荷情報`` の台帳ロード。

出荷時の出荷情報の履歴リストを読み込みます。

* '引渡し月','保守契約終了日','EOSL'の日付属性は、Redmine用に 'YYYY-MM-DD'形式に変換する

Example:

.. highlight:: python
.. code-block:: python

    from getconfig_cleansing.master_data.template.ship_list import MasterDataShipList
    df = MasterDataShipList().load_all()
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
from getconfig_cleansing.master_data.master_data import MasterData

@singleton
class MasterDataShipList(MasterData):
    """マスターファイルを読み込み、データをキャッシュする"""

    master_data_dir = 'master/shipping'
    """台帳ファイルディレクトリ"""

    header_row = 2
    """台帳シートのヘッダ行位置"""

    output_csv = 'ship_list.csv'
    """変換データのCSV出力ファイル"""

    def load_setup(self, df, **kwargs):
        """台帳読み込み時のデータクレンジング処理"""
        df = df.dropna(subset=['搬入日']) # 搬入日未登録は削除
        return df

    def load_all_setup(self, df, **kwargs):
        """全ての台帳読み込み後のデータクレンジング処理"""
        df = df.drop_duplicates(['ホスト名'], keep='last')
        df = df.assign(搬入日=df.apply(
                lambda x: Util().reform_date(x['搬入日']), axis=1))
        df.rename(columns={'発番': 'ジョブ名'}, inplace=True)
        return df

if __name__ == '__main__':
    # ログの初期化
    logging.basicConfig(
        level=getattr(logging, 'INFO'),
        format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
        datefmt='%Y/%m/%d %H:%M:%S',
    )
    logger = logging.getLogger(__name__)

    df = MasterDataShipList().load_all()
    Util().save_data(df, '/tmp', 'ship_list.csv')
