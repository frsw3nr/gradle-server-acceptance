ESXi ホスト収集
---------------

vCenter サーバ もしくは、ESXi ホストに接続して、ESXi ホストの構成情報の収集を行います。

「サーバチェックシート.xlsx」を開き、シート「チェック対象」の入力列に
収集対象サーバのVM接続情報を追加します。

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

_build の下に以下の収集結果が出力されます。
収集結果の確認手順は前節のLinuxの確認手順と同じです。
確認したら以下のコマンドで確認結果をローカルデータベースに登録します。

::

   getconfig -u local
