Git 2 アップデート


$ sudo yum -y remove git
$ sudo yum -y install curl-devel expat-devel gettext-devel openssl-devel perl-devel zlib-devel autoconf asciidoc xmlto docbook2X make gcc
$ sudo ln -s /usr/bin/db2x_docbook2texi /usr/bin/docbook2x-texi
$ wget https://github.com/git/git/archive/v2.15.1.tar.gz
$ tar -zxf v2.15.1.tar.gz
$ cd git-2.15.1
$ make configure
$ ./configure --prefix=/usr
$ make all doc info
$ sudo make install install-doc install-html install-info
$ git --version
