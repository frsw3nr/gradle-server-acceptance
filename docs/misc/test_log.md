v2.7 ログ管理クラス見直し
=========================

機能分類
--------

### AsIs

* CMDBModel
    * 初期化 : initialize, set_environment
    * 検索 : getDeviceResultByHost, getMetricByHost
    * 登録 : export, registMaster : registMetric, registDevice
* TestPlatform
    * アダプター : accept, set_environment
    * セッター: project_test_log_dir, evidence_log_share_dir, current_test_log_dir
* InventoryDB
    * 初期化 : set_environment
    * 履歴表示 : export
            * print_node_list
    * コピー（ベース⇒プロジェクト）
        * copy_base_node_dir
        * copy_base_test_log_dir
* EvidenceManager
    * 初期化 : set_environment
    * コピー（カレント⇒ベース、プロジェクト）
        * update_evidence_log
        * archive_json
    * DB登録 : export_cmdb_all : export_cmdb
    * メイン : update
* TestLog
    * セッター／ゲッター
         * get_log_path_v1, get_log_path, 
         * get_source_log_path, get_local_dir, get_target_log_path

### 機能分解

* ログディレクトパスリの管理
    * ベース、プロジェクト、カレント、テストログ、ノード定義
* アダプター
    * 検査スクリプトと連携
* ファイルコピー
    * ベース⇒プロジェクト、プロジェクト⇒カレント
    * プロジェクト⇒カレント、カレント⇒プロジェクト
    * ターゲット、プラットフォームでフィルタリング
* データベース連携
    * ロード機能、ベース、プロジェクト
* 履歴検索
    * ターゲット、プラットフォームのリスト出力、ベース、プロジェクト
* コマンド
    * コミット、DBエクスポート、履歴表示

クラス分類
----------

### TestLogクラス

* 機能
    * ログディレクトパスリの管理
        * ベース、プロジェクト、カレント
        * テストログ、ノード定義
    * アダプター
        * 検査スクリプトと連携
    * ステージ間コピー
    * 初期化
        * ログディレクトリの新規作成
* メンバー
    * static
        * log_dirs<String, String>
        * node_dirs<String,String>
* Util
    * copy(source_stage, dest_stage, target, platform)
        * ファイルコピー
            * ターゲット、プラットフォームでフィルタリング
        * ベース⇒プロジェクト
            * プロジェクト⇒カレント
        * プロジェクト⇒カレント
            * カレント⇒プロジェクト
    * list(filter_target, filter_platform)
        * 履歴検索
        * ターゲット、プラットフォームのリスト出力
            * ベース、プロジェクト
    * printList(filter_target, filter_platform)
    * getTargetPath(testSpec,metric, shared)
    * getLogPath(testSpec,metric, shared)
* Enum
    * TestLogStage
        * Base, Project, Current

### EvidenceManager -> 階層1つ上に

* コマンド
    * コミット
    * DBエクスポート
    * 履歴表示
* run(argv)

### CMDBModel->DBExporter

* データベース連携
    * ロード機能
    * ベース、プロジェクト
* export(stage)
