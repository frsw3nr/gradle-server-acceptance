import pytest
from click.testing import CliRunner
from dataset.types import Types
from getconfig.config import Config
from getconfig.ticket.redmine_cache import RedmineCache

# py.test tests/ticket/test_cache1.py -v --capture=no -k test_sqlite1

# [仕様検討]
# * サーバはトラッカー(サーバ種別)ごとにテーブルを分ける
#     * IA/SPARC/Power/Storage
#     * IAの分類は、Linux/Windows/ESXi
# * サーバ間のリレーション(仮想サーバの管理、ESXi <-> VM(Linux/Window))

# ネットワーク、ポート、サーバが非同期に登録される

# * 機器搬入
#     * IA、SPARC、Storage 機器インベントリ読込み
#     * ポート抽出
#     * チケット登録(サーバ,ポート)
#     * ポートリストはテーブル名を「ポート」に変える
#         * キーは、「スイッチ名,IP」 とする
#     * サービスIP の管理。ノード間で活性化状態が変化する
#         * サーバ関連ポートに登録。インベントリでは採れない
#         * 台帳からリレーション登録
#     * IPアドレスのフラグ
#         * インベントリフラグ/台帳フラグを追加
#         * フラグが付いてないIPは不明。ステータスを未完了とする
#     * 機器ステータス
#         * (調達)/発注/搬入/運用/廃棄
# * ネットワーク棚卸し
#     * ポートリスト(オンラインIPアドレスインベントリ)読込み
#     * チケット登録(ネットワーク, IPアドレス)
#     * ドメインが Router の場合は、ネットワークチケットに登録
#     * IPアドレスのフラグ更新、フラグの有無でステータス(完了/未完了)も更新

def test_sqlite1():
    Config().set_cmdb_url('sqlite:///dbcache.db')
    db = RedmineCache('networks', 'hostname', Types.text)
    row = dict(text1='text1', num1 = 1)
    db.regist('host1', row)
    row2 = [("test1", 'test2'), ("num1", 5)]
    db.regist('host2', dict(row2))
    
    host1 = db.get('host1')
    host2 = db.get('host2')

    assert host1.get('text1') == 'text1'
    assert host1.get('num1')  == '1'
    assert host2.get('test1') == 'test2'
    assert host2.get('num1')  == '5'

def test_server_shipping1():
    Config().set_cmdb_url('sqlite:///dbcache.db')
    ia_server = RedmineCache('ia_servers', 'hostname', Types.text)
    # row = dict(text1='text1', num1 = 1)
    ia_server.regist('ostrich', dict(OS名 = 'CentOS6.9', CPU数 = 1, MEM容量 = 4))
    port = RedmineCache('ports', 'switch_ip', Types.text)
    port.regist('router1 - 192.168.10.1', dict(IP = '192.168.10.1', 種別 = '上位', インベントリ取得 = False, 台帳取得 = False))
    port.regist('router1 - 192.168.0.1',  dict(IP = '192.168.0.1',  種別 = '下位', インベントリ取得 = False, 台帳取得 = False))
    print(port.get('router1 - 192.168.10.1'))

    # row2 = [("test1", 'test2'), ("num1", 5)]
    # db.regist('host2', dict(row2))
    
    # host1 = db.get('host1')
    # host2 = db.get('host2')

    # assert host1.get('text1') == 'text1'
    # assert host1.get('num1')  == '1'
    # assert host2.get('test1') == 'test2'
    # assert host2.get('num1')  == '5'

