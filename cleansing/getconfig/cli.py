import sys
import os
import logging
from argparse import ArgumentParser
# import click
# sys.path.append(os.path.dirname('.') + '/cleansing/')

'''
Usage:

 python getconfig/cli.py --job shipping1 project1

古いパッケージがサイトパッケージなどに入っている場合、スクリプトを更新すると、
ModuleNotFoundError が発生する。一旦サイトパッケージの管理をなしにする
pip uninstall getconfigcleansing

チェックシートを読み込んで、build/cleansing_dat/master の下に保存する

Usage :
    gctool [--debug] load <ディレクトリ> --grep <キーワード>
    gctool [--debug] load <Excelパス>

Example:
   gctool --debug load build/check_sheet.xlsx

------------------------------------------------------------------------------
New features:
* データクレンジングジョブ [x]
    * コマンド、ディレクトリ構成  [ ]
        gcadmin [--grep <s>] [load|transfer|classify|regist] <path>
        build/dat/(load|transfer|classify|regist) ワークデータ
        cleansing/getconfig_cleansing Python ライブラリ

'''

def parser():
    """
    実行オプション解析
    """
    usage = 'Usage: gcadmin --job <name> FILE [--grep <name>] [--force] [--load-only]'\
            .format(__file__)
    argparser = ArgumentParser(usage=usage)
    argparser.add_argument('fname', type=str,
                           help='echo fname')
    argparser.add_argument('-j', '--job', type=str,
                           required=True,
                           dest='job',
                           help='Job name')
    argparser.add_argument('-g', '--grep', type=str,
                           dest='keyword',
                           help='grep ticket keyworkd')
    argparser.add_argument('-f', '--force', action='store_true',
                           dest='force', 
                           help='force update')
    argparser.add_argument('-l', '--load-only', action='store_true',
                           dest='load_only', 
                           help='load only')
    argparser.set_defaults(force=False)
    argparser.set_defaults(load_only=False)
    return argparser.parse_args()

def get_job(name):
    _logger = logging.getLogger(__name__)
    try:
        import sys
        sys.path.append("/home/psadmin/work/gradle/gradle-server-acceptance/cleansing")
        # mod = __import__('getconfig.job.template.scheduler_' + name,
        #                  None, None, ['Scheduler'])
        mod = __import__('getconfig.job.template.scheduler_' + name, None, None, ['Scheduler'])
        # mod = __import__('getconfig.job.scheduler_' + name)
    except ImportError:
        import sys
        print(sys.path)
        import traceback
        traceback.print_exc()
        return
    return mod

def main():
    """全ポートリストとサーバ管理台帳、ネットワーク管理台帳と突合せをして結果を保存する"""

    # opts = dict(
    #     use_cache    = Config().get_boolean('JobOptions', 'USE_CACHE'),
    #     skip_redmine = Config().get_boolean('JobOptions', 'SKIP_REDMINE'),
    #     force_update = Config().get_boolean('JobOptions', 'FORCE_UPDATE'),
    #     filter_classify_file = Config().get('JobOptions', 'FILTER_CLASSIFY_FILE'),
    # )

    _logger = logging.getLogger(__name__)
    """変換処理統計サマリレポート"""
    # Stat().create_report_id()

    args = parser()
    job = get_job(args.job)
    if not job:
        # _logger.error("Job not found 'getconfig/job/template/scheduler_{}.py'".format(args.job))
        _logger.error("Job not found 'getconfig/job/scheduler_{}.py'".format(args.job))
        return -1

    inventory_source = os.path.join('data/import', args.fname)
    if not os.path.isdir(inventory_source):
        _logger.error("Inventory not found '{}'".format(inventory_source))
        return -1

    _logger.info("Load inventory '{}'".format(inventory_source))
    scheduler = job.Scheduler(inventory_source)
    # scheduler.test()
    scheduler.transfer()
    if not args.load_only:
        scheduler.regist()

    # # 登録処理統計サマリレポート
    # Stat().show_summary()
    # RedmineStatistics().show()

if __name__ == '__main__':
    # ログの初期化
    logging.basicConfig(
        level=getattr(logging, 'INFO'),
        format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
        datefmt='%Y/%m/%d %H:%M:%S',
    )
    logger = logging.getLogger(__name__)

    main()
