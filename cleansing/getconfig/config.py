"""Getconfig インベントリ収集結果管理用

'clensing.ini' ファイルを読み込み、get()関数で指定したパラメータ値を返します。

[Getconfig]
ディレクトリ関連
    GETCONFIG_HOME  本体ホーム
    PROJECT_HOME    プロジェクトホーム
    INVENTORY_DIR   入力ディレクトリ
    RESULT_DIR      出力ディレクトリ

[Redmine]
Redmine 接続情報
    REDMINE_API_KEY Redmine APIキー
    REDMINE_URL     Redmine 接続 URL
Redmine キャッシュ
    CMDB_URL        MySQL キャッシュDB 接続 URL
    
[JobOptions]
入力オプション
    DRY_RUN          予行演習
    VERIFY_TEST      デバッグ関連
    FILTER_INVENTORY インベントリファイルキーワード
    USE_OUTER_JOIN
    FETCH_FIRST_FEW_LINES
    USE_CACHE
    SKIP_REDMINE
    FORCE_UPDATE
    FILTER_CLASSIFY_FILE

Example:

.. highlight:: python
.. code-block:: python

    print(Config().get('Redmine','API_KEY'))
    print(Config().get_boolean('JobOptions', 'USE_CACHE'))

"""

import os
import logging
import configparser
import codecs
from getconfig.singleton import singleton

@singleton
class Config():

    def __init__(self, config_path = 'cleansing.ini'):
        config = configparser.ConfigParser()
        with open(config_path, 'r', encoding='shift_jis') as f:
            config.read_file(f)
        self.config = config
        self.getconfig_home   = os.environ.get('GETCONFIG_HOME') or \
            self.get_with_default('Getconfig', 'GETCONFIG_HOME', '/opt/server-acceptance')
        self.project_home     = os.getcwd()
        self.inventory_dir    = os.path.join(self.project_home, 'build/inventory')
        self.result_dir       = os.path.join(self.project_home, 'build/result')
        self.redmine_api_key  = os.environ.get('REDMINE_API_KEY') or \
            self.get_with_default('Redmine', 'API_KEY', None)
        self.redmine_url      = os.environ.get('REDMINE_URL') or \
            self.get_with_default('Redmine', 'URL', None)
        self.cmdb_url         = os.environ.get('REDMINE_CMDB') or \
            self.get_with_default('Redmine', 'CMDB', None)
        self.dry_run          = False
        self.filter_inventory = None

    def set(self, section_name, parameter_name, value):
        self.config[section_name][parameter_name] = value

    def get(self, section_name, parameter_name):
        return self.config[section_name][parameter_name]

    def get_boolean(self, section_name, parameter_name):
        return self.config.getboolean(section_name, parameter_name)

    def get_int(self, section_name, parameter_name):
        value = self.config.get(section_name, parameter_name)
        if value.isdigit():
            return int(value)
        else:
            return 0

    def get_with_default(self, section, name, default):
        if self.config.has_option(section, name):
            return self.config.get(section, name)
        else:
            return default

    def set_getconfig_home(self, getconfig_home):
        # 本体ホーム
        self.getconfig_home = getconfig_home

    def set_project_home(self, project_home):
        # プロジェクトホーム(カレントディレクトリ)
        self.project_home = project_home

    def set_inventory_dir(self, inventory_dir):
        # 入力ディレクトリ(カレントディレクトリ/build/inventory)
        self.inventory_dir = inventory_dir

    def set_result_dir(self, result_dir):
        # 出力ディレクトリ(カレントディレクトリ/build/result)
        self.result_dir = result_dir

    def set_redmine_api_key(self, redmine_api_key):
        self.redmine_api_key = redmine_api_key

    def set_redmine_url(self, redmine_url):
        self.redmine_url = redmine_url

    def set_cmdb_url(self, cmdb_url):
        self.cmdb_url = cmdb_url

    def set_dry_run(self, dry_run):
        # 予行演習
        self.dry_run = dry_run

    def set_filter_inventory(self, filter_inventory):
        # インベントリファイルキーワード
        self.filter_inventory = filter_inventory

    def get_getconfig_home(self):
        # 本体ホーム
        return self.getconfig_home

    def get_project_home(self):
        # プロジェクトホーム(カレントディレクトリ)
        return self.project_home

    def get_inventory_dir(self):
        # 入力ディレクトリ(カレントディレクトリ/build/inventory)
        return self.inventory_dir

    def get_result_dir(self):
        # 出力ディレクトリ(カレントディレクトリ/build/result)
        return self.result_dir

    def get_redmine_api_key(self):
        return self.redmine_api_key

    def get_redmine_url(self):
        return self.redmine_url

    def get_cmdb_url(self):
        return self.cmdb_url

    def get_dry_run(self):
        # 予行演習
        return self.dry_run

    def get_filter_inventory(self):
        # インベントリファイルキーワード
        return self.filter_inventory

    def accept(self, visitor):
        visitor.set_envoronment(self)

if __name__ == '__main__':
    # ログの初期化
    logging.basicConfig(
        level=getattr(logging, 'INFO'),
        format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
        datefmt='%Y/%m/%d %H:%M:%S',
    )
    logger = logging.getLogger(__name__)

    print(Config().config.sections())
    print(Config().get('Redmine','API_KEY'))
    # print(Config().get('Redmine','Hoge'))
    print(Config().get_boolean('JobOptions', 'USE_CACHE'))
    print(Config().get_boolean('JobOptions', 'SKIP_REDMINE'))
    # print(Config().get_boolean('JobOptions', 'Hoge', False))
    print(Config().get_int('JobOptions','FETCH_FIRST_FEW_LINES'))
