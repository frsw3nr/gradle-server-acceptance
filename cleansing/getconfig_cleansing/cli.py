import sys
import os
import click
sys.path.append(os.path.dirname('.') + '/cleansing/')
from getconfig_cleansing.getconfig.summary_sheet import GetconfigSummarySheet

'''
チェックシートを読み込んで、build/cleansing_dat/master の下に保存する

Usage :
    gctool [--debug] load <ディレクトリ> --grep <キーワード>
    gctool [--debug] load <Excelパス>

Example:
   gctool --debug load build/check_sheet.xlsx
'''
@click.group()
@click.option('--debug/--no-debug', default=False)
@click.pass_context
def cli(ctx, debug):
    ctx.obj['DEBUG'] = debug

@cli.command()
@click.argument('excel_file', required=False)
@click.pass_context
def load(ctx, excel_file):
    print ('TEST2')
    click.echo('Debug is %s' % (ctx.obj['DEBUG'] and 'on' or 'off'))
    click.echo('Loading 2 %s' % (excel_file))
    # db = GetconfigSummarySheet(os.path.abspath(excel_file))
    # db.load()
    # # df = pd.concat([db.df_hosts, df])
    # print(list(db.df_hosts.columns))

def main():
    """Getconfig data cleansing"""
    cli(obj={})
