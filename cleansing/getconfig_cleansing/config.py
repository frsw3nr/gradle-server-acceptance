"""Getconfig インベントリ収集結果管理用

'clensing.ini' ファイルを読み込み、get()関数で指定したパラメータ値を返します。

Example:

.. highlight:: python
.. code-block:: python

    print(Config().get('Redmine','API_KEY'))
    print(Config().get_boolean('JobOptions', 'USE_CACHE'))

"""

import logging
import configparser
import codecs
from getconfig_cleansing.singleton import singleton

@singleton
class Config():

    def __init__(self):
        config = configparser.ConfigParser()
        with open('cleansing.ini', 'r', encoding='shift_jis') as f:
            config.read_file(f)
        self.config = config

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
