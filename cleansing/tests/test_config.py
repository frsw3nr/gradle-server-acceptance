import pytest
from click.testing import CliRunner
from getconfig_cleansing import config

import re
import sys
import os
import pytest
from click.testing import CliRunner
from getconfig_cleansing.config import Config
from getconfig_cleansing.inventory.collector import InventoryCollector
from getconfig_cleansing.inventory.loader import InventoryLoader

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
    # print(Config().config.get('Getconfig', 'GETCONFIG_HOME'))
    assert 1 == 1
    # assert not result.exception
    # assert result.output.strip() == 'Hello, world.'

