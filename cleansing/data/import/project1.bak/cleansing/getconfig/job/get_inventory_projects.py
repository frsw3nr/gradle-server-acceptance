import re
import sys
import os
import logging
import argparse
from getconfig.config import Config

class UtilGetProjects():

    def set_envoronment(self, env):
        """
        環境変数の初期化。以下のコードで初期化する

            Config().accept(scheduler)

        :param Config env: パラメータ管理オブジェクト
        """
        self.inventory_dir = env.get_inventory_dir()
        self.master_dir    = env.get_master_dir()

    def scan_inventory_projects(self, inventory_source, n_import_path = 5):
        project_dirs = dict()
        for project_file in os.listdir(inventory_source):
            path = os.path.join(inventory_source, project_file)
            if os.path.isdir(path):
                timestamp = os.path.getmtime(path)
                project_dirs[project_file] = timestamp
        n = 0
        projects = []
        for k, v in sorted(project_dirs.items(), key=lambda x:x[1], reverse=True):
            n += 1
            if n > n_import_path:
                break
            projects.append(k)
        return projects

    def parser(self):
        """
        コマンド実行オプションの解析
        """
        parser = argparse.ArgumentParser()
        parser.add_argument("-n", "--number", type = int, default = 5, 
                            help = "limit")
        return parser.parse_args()

    def main(self):
        logging.basicConfig(
            level=getattr(logging, 'INFO'),
            format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
            datefmt='%Y/%m/%d %H:%M:%S',
        )
        logger = logging.getLogger(__name__)

        Config().accept(self)
        args = self.parser()
        print("INVENTORY:", self.inventory_dir)
        projects = self.scan_inventory_projects(self.inventory_dir, args.number)
        for project in projects:
            print('project : {}'.format(project))

if __name__ == '__main__':
    UtilGetProjects().main()
