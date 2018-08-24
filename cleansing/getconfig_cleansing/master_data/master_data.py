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

