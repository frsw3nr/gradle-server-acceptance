"""Redmine カスタムフィールド

Redmine カスタムフィールドの定義情報を管理します

Example:

.. highlight:: python
.. code-block:: python

    from cleansing.redmine.redmine_field import RedmineField

    def func(x):
        return x * x

    field = RedmineField('キー名', True, 'CSVカラム名', func, default_value = 'hoge')
"""
import math
import logging
import sys
from enum import Enum
from datetime import datetime
import dataset as ds
from redminelib import Redmine
from redminelib.exceptions import AuthError
from redminelib.exceptions import ResourceNotFoundError
from redminelib.exceptions import ValidationError
from redminelib.exceptions import UnknownError
from getconfig_cleansing.config import Config

class RedmineField():

    def __init__(self, column, func = None, **kwargs):
        _logger = logging.getLogger(__name__)
        self.required = kwargs.get('required', None)
        self.default  = kwargs.get('default' , None)
        self.column   = column
        self.func     = func
        self.kwargs   = kwargs

    def print(self):
        print ('必須        : ', self.required)
        print ('既定値      : ', self.default)
        print ('CSVカラム名 : ', self.column)
        print ('既定値      : ', self.kwargs.get('default_value'))
        print(type(self.column))

if __name__ == '__main__':
    # ログの初期化
    logging.basicConfig(
        level=getattr(logging, 'INFO'),
        format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
        datefmt='%Y/%m/%d %H:%M:%S',
    )
    logger = logging.getLogger(__name__)

    field1 = RedmineField('カラム名')
    field1.print()

    def multi(x):
        return x * x
    field2 = RedmineField(None, multi, default = 'Hoge', required = True)
    field2.print()
    print ('関数の計算 : ', field2.func(2))

