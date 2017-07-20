Windowサーバ収集
----------------

管理対象 Windows サーバの事前準備
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

管理対象の Windows サーバに作業 PC から PowerShell コマンドをリモート実行出来る様、
接続許可設定をします。
管理対象サーバに Administrator ユーザでログインし、
管理者でPowerShell を開いて、以下コマンドを実行します。

パブリックネットワークの場合の構成変更
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

ネットワーク構成がパブリックネットワークの場合、ネットワーク構成を
プライベートかドメインに変更します。以下のコマンドで確認します。

::

   Get-NetConnectionProfile -IPv4Connectivity Internet

結果が 'Public' の場合、以下の手順でプライベートへ変更します。

::

   Set-NetConnectionProfile -InterfaceAlias (Get-NetConnectionProfile -IPv4Connectivity Internet).InterfaceAlias -NetworkCategory Private

WinRM リモート管理設定
~~~~~~~~~~~~~~~~~~~~~~

以下の WinRM 設定コマンドを実行します。

::

   winrm quickconfig

本設定は以下のPowerShell リモート接続をするための設定を行います。

* WinRM用のservice起動設定
* WinRM用のLisner作成
* WinRM用のファイヤーウォールの設定

PowerShell リモートアクセス許可の有効化
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Windows Server 2012 より前のOSでは、PowerShell のリモートアクセス許可が無効化されている場合があります。
その場合、PowerShellを管理者権限で実行して、PowerShell コンソールから以下のコマンドで有効化します。

::

   Enable-PSRemoting

.. note::

   Windows Server 2012 R2 以上の環境では、リモートアクセス許可の既定値は有効です。

Windows検査シート入力
^^^^^^^^^^^^^^^^^^^^^

PowerShell を開いて、プロジェクトディレクトリに移動し、
「サーバチェックシート.xlsx」を編集します

   .. figure:: image/02_registTargetWindows.png
      :align: center
      :alt: New Project
      :width: 640px

* シート「チェック対象」の入力列に 検査対象の Windows サーバの情報を設定します。
* はじめに「platform」に"Windows"を選択してください。
* 「virtualization」に仮想化OSの場合は、"VM"、オンプレサーバの場合は"オンプレ"を選択してください。
* 各項目の入力手順はLinux検査と同じとなります。

config\\config.groovyの編集
^^^^^^^^^^^^^^^^^^^^^^^^^^^

サクラエディタなど UTF-8 に対応したエディタで、 config\\config.groovy を開き、以下の行の接続アカウント情報を編集します。
アカウントID,Windowsログオンテストの入力手順は、Linuxと同様です。

::

   // vCenter接続情報

   account.Remote.Test.server   = '192.168.10.100'
   account.Remote.Test.user     = 'test_user'
   account.Remote.Test.password = 'P@ssword'

   // Windows 接続情報

   account.Windows.Test.user     = 'administrator'
   account.Windows.Test.password = 'P@ssword'
   // account.Windows.Test.logon_test = [['user':'test1' , 'password':'test1'],
   //                                    ['user':'test2' , 'password':'test2']]


Windows検査実行
^^^^^^^^^^^^^^^

PowerShellコンソールから、プロジェクトディレクトリに移動して、getconfig を実行してください。

::

   getconfig

実行後、プロジェクトディレクトリ下の「build」の下に生成された
Excel検査結果を開いて結果を確認します。
Excel検査結果の確認ができたら"getconfig -u local"
でローカルデータベースに検査結果を登録します。

::

   getconfig -u local

次に、"getconfig -u db"を実行し、
構成管理データベースに検査結果を登録します。
本コマンドは構成管理データベースのRedmineデータベースに検査結果を
登録します。

::

   getconfig -u db

