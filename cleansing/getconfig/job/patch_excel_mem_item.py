"""Getconfig インベントリ収集結果管理用

Getconfig エビデンスをロードし、ロードした結果をJSONファイルに変換して保存します

詳細は、'getconfig/readme.md' を参照してください

"""
import re
import os
import logging
import datetime
import shutil
import openpyxl
import pprint
from argparse import ArgumentParser
from getconfig.config import Config
from getconfig.stat import Stat
import warnings
# from getconfig.inventory.old.evidence_old import GetconfigEvidence
from getconfig.inventory.old.evidence_v1 import GetconfigEvidenceV1

def parser():
    """
    実行オプション解析
    """
    usage = 'python {} [-s <./getconfig>][--help]'\
            .format(__file__)
    argparser = ArgumentParser(usage=usage)
    argparser.add_argument('-s', '--source', type=str,
                           dest='source_dir',
                           help='Getconfig result source directory')
    argparser.set_defaults(source_dir=Config().get_inventory_dir())
    argparser.set_defaults(export_dir='node')
    return argparser.parse_args()

def backup_excel(excel_path):
    backup_excel_path = excel_path + "_bak"
    print("backup {}".format(backup_excel_path))
    shutil.copy(excel_path, backup_excel_path)

def patch_excel_mem_item(excel_path, mem_position_from, mem_position_to):
    try:
        wb = openpyxl.load_workbook(excel_path)
        if not '検査レポート' in wb.sheetnames:
            return
        sheet = wb['検査レポート']
        cell = sheet[mem_position_from]
        if cell.value == 'MEM容量':
            sheet[mem_position_to] = 'MEM容量'
            sheet[mem_position_from] = ''
            print("patch {}".format(excel_path))
            wb.save(excel_path)
    except TypeError as e:
        print("error:", e)

def main():
    args = parser()
    for root, dirs, files in os.walk(args.source_dir):
        for excel_file in files:
            excel_path = os.path.join(root, excel_file)
            if excel_file == 'サーバチェックシート.xlsx':
                backup_excel(excel_path)
                patch_excel_mem_item(excel_path, 'K5', 'M5')

            if excel_file == 'Solarisチェックシート.xlsx':
                backup_excel(excel_path)
                patch_excel_mem_item(excel_path, 'J5', 'L5')

warnings.filterwarnings("ignore")
if __name__ == '__main__':
    # ログの初期化
    logging.basicConfig(
        level=getattr(logging, 'INFO'),
        format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
        datefmt='%Y/%m/%d %H:%M:%S',
    )
    logger = logging.getLogger(__name__)

    # excel_path = 'C:\\cleansing_data\\import\\zabbix_test\\サーバチェックシート.xlsx'
    # excel_path = 'C:\\cleansing_data\\import\\v2.4\\test2\\サーバチェックシート.xlsx'
    # excel_path = 'C:\\cleansing_data\\import\\zabbix_test\\template\\Solaris\\Solarisチェックシート.xlsx'
    # backup_excel(excel_path)
    # patch_excel_mem_item(excel_path, 'J5', 'L5')
    main()

