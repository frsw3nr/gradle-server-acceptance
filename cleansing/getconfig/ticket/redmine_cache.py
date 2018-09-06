"""Redmine チケットのキャッシュ用 SQLite3 データベースの管理

Redmine チケット登録処理用のキャッシュとして、 SQLite3 に登録結果を保存します。

* Redmine チケット登録後に SQLite3 キャッシュに結果を登録します
* 次回の Redmine チケット登録時にキャッシュを読み込み、 Redmine 更新の必要有無をチェックします
* SQLite3 キャッシュと比較して登録する属性値に変更がない場合は Redmine 更新をスキップします

Note:

    Redmine チケットトラッカー毎に本インスタンスを生成します。
"""

import math
import logging
import sys
from datetime import datetime
import dataset as ds
from dataset.types import Types
from getconfig.config import Config

class RedmineCache():

    database    = 'dbcache.db'
    """キャッシュ用 SQLite3 データベース名"""
    table_name  = None
    """Redmine トラッカー別のキャッシュ管理テーブル名"""
    primary_id  = None
    """キャッシュ管理テーブルの主キー名"""
    # primary_type = Types.text
    primary_type = Types.string(256)
    """キャッシュ管理テーブルの主キーの型(既定はテキスト)"""

    def __init__(self, table_name, primary_id, primary_type):
        _logger = logging.getLogger(__name__)
        cmdb = ''
        try:
            # cmdb = Config().get('Redmine','CMDB')
            cmdb = Config().get_cmdb_url()
        except KeyError as e:
            _logger.error(e)
            sys.exit("Could not read configfile")

        # db = ds.connect('sqlite:///{}'.format(self.database))
        db = ds.connect(cmdb)
        self._table = db.create_table(table_name, primary_id=primary_id, primary_type=primary_type)
        self.table_name = table_name
        self.primary_id = primary_id
        self.primary_type = primary_type

    def regist(self, key, row):
        """キャッシュ管理テーブルの更新"""
        row[self.primary_id] = key
        cond = dict([(self.primary_id, key)])
        self._table.delete(**cond)
        store_data = dict()
        for key, value in row.items():
            if value is not None:
                store_data[key] = str(value)
        # row = dict([(k, str(v)) for k,v in row.items()])
        self._table.insert(store_data, ensure=True)

    def get(self, key):
        """キャッシュ管理テーブルの検索"""
        cond = dict([(self.primary_id, key)])
        return self._table.find_one(**cond)

    def set_relation(self, from_key, to_key):
        """Redmine チケットリレーションの登録"""
        row = dict(from_to_ticket_id = '{}-{}'.format(from_key, to_key),
                from_key = int(from_key), to_key = int(to_key))
        self._table.upsert(row, ['from_to_ticket_id'])

    def exist_relation(self, from_key, to_key):
        """Redmine チケットリレーションの検索"""
        key = '{}-{}'.format(from_key, to_key)
        return self._table.find_one(from_to_ticket_id = key)

if __name__ == '__main__':
    Config().set_cmdb_url('sqlite:///dbcache.db')
    db = RedmineCache('networks', 'hostname', Types.text)
    row = dict(text1='text1', num1 = 1)
    db.regist('host1', row)
    row2 = [("test1", 'test2'), ("num1", 5)]
    db.regist('host2', dict(row2))
    print(db.get('host1'))
    print(db.get('host2'))
    # row = db.get_network('NY2-M4AX-J02')
    # print(row['id'])
