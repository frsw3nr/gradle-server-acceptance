import re
import sys
import os
import logging
from abc import ABCMeta, abstractmethod
import numpy as np
import pandas as pd
from getconfig.merge_master import MergeMaster
from getconfig.inventory.info import InventoryInfo
from getconfig.inventory.loader import InventoryLoader

class Scheduler(metaclass=ABCMeta):
    INVENTORY_DIR = 'build'

    def __init__(self, inventory_source = INVENTORY_DIR, **kwargs):
        self.inventory_source = inventory_source

    def load():
        '''データロード'''

    def transfer():
        '''データ変換'''

    def classify():
        '''データ分類'''

    def regist():
        '''データ登録'''

