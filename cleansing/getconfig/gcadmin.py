import re
import sys
import os
import logging
# from abc import ABCMeta, abstractmethod
import numpy as np
import pandas as pd
import click

from getconfig.merge_master import MergeMaster
from getconfig.inventory.info import InventoryInfo
from getconfig.inventory.loader import InventoryLoader

@click.command()
@click.option('-i', '--ifastq', 'ifastq', help='Input file in FASTQ file.')
@click.option('-o', '--output-prefix', 'output', help='Prefix of output file.')

# class GetconfigAdmin(metaclass=ABCMeta):
class GetconfigAdmin():
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

    def get_scheduler(self, name):
        _logger = logging.getLogger(__name__)
        try:
            mod = __import__('getconfig.job.scheduler_' + name,
                             None, None, ['Scheduler'])
        except ImportError:
            return
        return mod

    def cmd(ifastq, output):
        click.echo(ifastq)

if __name__ == '__main__':
    # ログの初期化
    logging.basicConfig(
        level=getattr(logging, 'INFO'),
        format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
        datefmt='%Y/%m/%d %H:%M:%S',
    )
    logger = logging.getLogger(__name__)
    # getconfig.job.scheduler_test1
    # getconfig/job/scheduler_test1.py
    scheduler = GetconfigAdmin().get_scheduler('test1')
    # print(dir(scheduler))
    # scheduler.Scheduler().test()
    # scheduler.regist()
    scheduler.cmd()
