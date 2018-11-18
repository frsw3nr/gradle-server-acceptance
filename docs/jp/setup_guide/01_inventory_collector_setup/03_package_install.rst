パッケージインストール
======================


Windows 更新プログラムの適用
----------------------------

パッケージをインストールする前に Windows 更新プログラムを適用してください。

1. Windows Update による更新プログラムの適用

* スタートメニューを選択し、検索バーに「Windows Update」を入力し、Windows Updateを起動してください。
* 更新プログラムの確認をし、更新プログラムを適用してください。

Chocolatey によるパッケージ インストール
----------------------------------------

以下のソフトウェアをインストールします。

* Java関連
    * JDK1.8 (64bit)
    * Gradle(ビルドツール)
* Git 関連
    * git.install(Git)
    * TortoiseGit(Git GUIクライアント)
    * WinSCP(SCPクライアント)
* UTF-8対応したユーティリティ
    * notepad++(テキストエディタ)
    * 7-zip(zipアーカイバ)
* Unix 関連
    * UnxUtils(Unix コマンドユーティリティ)
* VMware 関連
    * VMware vSphere Client
* その他
    * Google Chrome(Webブラウザ確認用)

本手順書では、Windows 版パッケージ管理ツール `Chocolatey`_ を用いて、各種ソフトウェアをインストールします。


.. _Chocolatey: https://chocolatey.org/


管理者ユーザで PowerShell を起動して、以下のコマンドで Chocolatey をインストールします。

::

   iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))

続けて、以下 Chocolatey コマンドで各種ソフトウェアをインストールします。

::

   choco install -y unxutils winscp 7zip notepadplusplus.install jdk8 gradle TortoiseGit git.install GoogleChrome vmwarevsphereclient

.. note::

   **32ビット版Java導入済み環境の注意点**
   
   Java環境は 64ビット版が必要となりますが、32ビット版Java がインストール済み環境の場合、
   chocolatey の64ビット版 Java のインストールがスキップする問題があります。
   その場合は以下コマンドで、64ビット版 Java を指定してインストールしてください。

   ::

      choco install jdk8 -params "x64=true"

   java -version を実行し、出力メッセージに64-bitの記述があることを確認します。
   32ビット版のみの場合、この後のgetconfigアプリ実行時に、
   PowerShellスクリプトで「UnauthorizedAccess」というエラーが発生します。

.. note::

   **Office 製品がない場合**

   Office 製品がない場合は、以下コマンドで、Libre Office をインストールします。

   ::

      choco install -y libreoffice-oldstable

