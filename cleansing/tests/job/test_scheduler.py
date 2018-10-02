import re
import sys
import os
import pytest
import pandas as pd
from getconfig.config import Config
from getconfig.inventory.collector import InventoryCollector
from getconfig.job.scheduler_base import SchedulerBase
from getconfig.job.template.scheduler_shipping1 import Scheduler

# py.test tests/job/test_scheduler.py -v --capture=no -k test_scheduler1

# 以下の順に設定パラメータをセットする。後から上書きする

# 1. 設定ファイルの読み込み(設定ファイルの指定がある場合)
# 2. 実行オプションの読み込み
# 3. コードで直接指定

# 1-3で設定されていないければデフォルト値を返す

@pytest.fixture
def config():
    config = Config()
    config.set_inventory_dir('data/import')
    config.set_master_dir('data/master')
    return config

def test_scheduler_init1(config):
    scheduler = SchedulerBase()
    scheduler.clear_work_directory()
    config.accept(scheduler)

    print(scheduler.inventory_dir)
    assert scheduler.inventory_dir == 'data/import'
    assert scheduler.master_dir == 'data/master'

def test_extract_inventory_data1(config):
    scheduler = SchedulerBase()
    scheduler.clear_work_directory()
    config.accept(scheduler)
    scheduler.extract_inventory_data(['project1'])
    hosts = pd.read_csv('data/work/transfer/host_list.csv')
    ports = pd.read_csv('data/work/transfer/port_list.csv')

    assert len(hosts) > 0
    assert len(ports) > 0

def test_scheduler3():
    collector = InventoryCollector()
    Config().accept(collector)
    print(collector.dry_run)

    assert collector.dry_run == False
    