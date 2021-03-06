Excel管理台帳からのチケットインポート
-------------------------------------

既存のExcel管理台帳からチケットへまとめてインポートする手順を記します。

1. 既存のExcel ファイルを CSVファイルに変換
2. Redmine のプロジェクトメニュー、チケット、サマリーインポートを選択
3. ファイル名に変換したCSV を指定
4. チケットとCSVのフィールドの対応関係を入力
5. インポート実行

CSVファイルの作成
^^^^^^^^^^^^^^^^^

インポートしたいチケットの情報を記述したCSVファイルを作成ます。

   .. figure:: image/06_Import1.png
      :align: center
      :alt: Ticket Import 1
      :width: 640px

.. note::

   * 開始日や期日など日付は年・月・日をハイフンでつなげた形式(例: 2015-12-31)
     で入力してください。これ以外の形式だとインポート時にエラーが発生します。
     ※ Excelの「セルの書式設定」→「ユーザー定義」で「yyyy-mm-dd」と設定
   * 一つのCSVファイルにプロジェクトやトラッカーが異なるチケットを混在させる
     ことはできません。
   * 複数選択可能なリスト項目はインポートしません。別途、手動で項目を設定してください。

インポート開始
^^^^^^^^^^^^^^

「チケット」画面を開き、右サイドバー内のメニュー「インポート」をクリックしてください。

   .. figure:: image/06_Import2.png
      :align: center
      :alt: Ticket Import 1

インポートファイルの選択
^^^^^^^^^^^^^^^^^^^^^^^^

「ファイルを選択」ボタンをクリックして作成したCSVファイルを選択し、「次」ボタンをクリックしてください。

   .. figure:: image/06_Import3.png
      :align: center
      :alt: Ticket Import 1

作成したCSVファイルの形式にあわせてオプションを指定して「次」ボタンをクリックしてください。

フィールドの対応関係の指定
^^^^^^^^^^^^^^^^^^^^^^^^^^

CSVファイル内の各列がチケットのどのフィールドに対応するか対応関係を設定して「インポート」をクリックしてください。

   .. figure:: image/06_Import4.png
      :align: center
      :alt: Ticket Import 1

完了すると実行結果が表示されます。
チケットが想定通りに作成されていることを確認してください。
