import com.taskadapter.redmineapi.*
import com.taskadapter.redmineapi.bean.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.Ticket.*

/*
Redmine カスタムフィールド

Redmine カスタムフィールドの定義情報を管理します

Example:

.. highlight:: python
.. code-block:: python

    from cleansing.redmine.redmine_field import RedmineField

    def func(x):
        return x * x

    field = RedmineField('キー名', True, 'CSVカラム名', func, default_value = 'hoge')
*/

