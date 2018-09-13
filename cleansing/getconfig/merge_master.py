""" Python 行列計算ライブラリの Pandas データフレームで 2 つのデータフレームをマージする。
各種台帳のマージ処理で使用する。
"""

import re
import os
import logging
import datetime
import numpy as np
import pandas as pd
import dataset as ds
from getconfig.singleton import singleton

@singleton
class MergeMaster():

    def join_by_host(self, df_source, df_lookup, key_column, arg_how='outer',
                     **kwargs):
        """マスターのマージ

        ソースマスターが駆動表となり、ルックアップマスタから参照する。
        カラム名の重複は、ソースに値がない場合はルックアップ先の値をセットする。
        ルックアップ先のカラムは削除する。

        Attributes:
            df_source(DataFrame):
                マージ処理の駆動表

            df_lookup(DataFrame):
                マージ処理のルックアップ表

            key_column(String):
                ルックアップするキーカラム名('ホスト名'または、'IPアドレス')

            arg_how(String):
                ジョインの方法。'left', 'right', 'outer' を選択
        """
        if df_source.empty:
            return df_lookup
        if not isinstance(key_column, list):
            key_column = [key_column]
        if not pd.Series(key_column).isin(df_source.columns).all() or \
            not pd.Series(key_column).isin(df_lookup.columns).all():
            return df_source
        df = pd.merge(df_source, df_lookup, on=key_column, how=arg_how, 
                      suffixes=('', '__lookup'))
        for column in list(df.columns.values):
            match = re.match(r'(.+)__lookup$', column)
            if not match:
                continue
            merge_column = match.group(1)
            # マージ先カラムに記載がなく、マージ元に記載があるレコードは
            # マージ先カラムにマージ元カラムをセットし、マージ元は削除する
            cond = (df[merge_column].isnull()) & ~(df[column].isnull())
            if cond.any().any():
                df.loc[cond, merge_column] = df[cond][column]
            del df[column]
        return df

    def join_by_network(self, df_source, df_lookup, arg_how='outer',
                     **kwargs):
        """マスターのマージ(ネットワーク機器)

        ソースマスターが駆動表となり、ルックアップマスタから参照する。
        カラム名の重複は、ソースに値がない場合はルックアップ先の値をセットする。
        ルックアップ先のカラムは削除する。

        Attributes:
            df_source(DataFrame):
                マージ処理の駆動表

            df_lookup(DataFrame):
                マージ処理のルックアップ表

            arg_how(String):
                ジョインの方法。'left', 'right', 'outer' を選択
        """
        df = pd.merge(df_source, df_lookup, on=['スイッチ名', 'ポート名'], how=arg_how, 
                      suffixes=('', '__lookup'))
        for column in list(df.columns.values):
            match = re.match(r'(.+)__lookup$', column)
            if not match:
                continue
            merge_column = match.group(1)
            # マージ先カラムに記載がなく、マージ元に記載があるレコードは
            # マージ先カラムにマージ元カラムをセットし、マージ元は削除する
            cond = (df[merge_column].isnull()) & ~(df[column].isnull())
            if cond.any().any():
                df.loc[cond, merge_column] = df[cond][column]
            del df[column]
        return df
