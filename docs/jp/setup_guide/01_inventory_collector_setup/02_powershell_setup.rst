PowerShell設定
==============

PowerShellのインストール
------------------------

OSが以下のバージョンの場合、PowerShellの追加インストールが必要となります

   * Windows 7
   * Windows Server 2008 R2
   * Windows Server 2012

以下サイトからインストールしてください

::

   # Microsoft .NET Framework 4.5のインストール
   http://www.microsoft.com/en-us/download/details.aspx?id=30653
   # Windows Management Framework 5.0 (WFM 5.0) のインストール
   https://www.microsoft.com/en-us/download/details.aspx?id=50395
 
PowerShell のスクリプト実行権限の設定
-------------------------------------

PowerShell スクリプトの実行許可設定をします。
管理者ユーザでPowerShellを起動し、以下コマンドを実行して、現在の設定を確認します。

::

   Get-ExecutionPolicy

上記確認結果が、Restricted、AllSignedの場合は、以下コマンドで RemoteSigned に
設定変更してください。

::

   Set-ExecutionPolicy RemoteSigned

.. note::

   RemoteSignedは、 署名付きのスクリプト、及びローカルに保存されているスクリプトを実行可能にします。

   詳細は、 `PowerShellスクリプトの実行ポリシー`_ を参照してください。

   .. _PowerShellスクリプトの実行ポリシー: http://www.atmarkit.co.jp/ait/articles/0805/16/news139.html

.. note::

   Windows Server 2012 R2 以上の場合の既定値は RemoteSigned です。

PowerShell のリモートアクセス設定
---------------------------------

PowerShell でリモートアクセスをできるようにします。
管理者ユーザで PowerShell を起動し、以下コマンドを実行して、「信頼されたホストの一覧」
に追加します。

::

   Set-Item wsman:\localhost\Client\TrustedHosts -Value * -Force

WinRM リモート管理設定
~~~~~~~~~~~~~~~~~~~~~~

.. note::

   自ホストのインベントリ収集をする場合は以下の WinRM 設定コマンドを実行します。

::

   winrm quickconfig

変更しますかに対し、yを入力してください。
本設定は以下のPowerShell リモート接続をするための設定を行います。

* WinRM用のservice起動設定
* WinRM用のLisner作成
* WinRM用のファイヤーウォールの設定
