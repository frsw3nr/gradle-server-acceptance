Getconfig インベントリ収集構成
------------------------------

作業PCからGetconfig構成収集ツールを実行し、
実行結果を構成管理データベースにアップロードします。
Getconfig ツールの利用は、PowerShell コンソールを開いて手動で
コマンドを実行する方法と、Jenkins ジョブスケジューラを用いて、
Web ブラウザから Getconfig ジョブを実行する方法の2つがあります。

Getconfigの詳細は、Getconfig ユーザガイドの :doc:`../../01_Setup/05_StandaloneTest/index`
を参照してください。

Getconfig構成
^^^^^^^^^^^^^

Getconfig は Groovy 言語で記述した、
Windows PC 上で動作する Java アプリケーションで、以下の構成からなります。

   .. figure:: 01_GetconfigConfig.png
      :align: center
      :alt: Getconfig 構成
      :width: 480px

* インベントリ収集シナリオ

   - Excel仕様書に、インベントリ収集を行う管理対象設備の接続情報、
     収集シナリオを定義します。
   - 収集スクリプトは Groovy 言語で記載したインベントリ収集コードで、
     管理対象で実行するコマンドやコマンド実行結果から値を抽出する
     コードを記述します。
   - 各管理対象設備の種別ごとにインベントリ収集シナリオのテンプレートがあります。

* Getconfig アプリケーション

   - インベントリ収集シナリオを読み込み、
     管理対象設備にSSH、WinRM、RESTful API等を用いてリモートアクセスし、
     インベントリ収集を実行します。
   - インベントリ収集実行結果を、Excel検査結果とJSONファイルに保存します。


Getconfig設定
^^^^^^^^^^^^^

* 作業用ディレクトリ

   Getconfig 作業用ディレクトリは以下パスとします。

   ::

      C:\Users\Administrator\GetconfigProjects

* 作業名(Getconfigプロジェクト名)

   作業ディレクトリ下に作業毎に Getconfig プロジェクトを作成します。
   プロジェクトは、作業名を指定し、以下のコマンドで実行します。
   作業名は、「日付_作業内容」とし、例えば、「201070623_監視設定」という作業名を指定します。

* Gitリポジトリ

   作業用ディレクトリ、GetconfigProjects を Gitリポジトリとし、
   構成管理サーバの GitBucket サーバを Git リモートリポジトリ保存先とします。

Getconfig作業の流れ
^^^^^^^^^^^^^^^^^^^

サーバ構築作業、監視設定作業、運用開始後のサーバ変更作業などの作業終了後に、
Getconfig にて構成情報の収集を行います。
その手順は以下の通りです。

1. 作業プロジェクト作成

   作業ディレクトリに移動し、「getconfig -g {作業名}」 コマンドで作業プロジェクトを作成します。
   これ以降の作業は作成したプロジェクトディレクトリ下に移動して行います。

2. Getconfig実行

   Getconfig 構成収集を実行します。
   詳細は、 :doc:`../../01_Setup/05_StandaloneTest/02_Usage/index` やチュートリアルを参照してください。

3. Redmineへの実行結果登録

   「getconfig -u db」コマンドで、Getconfig 実行結果の検査結果シートの値を、Redmine データベースに登録します。

4. Gitコミット

   作業終了後に、Git リポジトリをコミットし、 リモートリポジトリにプッシュします。

