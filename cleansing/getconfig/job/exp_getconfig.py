"""Getconfig インベントリ収集結果管理用

Getconfig エビデンスをロードし、ロードした結果をJSONファイルに変換して保存します

詳細は、'getconfig/readme.md' を参照してください

"""
import re
import os
import logging
import datetime
from argparse import ArgumentParser
from getconfig.config import Config
from getconfig.stat import Stat
from getconfig.inventory.old.evidence_old import GetconfigEvidence

def parser():
    """
    実行オプション解析
    """
    usage = 'python {} [-s <./getconfig>] [-t <./build/log>][--help]'\
            .format(__file__)
    argparser = ArgumentParser(usage=usage)
    argparser.add_argument('-s', '--source', type=str,
                           dest='source_dir',
                           help='Getconfig result source directory')
    argparser.add_argument('-t', '--target', type=str,
                           dest='export_dir',
                           help='JSON export directory')
    argparser.set_defaults(source_dir='getconfig')
    argparser.set_defaults(export_dir='build/log')
    return argparser.parse_args()

def main():
    args = parser()
    source_dir = args.source_dir
    export_dir = args.export_dir

    for root, dirs, files in os.walk(source_dir):
        for excel_file in files:
            # print(root)
            # match = re.match('(.+)(\\|/)build$', root)
            match = re.match(r'(.+)(\\|/)build$', root)
            if not match:
                continue
            project_home = match.group(1)
            if not re.match('(.+)\.xlsx$', excel_file):
                continue

            excel_path = os.path.join(project_home, 'build', excel_file)
            print("excel_file {}".format(excel_path))
            # export_dir = os.path.join(project_home, 'src/test/resources/log')
            print("export_dir: {}".format(export_dir))
            db = GetconfigEvidence(excel_path, export_dir=export_dir)
            db.load()
            db.export()

    # excel_file = 'tests/resources/getconfig/4at012x/build/チェックシート_20170623_155523.xlsx'
    # db = GetconfigEvidence(excel_file, export_dir='build/tmp')
    # db.load()
    # db.export()

if __name__ == '__main__':
    # ログの初期化
    logging.basicConfig(
        level=getattr(logging, 'INFO'),
        format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
        datefmt='%Y/%m/%d %H:%M:%S',
    )
    logger = logging.getLogger(__name__)

    main()
