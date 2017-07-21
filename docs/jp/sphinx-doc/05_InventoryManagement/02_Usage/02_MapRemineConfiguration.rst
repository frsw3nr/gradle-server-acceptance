Redmine マッピング設定
^^^^^^^^^^^^^^^^^^^^^^

Redmine カスタムフィールドとExcel検査項目のマッピング設定
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Getconfig は、Redmine チケットから検査対象設備の情報を抽出し、
インベントリ収集シナリオ仕様書 Excel を作成します。
その際に Redmine のどのカスタムフィールドが 検査対象Excelシートの
項目になるかのマッピング設定から値をを抽出します。

以下の設定ファイルを開いて、マッピング設定を編集してください。

::

   sakura c:\server-acceptance\config\cmdb.groovy

cmdb.redmine.custom_fields パラメータに、
「'カスタムフィールド名' : '検査対象シート項目名',」
の形式でマッピング設定を追加します。

::

   cmdb.redmine.custom_fields = [
       'ホスト名' :         'server_name',
       'IPアドレス' :       'ip',
       'プラットフォーム' : 'platform',
       'OSアカウント' :     'os_account_id',
       '固有パスワード' :   'os_specific_password',
       'vCenterアカウント': 'remote_account_id',
       'Zabbixアカウント':  'remote_account_id',    // Zabbix構成収集テンプレート用
       'VMエイリアス名' :   'remote_alias',
       '検証ID' :           'verify_id',
       '比較対象' :         'compare_server',
       'CPU割り当て' :      'NumCpu',
       'メモリ割り当て' :   'MemoryGB',
       'ESXiホスト' :       'ESXiHost',
       'ストレージ構成' :   'HDDtype',
   ]

.. note::

   既定の設定は使用する収集シナリオと関係のないカスタムフィールドも含まれます。
   既定の設定はそのまま残し、行を追加して関連するカスタムフィールドを
   追加してください。

.. note::

   Redmine カスタムフィールドに「Zabbixアカウント」を追加し、
   Zabbix 監視対象の設備トラッカーに追加したカスタムフィールドを追加(チェック)してください。

今回のデモでは、Zabbix監視設定の以下のカスタムフィールドのマッピング定義をします。

   .. figure:: image/01_redmine_map.png
      :align: center
      :alt: Redmine map

Excel検査対象項目の既定値、固定値、必須項目の設定
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

次に、プロジェクトディレクトリ下の config\\config_zabbix.groovy を編集して、
Excel検査対象項目の既定値、固定値、必須項目の設定パラメータを編集します。

::

   sakura config\config_zabbix.groovy

.. note::

   各収集テンプレートには設定ファイル内に上記パラメータの既定の設定がされています。
   特に指定がない場合は既定の設定ファイルのまま変更をせずに使用してください。

以下の行を編集してください。

::

   // Redmine フィルター設定

   redmine.default_values = [
      'remote_account_id' : 'Test',
      'verify_id' : 'RuleAP',
   ]
   redmine.fixed_items    = [
      'platform' : 'Zabbix'
   ]
   redmine.required_items = ['server_name', 'platform', 'remote_account_id']

* redmine.default_values

   Redmine フィールドが未入力の場合の規定値を設定します。

* redmine.fixed_items

   Redmine フィールドの固定値を設定します。
   Redmine フィールドの入力値があっても、本固定値に上書きされます。
   'platform'など別用途に使われているフィールドを使用させない場合に設定します。

* redmine.required_items

   検査シートの必須項目を設定します。
