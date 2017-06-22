利用手順
========

Redmine 構成管理データベース、Getconfig構成収集ツールの利用手順を記します。
以下のESXi 上に構築した VM 2台のデモ環境をリファレンスとします。

   .. figure:: ../../image/tutorial_env.png
      :align: center
      :alt: CMDB Overview
      :width: 640px

はじめにRedmineプロジェクトの登録をします。
プロジェクト名を「構成管理データベース」、サブプロジェクト名を「監視サイト」としてプロジェクトを登録します。
ユーザは管理者用に「担当 A」、担当者用に「担当 B」を追加します。
管理対象は、CentOS 6 と Windows Server 2012 の2台とし、
構成管理システムの検証用としての利用を想定しています。
課題は「構成管理システム検証検証環境構築」とし、対応バージョンは「2017年上期」とします。

.. toctree::

   01_Planning/index
   02_InventoryCollection/index
   03_ChangeManagement/index