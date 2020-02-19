Getconfig 2 プロトタイピング
========================

1. 共通

    * モデル

        * Model / Scenario / Template / Metric
        * Target / Platform / Result / ResultLine
        * Node / Platform / Result / ResultLine
            * RedmineTicket / TicketRegister / PortList

    * groovy3対応

        * gradle 4→5.6
        * compile statistics 化
        * 簡素化

2. エージェント機能

    * スクリプト実行
        * エージェントのGo言語方式を用意する
            * コマンド実行(SSH,Telnet,REST,エージェント)
    * 採取シナリオ
        * windows
            * golang バージョン
        * zabbix
        * vmware
    * 接続情報（構成）にローカルのエージェントからリモート接続するパターンを追加する
    * エージェントのデーモン起動
        * 定期的にスクリプトを実行し、実行結果をzip圧縮する

3. 検査仕様の読み込み

    * テンプレートごとのExcelを共通化して一つのExcelから読み込む
    * 各プラットフォームのシナリオは JSON または、スクリプトにする
    * モデルを作成してサンプル作成

4. アグリケータ

    * エージェントの配布機能
        * 定期実行
            * デーモンプロセス化
    * Groovy 側集計機能
        * エージェント通信機能
            * zipファイルのダウンロード
        * 集計、抽象化、フォーマット変換

