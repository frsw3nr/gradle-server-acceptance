Linux/Windows/ESXi構成情報収集
==============================

標準テンプレートのLinux/Windows/ESXiホストの構成情報収集手順を説明します。

シナリオ構成
------------

検査用 PC から以下の検査対象サーバの情報を収集し、検査をます。

1. VMWare のVMリソースの割り当てやVM設定情報
2. Linux／Windows のシステム設定情報、ネットワーク／ストレージ構成情報、パッケージ構成情報
3. ESXiホストのリソース構成情報、ネットワーク／ストレージ構成情報

1,3は、 vCenter サーバから PowerCLI 経由で情報採取します。
2は、検査対象の OS にリモート接続して情報採取します。
Linux は ssh 接続、Windows は PowerShell リモートセッションで情報を取得します。

前提条件
--------

サポートするOSバージョン
~~~~~~~~~~~~~~~~~~~~~~~~

構成情報の収集をサポートするバージョンは以下の通りです。

* vCenter/ESXi 5/6
* Linux Redhat系 6/7
* Window Server 2008 R2以上
* Solaris 10/11(シナリオ準備中)

事前準備
~~~~~~~~

* 検査用 PC の設定準備

   - :doc:`../01_Setup/01_TestPCSetup` の手順で事前に検査用PCのセットアップをしてください
   - 本シナリオはベースモジュールにバンドルされているため、テンプレートの追加は不要です

* Windows 検査対象の設定準備

   以下の設定が必要です。
   詳細は、 :ref:`windows-prepare-label` を参照してください。

   - ファイヤーウォールの許可
   - PowerShell リモートアクセス許可の有効化

* 検査対象サーバのアカウント情報の確認

   指定したアカウントでサーバにssh接続して情報収集を行います。
   事前にユーザ名、パスワードを確認してください。

   - vCenter、Windows サーバは「管理者ユーザ」アカウントが必要
   - Linux サーバは「一般ユーザ」アカウントが必要

   .. note::

      ssh接続はパスワード認証のみサポートします。
      公開鍵認証を使った認証は未サポートとなりますので、
      検査対象はパスワード認証による認証ができる設定をしてください。

使用方法
--------

以下の順に本検査シナリオの手順を記します。
用途や使用環境に合わせて各ステップを順に実行してください。

   1. プロジェクト作成
   2. Linux 検査対象の検査
   3. Windows 検査対象の検査
   4. VM 構成情報の検査
   5. ESXi 検査対象の検査
   6. 検査ルールの設定
   7. 比較対象ルールの設定

プロジェクト作成
----------------

はじめに検査用プロジェクトを作成します。
「getconfig -g <プロジェクトホーム>」で指定したプロジェクトディレクトリにプロジェクトを作成します。

::

   cd C:\Users\Public
   getconfig -g test-project1

実行後、指定したディレクトリ下に検査用ファイル、ディレクトリが作成されます。
主なファイル構成は以下の通りです。

* サーバチェックシート.xlsx

   本シートに検査対象の接続情報、検査ルールを設定します。
   getconfig コマンドは設定した内容を基に、検査シートに記載した検査項目で検査を行います。

* config/config.groovy

   検査構成情報を設定します。
   Windows, Linux, vCenter の各プラットフォームの接続情報を記述します。

* lib/InfraTestSpec ディレクトリ

   検査コードスクリプトの保存ディレクトリ。
   Groovy 言語で記述した検査コードで、各プラットフォーム毎にスクリプトが分かれます。
   各スクリプトは、「サーバチェックシート.xlsx」内の検査IDと同じ名前の関数名から構成し、
   本ソースコードを編集して検査シナリオのカスタマイズをします。

* lib/script ディレクトリ

   Jenkins ジョブスクリプトなどの保存ディレクトリ。
   管理サーバでリモートで検査を実行する場合に使用します。

* lib/template ディレクトリ

   PowerShell スクリプトのテンプレートスクリプトの保存ディレクトリ。
   Windows のPowerShellリモート実行、vCenter の PowerCLI 実行用スクリプトのひな形となります。

* build ディレクトリ

   検査結果の保存ディレクトリ。

* node ディレクトリ

   ローカル構成管理データベースの保存ディレクトリ。
   検査結果を JSON ファイルで保存します。

Linux サーバの検査
------------------

「サーバチェックシート.xlsx」を編集します。
シート「チェック対象」の入力列に 検査対象の Linux サーバの情報を設定します。

   * server_name : 検査対象サーバ名

      検査対象の Linux サーバのホスト名を入力

   * ip : 検査対象IP

      Linux サーバの IP アドレスを入力

   * platform : "Linux"

      "Linux" を入力

   * os_account_id : "Test"

      "Test" のまま

設定ファイル「config\\config.groovy」 を編集します。
以下の行に Linux サーバのアカウント情報パラメータを入力します。

::

   // Linux 接続情報

   account.Linux.Test.user      = 'someuser'
   account.Linux.Test.password  = 'XXXXXXXX'

.. note::

   パラメータ名は、「account.{platform}.{os_account_id}.{名前}」となります。
   複数の検査対象サーバでアカウント情報が異なる場合、os_account_id の箇所を変更し、
   アカウント情報を定義してください。

PowerShell コンソールを開いて、プロジェクトディレクトリに移動し、getconfig コマンドを実行します。

::

   cd C:\Users\Public\test-project1
   getconfig

出力メッセージにエラーがなく、"Finish server acceptance test ..."
が出力されれば成功です。_build の下に以下の検査結果が出力されます。

   * 「_build/チェックシート_{日付}.xlsx」 が検査結果となります
   * 「_build/log/Linux」 ディレクトリ下に、各検査の詳細ログファイルを保存します

「_build/チェックシート_{日付}.xlsx」を開きます。
シート「ゲストOSチェックシート(Linux)」が、検査結果のサマリとなります。
シート「検査ルール」より右側のシートは、詳細の検査結果で、
検査シートの検査項目でデバイスが "Y" となる検査項目の検査結果となります。

検査結果を確認したら、「getconfig -u local」で、
検査結果をローカルデータベースに登録します。
本データベースは、後述の検査対象の比較をする際に使用します。

::

   getconfig -u local

.. note::

   ssh接続に失敗した場合、"com.jcraft.jsch.JSchException: java.net.ConnectException: Connection timed out: connect" のメッセージが出力されます。
   「チェック対象」シートの ip アドレスが正しいか確認してください。

.. note::

   ssh 接続で認証エラーが生じた場合、
   "com.jcraft.jsch.JSchException: Auth fail" のメッセージが出力されます。
   config.groovy ファイルの Linux アカウントのユーザ、パスワードが正しいか確認してください。

Windows サーバの検査
--------------------

「サーバチェックシート.xlsx」を開き、シート「チェック対象」の入力列に
検査対象の Windows サーバの情報を設定します。

   * server_name : 検査対象のサーバ名

      検査対象の Windows サーバのホスト名を入力

   * ip : 検査対象のIP

      Windows サーバの IP アドレス確認

   * platform : "Windows"

      "Windows" を入力

   * os_account_id : "Test"

      "Test" のまま

設定ファイル「config\\config.groovy」 を編集します。
以下の行に Windows サーバのアカウント情報パラメータを入力します。

::

   // Windows 接続情報

   account.Windows.Test.user      = 'administrator'
   account.Windows.Test.password  = 'XXXXXXXX'

PowerShell コンソールを開いて、プロジェクトディレクトリに移動し、getconfig コマンドを実行します。

::

   cd C:\Users\Public\test-project1
   getconfig

_build の下に以下の検査結果が出力されます。
検査結果の確認手順は前節のLinuxの確認手順と同じです。
確認したら以下のコマンドで確認結果をローカルデータベースに登録します。

::

   getconfig -u local

.. note::

   PowerShell のリモート接続に失敗した場合、"アクセスが拒否されました。詳細については、
   about_Remote_Troubleshooting のヘルプ" のメッセージが出力されます。
   「チェック対象」シートの ip アドレスが正しいか、
   config.groovy ファイルの Windows アカウントのユーザ、パスワードが正しいか確認してください。

VM 構成の検査
-------------

検査対象が仮想化インフラの場合、vCenter サーバ もしくは、ESXi ホストに接続して、
VM 構成情報の検査を行います。

「サーバチェックシート.xlsx」を開き、シート「チェック対象」の入力列に
検査対象サーバのVM接続情報を追加します。

   * remote_account_id : "Test" を入力

      vCenter サーバもしくは、 ESXi ホストのアカウントID。config.groovy に記述

   * remote_alias : vCenter 側で管理しているVMのエイリアス名。

      vSphere Client 管理コンソールからメニュー、ホーム、インベントリを選択し、
      画面左側のツリーリストのVM名を入力

.. note::

   前節のLinux,Windows サーバの検査と同じシナリオで検査を行います。
   シートに上記値を追加した場合、既存のOS検査に、VM構成の検査が加わります。


設定ファイル「config\\config.groovy」 を編集します。
以下の行に vCenter サーバ もしくは ESXi ホストのアカウント情報パラメータを入力します。

::

   // vCenter接続情報

   account.Remote.Test.server   = '192.168.10.100'
   account.Remote.Test.user     = 'vCenter 管理者ユーザ'
   account.Remote.Test.password = 'XXXXXXXX'

PowerShell コンソールを開いて、プロジェクトディレクトリに移動し、getconfig コマンドを実行します。

::

   cd C:\Users\Public\test-project1
   getconfig

_build の下に以下の検査結果が出力されます。
検査結果の確認手順は前節のLinuxの確認手順と同じです。
確認したら以下のコマンドで確認結果をローカルデータベースに登録します。

::

   getconfig -u local

.. note::

   PowerCLI が未インストールの場合、"パターン 'VMware.VimAutomation.Core' に一致する
   Windows PowerShell スナップインがありません。" のエラーメッセージが出力されます。
   PowerCLI が正しくインストールされているか確認してください。

   .. note::

      PowerCLI インストール後はOSの再起動が必要です

ESXi ホストの検査
-----------------

vCenter サーバ もしくは、ESXi ホストに接続して、ESXi ホストの構成情報の検査を行います。

「サーバチェックシート.xlsx」を開き、シート「チェック対象」の入力列に
検査対象サーバのVM接続情報を追加します。

   * server_name : ESXi ホスト名

      "ホスト名" を入力。必須

   * ip : ESXi ホスト IPアドレス

      "IPアドレス" を入力。必須

   * platform : "VMHost"

      "VMHost" を入力

   * os_account_id : "Test"

      "Test" を入力
      ESXi ホストのアカウントID。config.groovy に記述


設定ファイル「config\\config.groovy」 を編集します。
以下の行に ESXi ホストのアカウント情報パラメータを入力します。

::

   // VMHost 接続情報

   account.VMHost.Test.user      = 'test_user'
   account.VMHost.Test.password  = 'P@ssword'

PowerShell コンソールを開いて、プロジェクトディレクトリに移動し、getconfig コマンドを実行します。

::

   cd C:\Users\Public\test-project1
   getconfig

_build の下に以下の検査結果が出力されます。
検査結果の確認手順は前節のLinuxの確認手順と同じです。
確認したら以下のコマンドで確認結果をローカルデータベースに登録します。

::

   getconfig -u local

検査ルールの設定
----------------

設定した検査ルールに従って採取した値のチェックをします。以下のシナリオを想定しています。

   1. VMのCPU／メモリ／ディスクリソースの割り当てが正しいか
   2. サーバのネットワーク／ストレージ構成が正しいか
   3. システム設定値が正しいか
   4. 指定したパッケージがインストールされているか、バージョンは正しいか
   5. 指定したサービスが起動されているか

現バージョンは 1 のVMリソースの割り当てチェックのみをサポートします。
実行手順は以下の通りです。

「サーバチェックシート.xlsx」を開き、シート「チェック対象」の入力列に
検査対象サーバのVM接続情報を追加します。

   * verify_id : RuleAP または、 RuleDB

     シート「検査ルール」に記述したルールIDを入力。「RuleAP」または、「RuleDB」を入力します

   * NumCpu : 1～N

      CPU割り当て数を入力します

   * MemoryGB : 1～N

      メモリ割り当て量[GB]を入力します

   * ESXiHost : ESXiホスト名

      リソース割り当てをするESXiホスト名を入力します。中間一致で名前を検索します。

   * HDDType

      ストレージ構成を入力します。[{ストレージタイプ}:{容量GB}] の形式で記述します。
      ストレージタイプは 「Thin」(Thin provisioning)、「Thick」(Thick provisioning)を入力します。
      複数のストレージ構成の場合、[Thin:30, Thin:40] のように、配列形式で記述します。

上記値を入力後、 getconfig コマンドを実行すると、チェックシートの検査結果に検査ルール結果を追加します。
検査ルールがOKのときはセルが「グリーン」の配色となり、検査ルールがNGのときはセルが「ピンク」の配色になります。


検査結果の比較ルールの設定
--------------------------

複数の検査対象の検査結果を比較して値が同じかどうかをチェックします。

   1. 過去の検査結果との比較

      過去の検査結果と実績を比較します。ローカルデータベースに蓄積した検査結果とgetconfig 実行結果の比較をします

   2. 複数の類似設定の検査対象サーバの比較

      類似の設定を複数のサーバで行った場合に、1台を代表サーバとして各サーバの実行結果との比較をします

実行手順は以下の通りです。

「サーバチェックシート.xlsx」を開き、比較対象の検査対象サーバの代表のサーバ名を追加します。

   * compare_server

      * シート「検査対象」または、シート「検査ルール」に結果を比較する元のサーバ名を記入します
      * シート「検査対象」を未記入にすると verify_id で指定した検査ルールの比較対象サーバ設定が既定値となります

      .. note::

         シート「検査対象」の検査ルール verify_id は必須入力となります。

   * compare_source

      シート「検査ルール」に記入します。
      結果を比較するソースで、以下を入力します。

      * 「actual」 : 実行結果から
      * 「local」 : ローカルデータベースから。事前に「getconfig -u local」で、検査結果をローカルデータベースに登録が必要です
      * 「db」 : リモートデータベースから。別途、後述する管理サーバが必要です

上記値を入力後、 getconfig コマンドを実行すると、チェックシートの検査結果に比較結果を追加します。

    * 結果列の一番左の列に比較対象のベースとなるサーバの検査結果の実績値を表示します。
    * 次の列から、比較結果の列で値が一致しているセルは「水色」の配色になります。
    * 検査ルールが設定されている行は、上記の検査ルールにしたがって「グリーン」、「ピンク」の配色になります。

