import re
import sys
import os
import math
import logging
from enum import Enum
from datetime import datetime
from abc import ABCMeta, abstractmethod
import json
import numpy as np
import pandas as pd
import dataset as ds
from dataset.types import Types

"""
Getconfig エビデンスサマリーシート検索

使用例：

    python cleansing/getconfig/summary_sheet.py .\
        ./getconfig/AT0043G/build/サーバチェックシート_20180803_152456.xlsx
"""

class EvidenceParser():
    def say(self, name):
        print("サーバチェックシート: %s" % (name))
