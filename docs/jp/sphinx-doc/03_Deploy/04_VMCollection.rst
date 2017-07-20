VM ゲスト収集
-------------

検査対象が仮想化インフラの場合、vCenter サーバ もしくは、ESXi ホストに接続して、
VM 構成情報の収集を行います。

「サーバチェックシート.xlsx」を開き、シート「チェック対象」の入力列に
検査対象サーバのVM接続情報を追加します。

   * remote_account_id : "Test" を入力

      vCenter サーバもしくは、 ESXi ホストのアカウントID。config.groovy に記述

   * remote_alias : vCenter 側で管理しているVMのエイリアス名。

      vSphere Client 管理コンソールからメニュー、ホーム、インベントリを選択し、
      画面左側のツリーリストのVM名を入力

.. note::

   前節のLinux,Windows サーバの収集と同じシナリオで収集を行います。
   シートに上記値を追加した場合、既存のOS収集に、VM構成の収集が加わります。


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

_build の下に以下の収集結果が出力されます。
収集結果の確認手順は前節のLinuxの確認手順と同じです。
確認したら以下のコマンドで確認結果をローカルデータベースに登録します。

::

   getconfig -u local

.. note::

   PowerCLI が未インストールの場合、"パターン 'VMware.VimAutomation.Core' に一致する
   Windows PowerShell スナップインがありません。" のエラーメッセージが出力されます。
   PowerCLI が正しくインストールされているか確認してください。

   .. note::

      PowerCLI インストール後はOSの再起動が必要です
