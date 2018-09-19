PowerCLIインストール
====================

 Chocolatey で VMware PowerCLI のインストールします。
バージョンは PowerCLI 6.x の最新を選びます。

::

   choco install vmware-powercli-psmodule --version 6.5.4.7155375

.. note::

   2018/2/17から Chocolatey は、 VMware PowerCLI をサポートするようになりました。

.. VMware PowerCLI のインストールは Chocolatey がまだ未サポートのため、手動でインストールします。

.. VMWareサイトから PowerCLI モジュールをダウンロードしてインストールします。
.. バージョンは PowerCLI 6.x を選びます。

.. ::

..    Install-Module -Name VMware.PowerCLI -RequiredVersion 6.5.4.7155375


.. NuGet プロバイダーをインストールしますか? の質問に Y を入力します。
.. 'PSGallery' からソフトウェアをアンインストールしますか? の質問に Y を入力します。

.. PowerCLI のインストールは以下のサイトを参照してください。

.. ::

..    https://www.vmware.com/support/developer/PowerCLI/

.. .. note::

..    ダウンロードには VMWare アカウントが必要となり、未登録の場合はサインアップしてください。

.. ダウンロードした VMWare-PowerCLI-\*.exe を起動して、既定の設定でインストールします。

.. 一旦、ここでOSを再起動します。

