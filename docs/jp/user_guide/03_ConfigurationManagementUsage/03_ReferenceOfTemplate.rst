Getconfig リファレンス
======================

各インベントリ収集テンプレートの利用手順は、以下Excelファイル内シート「利用手順」に
記載しています。
「getconfig -g {プロジェクトディレクトリ}」で作成したプロジェクトディレクトリ下に
各Excel ファイルがコピーされますので、本ファイルを参照して順に実行します。

* 仮想マシン(VM)

   - vCenter/OS

      + サーバチェックシート.xlsx

* オンプレIAサーバ

   - HW

      + HP IAサーバ : template/HP_iLO/iLOチェックシート.xlsx
      + Cisco UCS : template/Cisco_UCS/UCSチェックシート.xlsx
      + 富士通 IAサーバ : template/FJ_Primergy/PRIMERGYチェックシート.xlsx
      
   - OS

      + サーバチェックシート.xlsx

* オンプレSPARCサーバ

   - HW

      + template/Solaris/XSCFチェックシート.xlsx

   - OS

      + template/Solaris/Solarisチェックシート.xlsx

* ESXi サーバ        

   - HW

      + HP IAサーバ : template/HP_iLO/iLOチェックシート.xlsx
      + Cisco UCS : template/Cisco_UCS/UCSチェックシート.xlsx

   - vCenter

      + template/VMWare_ESXi/ESXiチェックシート.xlsx

* 仮想化用ストレージ 

   - HW

      + template/NetApp/DataONTAPチェックシート.xlsx

* オンプレストレージ 

   - HW

      + 日立VSP : template/Hitachi_VSP/HitachiVSPチェックシート.xlsx
      + 富士通ETERNUS : template/FJ_Eternus/ETERNUSチェックシート.xlsx

* 監視設定

   - Zabbix 監視対象設定

      + template/Zabbix/Zabbix監視設定チェックシート.xlsx

.. note::

   * オンプレ IA サーバ、オンプレ SPARC サーバ、ESXi サーバの場合、HWとOSで
     異なるインベントリ収集テンプレートを実行しますが、
     その際の注意点としてシート「検査対象」の「対象サーバ」列の入力は、
     HW, OS ともに同一名にしてください。

.. template/Oracle/Oracle設定チェックシート.xlsx
.. template/Router/RTXチェックシート.xlsx
.. template/Router/CiscoIOSチェックシート.xlsx
.. template/AIX/AIXチェックシート.xlsx
