import re
import sys
import os
import logging
import subprocess
import argparse

class GetconfigExecuter():

    def set_envoronment(self, args):
        """
        環境変数の初期化。以下のコードで初期化する

        """
        self.config_path = args.config
        self.collect_level = args.level
        self.project_id = "k1"

    def run(self):
        _logger = logging.getLogger(__name__)
        cmd_base = "getconfig.bat -c {}".format(self.config_path)
        try:
            _logger.info("run getconfig : {}".format(cmd_base))
            subprocess.check_call(cmd_base.split())
            redmine_cmd = cmd_base + " -r {}".format(self.project_id)
            _logger.info("run redmine update : {}".format(redmine_cmd))
            subprocess.check_call(redmine_cmd.split())
            updatedb_cmd = cmd_base + " -u db-all"
            _logger.info("run inventory update : {}".format(updatedb_cmd))
            subprocess.check_call(updatedb_cmd.split())
        except Exception as e:
              print("Command error :{}".format(e.args))

    def parser(self):
        """
        コマンド実行オプションの解析
        """
        parser = argparse.ArgumentParser()
        parser.add_argument("-c", "--config", type = str, required = True, 
                            help = "<path>\\connfig.groovy")
        parser.add_argument("-l", "--level", type = int, default = 0, 
                            help = "collection level")
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
