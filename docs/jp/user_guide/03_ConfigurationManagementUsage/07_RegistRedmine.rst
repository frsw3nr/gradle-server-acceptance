インベントリ収集結果の Redmine 登録について
===========================================

インベントリの収集結果を、Redmine に登録します。
ワークフローサーバにリモートデスクトップ接続をして、PowerShell コンソールから
Getconfig コマンドを実行します。
インベントリ収集を実行した作業ディレクトリに移動し、以下のコマンドを実行してください。

.. note::

   工場出荷前に採取した場合、採取したインベントリプロジェクトディレクトリを
   ワークフローサーバの指定のディレクトリにコピーしてから実行してください。

1. インベントリ収集プロジェクトディレクトリ移動

   インベントリプロジェクトディレクトリに移動します。
   以下例では "vm1" というプロジェクトディレクトリに移動します。

   ::

      cd C:\cleansing_data\import\vm1

2. Redmine プラグイン用インベントリデータ登録

   以下のコマンドで、Redmineプラグイン用インベントリデータを登録してください。

   ::

      getconfig -u db-all

3. Redmine 設備チケット登録

   以下のコマンドで、Redmine 設備チケットを登録してください。
   以下の通り、機種ごとに-c オプションに各テンプレートの設定ファイルを指定して
   実行してください。

   * オンプレIAサーバの場合

      ::

         getconfig -rp {Redmineプロジェクト名}

   * オンプレSPARCサーバの場合

      ::

         getconfig -c .\template\Solaris\config_solaris.groovy -rp {Redmineプロジェクト名}

   * ESXiサーバの場合

      ::

         getconfig -c .\template\VMWare_ESXi\config_esxi.groovy -rp {Redmineプロジェクト名}

   * Zabbix監視設定の場合

      ::

         getconfig -c .\template\Zabbix\config_zabbix.groovy -rp {Redmineプロジェクト名}

   * NetAppストレージの場合

      ::

         getconfig -c .\template\NetApp\config_netapp.groovy -rp {Redmineプロジェクト名}

   * 日立VSPストレージの場合

      ::

         getconfig -c .\template\Hitachi_VSP\config_vsp.groovy -rp {Redmineプロジェクト名}

   * 富士通ETERNUSストレージの場合

      ::

         getconfig -c .\template\FJ_Eternus\config_eternus.groovy -rp {Redmineプロジェクト名}

