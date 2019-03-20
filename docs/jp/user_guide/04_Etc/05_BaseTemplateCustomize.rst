ベーステンプレートのカスタマイズ
================================

検査対象へのインベントリ収集の際に既定アカウントを使用する場合、
あらかじめベーステンプレートに既定アカウントの接続情報を定義することで、
インベントリ収集の際の検査シートの接続情報の入力を省略することが可能です。

Getconfig ホームディレクトリ(C:\\server-acceptance)下の以下の設定ファイル
に既定アカウントの接続情報を追加します。

* c:\\server-acceptance\\config\\config.groovy
* \\server-acceptance\\template 下の、config_{プラットフォーム名}.groovy

以下例の通り既定アカウントの接続情報を追加します。

::

   // vCenter接続情報(Aセグメント)
   account.vCenter.SEGMENTA.server   = '192.168.10.1'
   account.vCenter.SEGMENTA.user     = 'root@vsphere.local'
   account.vCenter.SEGMENTA.password = 'P@ssword'

   // Linux 接続情報(Aセグメント)
   account.Linux.SEGMENTA.user      = 'zabbix'
   account.Linux.SEGMENTA.password  = 'P@ssword'
   account.Linux.SEGMENTA.work_dir  = '/tmp/gradle_test'

   // Windows 接続情報(Aセグメント)
   account.Windows.SEGMENTA.user     = 'administrator'
   account.Windows.SEGMENTA.password = 'P@ssword'

"SEGMENTA"がユーザIDとなり、検査シートのユーザID列に指定します。

.. note::

   * 個別パスワードの入力について

      Windows はadministrator ユーザでの接続が必要となり、
      機器毎に個別のパスワードが設定されているケースがあります。
      また、既定アカウントのパスワードを変更している場合は、
      個別に設定が必要です。
      その場合は、OS設定の「特定パスワード」列に個別のパスワード
      を入力してください。

   * 特定プラットフォームのユーザID追加

      ユーザIDの追加は全プラットフォームの接続情報の定義が
      必要になります。Linux のみ既定アカウントを追加した場合も、
      vCenter, Windows などそのほかのプラットフォームも合わせて
      ユーザIDを追加してください。
