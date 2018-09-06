"""``出荷情報`` の台帳ロード。

出荷時の出荷情報の履歴リストを読み込みます。

* '引渡し月','保守契約終了日','EOSL'の日付属性は、Redmine用に 'YYYY-MM-DD'形式に変換する

Example:

.. highlight:: python
.. code-block:: python

    from getconfig.master_data.template.mw_list import MasterDataSoftwareList
    df = MasterDataSoftwareList().load_all()
"""

import re
import os
import logging
import datetime
import numpy as np
import pandas as pd
import dataset as ds
from getconfig.singleton import singleton
from getconfig.stat import Stat
from getconfig.util import Util
from getconfig.master_data.master_data import MasterData

@singleton
class MasterDataSoftwareList(MasterData):
    """マスターファイルを読み込み、データをキャッシュする"""

    master_data_dir = 'master/software'
    """台帳ファイルディレクトリ"""

    header_row = 2
    """台帳シートのヘッダ行位置"""

    output_csv = 'mw_list.csv'
    """変換データのCSV出力ファイル"""

    def load_setup(self, df, **kwargs):
        """台帳読み込み時のデータクレンジング処理"""
        # df = df.dropna(subset=['搬入日']) # 搬入日未登録は削除
        return df

    def load_all_setup(self, df, **kwargs):
        """全ての台帳読み込み後のデータクレンジング処理"""
        df.rename(columns={
                    '設置場所': 'サイト',
                    'システム': '用途',
                    '発番': 'ジョブ名'
                 }, inplace=True)
        return df

if __name__ == '__main__':
    # ログの初期化
    logging.basicConfig(
        level=getattr(logging, 'INFO'),
        format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
        datefmt='%Y/%m/%d %H:%M:%S',
    )
    logger = logging.getLogger(__name__)

    df = MasterDataSoftwareList().load_all()
    Util().save_data(df, '/tmp', 'mw_list.csv')
