import com.taskadapter.redmineapi.*
import com.taskadapter.redmineapi.bean.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.Ticket.*

/*
Redmine リポジトリ管理

Redmine に接続し、Redmine リポジトリ情報を検索します。

* インスタンス生成時にRedmine に接続し、Redmine リポジトリ情報を検索します。
* 検索結果をハッシュに格納します。
* 検索した結果は、get_XXX() メソッドで検索します。

Example:

.. highlight:: python
.. code-block:: python

    from cleansing.redmine.redmine_repository import RedmineRepository

    print (db.get_tracker_fields_by_id(1, 27))
    print (db.project_ids)
*/

