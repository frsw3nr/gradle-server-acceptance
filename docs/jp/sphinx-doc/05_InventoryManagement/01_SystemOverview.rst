システム概要
------------

システム構成
^^^^^^^^^^^^

システム構成は以下の通りです。

   .. figure:: image/05_Overview.png
      :align: center
      :alt: Inventory Management Overview
      :width: 720px

* Redmine

   作業PCから、Redmine チケットのインベントリ収集対象設備メタ情報を抽出して、
   収集シナリオを作成します。
   また、インベントリ収集実行後の結果を　Redmine データベースにアップロードします。

* プロジェクト(Git)

   Getconfig プロジェクトを Git リポジトリとして登録し、インベントリ収集シナリオ、
   インベントリ収集結果の変更管理をします。

* GitBucket

   GitBucket で Getconfig プロジェクトを一元管理します。
   プロジェクトの変更内容は、Webブラウザから GitBucket にアクセスしてブラウズします。
   インベントリ収集結果の変更履歴は、インベントリ収集結果の JSON 検査結果から
   確認します。

* Jenkins

   GitBucket から、Getconfig プロジェクトを読込み、収集シナリオを実行し、収集結果の
   アップロードまでの一連のジョブを Jenkins から実行します。
   Jenkins Web コンソールからの手動実行と、スケジューラによる自動実行の2種類があります。

   .. note::

      自動実行の実行頻度、ログ生成量によってディスク容量圧迫となる恐れがあります。
      極端な実行頻度での設定は控え、週に1回程度の設定にしてください。

* 作業PC

   手元のPCから、Cetconfig プロジェクトを複製し、プロジェクト 編集後、 Git
   リポジトリにアップロードすることで、編集したプロジェクトを Jenkins で実行
   することが可能です。

利用手順シナリオ
^^^^^^^^^^^^^^^^

Zabbix の監視設定の収集シナリオをモチーフとして利用手順を記します。
Linux の構成管理データベース、Windows の作業PC の各1台が Zabbix 監視対象として
設定されている環境を基に、Redmine や Jenkins との連携手順を記します。
Zabbix 監視設定は以下2つのサーバで Linux, Windows 標準テンプレートが適用されている
環境を想定します。

   .. figure:: image/05_ZabbixDemo.png
      :align: center
      :alt: Inventory Management Overview
      :width: 720px
