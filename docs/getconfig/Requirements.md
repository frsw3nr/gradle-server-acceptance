Getconfig 2 プロジェクト
========================

要件検討
--------

1. 検査仕様の読み込み

    * テンプレートごとのExcelを共通化して一つのExcelから読み込む
    * 各プラットフォームのシナリオは JSON または、スクリプトにする

2. 採取方式

    * 採取と後処理に分離する。採取は Go エージェントが実行する。後処理は従来のGroovy
    * 採取はスクリプト実行形式にして、エージェントのGo言語方式を用意する
    * コマンド実行(SSH,Telnet,REST,エージェント)

3. エージェント採取シナリオ

    * ローカル採取用のエージェント機能を追加する
    * 接続情報（構成）にローカルのエージェントからリモート接続するパターンを追加する
    * 実行結果は各メトリック毎にファイル保存する
    * エージェントのデーモン起動
        * 定期的にスクリプトを実行し、実行結果をzip圧縮する

4. エージェントプラットフォームシナリオ
    
    * windows、golang バージョン
    * VMWare govmoni
    * ssh , rest パターン

5. アグリケータ

    * Groovy 側集計機能、集計、抽象化、フォーマット変換
    * 比較機能
    * エージェントの配布機能
    * コマンドラインツールにする。定期起動は別スクリプトで準備
        * エージェント配布
        * インベントリ採取結果集計
        * ドキュメント作成
        * DB登録

6. 保守性向上

    * groovy3対応
    * gradle 4→5.6
    * compile statistics 化
    * 簡素化

構成
----

1. エージェント

    * 設定ファイル管理
    * 採取、各プラットフォームシナリオ、SSH、Telnet
    * デーモンプロセス化

2. アグリゲータ(仮名称)

    * プロジェクト初期化 
        * ProjectBuilder

    * テスト実行 

        * TestRunner / TestScheduler / PlatformTester
        * TestItem / TestScriptGenerator

    * 設定ファイル管理

        * Config / ConfigTestEnvironment
        * LogFile / NodeFile / TestLog / TestLogBase

3. インベントリ採取コマンド

    * InfraTestSpec
        * Platform
        * Base
            * Unix / Linux / Windows / VCenter / REST
        * Command
            * SSH / Telnet / REST / JSON RPC / Agent

4. モデル Model

    * 仕様定義
        * Scenario / Target / Platform / Result / ResultLine
        * Template / Metric
    * インベントリ
        * Redmine / Ticket / TicketRegister / PortList

5. ドキュメント化 Document

    * Excel 仕様パーサー
        *ExcelParser / SheetDesign
    * Excle生成
        * EvidenceMaker / ReportMaker / ExcleSheetMaker / ExcelSheetParser
    * Redmine登録
        * RedmineTicket / RedmineTicketField
    * データの永続化 JSON
        * TestResultReader / TestResultWriter
    * 検査レポート
        * DataComparator
    * 比較
        * TagGenerator / TagGeneratormanual
