Git バージョンアップ(オプション)
================================

.. note::

   構成管理データベースでサブモジュールの Git リポジトリを扱う場合は、
   CentOS6標準のパッケージバージョン v.1.7でエラーになります。
   本エラーを回避するため、居にかの手順で Git をバージョンアップします。


Git バージョンアップ
--------------------

::

   sudo -E yum -y remove git
   sudo -E yum -y install curl-devel expat-devel gettext-devel openssl-devel perl-devel zlib-devel autoconf asciidoc xmlto docbook2X make gcc
   sudo ln -s /usr/bin/db2x_docbook2texi /usr/bin/docbook2x-texi
   wget https://github.com/git/git/archive/v2.19.1.tar.gz
   tar -zxf v2.15.1.tar.gz
   cd git-2.15.1
   make configure
   ./configure --prefix=/usr
   make all doc info
   sudo -E make install install-doc install-html install-info
   git --version

