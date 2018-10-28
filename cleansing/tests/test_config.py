import re
import sys
import os
import pytest
from click.testing import CliRunner
from getconfig.config import Config
from getconfig.inventory.collector import InventoryCollector
from getconfig.inventory.loader import InventoryLoader

# py.test tests/test_config.py -v --capture=no -k test_config1

# 以下の順に設定パラメータをセットする。後から上書きする

# 1. 設定ファイルの読み込み(設定ファイルの指定がある場合)
# 2. 実行オプションの読み込み
# 3. コードで直接指定

# 1-3で設定されていないければデフォルト値を返す

def test_config1():
    print(Config().config.sections())
    print(Config().get_getconfig_home())
    print(Config().get_project_home())
    print(Config().get_inventory_dir())
    print(Config().get_master_dir())

    # assert Config().get_getconfig_home() == '/opt/server-acceptance'

def test_config2():
    Config().set_inventory_dir('/tmp')
    print(Config().get_getconfig_home())
    print(Config().get_inventory_dir())

    assert Config().get_inventory_dir() == '/tmp'
    # assert Config().get_getconfig_home() == '/opt/server-acceptance'

def test_config3():
    collector = InventoryCollector()
    Config().accept(collector)
    print(collector.dry_run)

    assert collector.dry_run == False

def test_config4():
    config_path = Config().find_config_path('cleansing.ini')
    print("CONFIG1:{}".format(config_path))
    print("CONFIG2:{}".format(Config().config_path))
