"""Getconfig　バッチ実行スクリプト

指定したコンフィグファイルを基に、Getconfigのインベントリ収集からDB登録までを
バッチ実行します。
実行済みの Getconfig インベントリ収集プロジェクトを定期実行する場合に使用します。

Parametrs
---------
-c config_paths : str[]
    Getconfig コンフィグファイルパスを指定します。
    HW, OSなど複数インベントリを収集する場合は、リスト形式で指定してください。
    例：python .\\gcbat.py \
        -c .\\template\\Cisco_UCS\\config_ucs.groovy,.\\config\\config.groovy
-l collect_level : int
    Getconfig インベントリ収集レベルを指定します。既定は、1
-d dry_run : bool
    True の場合、実行コマンドの出力のみの予行演習モードで実行します。既定は False
-r redmine_id : str
    チケット登録する Redmine のプロジェクトを指定します。
    未指定の場合は、REDMINE_PROJECT 環境変数を選択し、環境変数がない場合は、
    Redmine 既定プロジェクトを選択します。

Usage
-----

Cisco UCS のサーバのインベントリ収集、登録する。

    gcbat.py  -c .\\template\\Cisco_UCS\\config_ucs.groovy,.\\config\\config.groovy

コンフィグファイルは絶対パスでの指定も可能です。パスからホームを検索します。

     gcbat.py -c C:\\cleansing_data\\import\\v2.4\\test2\\config\\config.groovy


"""

import re
import sys
import os
import logging
import pathlib
import subprocess
import argparse

class GetconfigExecuter():
    GETCONFIG_TIMEOUT = 300

    def set_envoronment(self, args):
        """
        環境変数の初期化。以下のコードで初期化する

        """
        _logger = logging.getLogger(__name__)
        self.config_paths = args.config.split(',')
        self.base_config = self.config_paths[0]
        self.collect_level = args.level
        self.dry_run = args.dry
        self.redmine = args.redmine
        if not self.redmine:
            self.redmine = os.environ.get('REDMINE_PROJECT')
        self.home = self.get_home(self.base_config)
        if not self.home:
            _logger.critical('config path not found')
            sys.exit(1)
        else:
            _logger.info('set home : {}'.format(self.home))

    def check_os_config(self, config_path):
        """
        config_path が OS用途の場合は、既定の設定として登録する
        """
        if re.search(r'[/|\\]config\.groovy', config_path) or \
           re.search(r'[/|\\]config_(aix|solaris|esxi)\.groovy', config_path):
            self.base_config = config_path

    def get_home(self, config_path):
        """
        Getconfg ホームを、{home}/ccnfig/config.groovy のパスから検索する
        """
        home = None
        path = str(pathlib.Path(config_path).resolve())
        match_dir = re.search(r'^(.+?)[/|\\](config|template)[/|\\]', path)
        if match_dir:
            home = match_dir.group(1)
        return home

    def get_cmd_base(self, config_path):
        """
        getconfig -c config.groovy コマンドラインを取得する。
        Getconfi ホームから実行するよう、 config_path を相対パスに変更する。
        """
        path = str(pathlib.Path(config_path).resolve())
        return "getconfig.bat -c {}".format(path.replace(self.home, '.'))

    def spawn(self, command):
        """
        外部コマンドを実行する。終了コードエラー、タイムアウトの場合は例外を発生する。
        """
        _logger = logging.getLogger(__name__)
        _logger.info("run : {}".format(command))
        if not self.dry_run:
            subprocess.check_call(command.split(), cwd=self.home, \
                timeout=self.GETCONFIG_TIMEOUT)

    def spawn_get_inventory(self, config_path):
        """
        インベントリ収集コマンド getconfig -c config.groovy 実行。
        """
        cmd_base = self.get_cmd_base(config_path)
        self.spawn(cmd_base + " --level {}".format(self.collect_level))
        self.spawn(cmd_base + " -u local")

    def spawn_regist_redmine(self):
        """
        Redmine チケット登録コマンド getconfig -c config.groovy -r 実行。
        """
        cmd_base = self.get_cmd_base(self.base_config)
        if self.redmine:
            self.spawn(cmd_base + " -rp {}".format(self.redmine))
        else:
            self.spawn(cmd_base + " -r")

    def spawn_regist_inventory_db(self):
        """
        インベントリDB登録コマンド getconfig -c config.groovy -u db-all 実行。
        """
        cmd_base = self.get_cmd_base(self.base_config)
        self.spawn(cmd_base + " -u db-all")

    def run(self):
        """
        Getconfigのインベントリ収集からDB登録までをバッチ実行する。
        """
        _logger = logging.getLogger(__name__)
        try:
            for config_path in self.config_paths:
                cmd_base = self.get_cmd_base(config_path)
                self.check_os_config(config_path)
                self.spawn_get_inventory(config_path)

            self.spawn_regist_redmine()
            self.spawn_regist_inventory_db()
        except Exception as e:
              print("Command error :{}".format(e.args))

    def parser(self):
        """
        コマンド実行オプションの解析
        """
        parser = argparse.ArgumentParser()
        parser.add_argument("-c", "--config", type = str, required = True, 
                            help = "<path>\\config.groovy, ...")
        parser.add_argument("-l", "--level", type = int, default = 1, 
                            help = "collection level")
        parser.add_argument("-d", "--dry", action="store_true", 
                            help = "dry run")
        parser.add_argument("-r", "--redmine", type = str,
                            help = "redmine project id")
        return parser.parse_args()

    def main(self):
        logging.basicConfig(
            level=getattr(logging, 'INFO'),
            format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
            datefmt='%Y/%m/%d %H:%M:%S',
        )
        args = self.parser()
        self.set_envoronment(args)
        self.run()

if __name__ == '__main__':
    GetconfigExecuter().main()
