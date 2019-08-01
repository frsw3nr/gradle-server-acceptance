イントラネット環境での設定
==========================

.. note:: 

    インストールは外部インターネットから各種オープンソースをダウンロードして行います。
    イントラネット環境でプロキシー経由でのアクセスが必要な場合は、以下のプロキシー設定
    が必要になります。

.. note:: プロキシー設定が不要な場合は本セクションの設定はスキップしてください。

プロキシー設定
--------------

以下の設定は前述の psadmin ユーザで行います。

プロキシー環境の設定
~~~~~~~~~~~~~~~~~~~~

これからの作業はGetperf管理ユーザで行います。プロキシーサーバを
proxy.your.company.co.jp、接続ポートを 8080　を例にして設定手順を記します。

/etc/hosts にプロキシーサーバを追加
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

::

    sudo vi /etc/hosts

最終行に以下例の通りプロキシーサーバを追加します。

::

    xxx.xxx.xxx.xxx  proxy.your.company.co.jp

管理ユーザの環境変数設定
^^^^^^^^^^^^^^^^^^^^^^^^

::

    vi $HOME/.bash_profile

最終行にプロキシーの環境変数を追加します。ついでに PATH の設定に /usr/local/bin を追加します。

::

    PATH=$PATH:$HOME/bin:/usr/local/bin

    export PATH

    export http_proxy=http://proxy.your.company.co.jp:8080
    export HTTP_PROXY=http://proxy.your.company.co.jp:8080
    export https_proxy=http://proxy.your.company.co.jp:8080
    export HTTPS_PROXY=http://proxy.your.company.co.jp:8080
    export ftp_proxy=http://proxy.your.company.co.jp:8080

環境変数設定読込

::

    source ~/.bash_profile

yumのプロキシー設定
^^^^^^^^^^^^^^^^^^^

::

    sudo vi /etc/yum.conf

最終行にプロキシーの環境変数を追加します。

::

    Proxy=http://proxy.your.company.co.jp:8080

Gradleのプロキシー設定
~~~~~~~~~~~~~~~~~~~~~~

::

    mkdir -p ~/.gradle/
    vi ~/.gradle/gradle.properties

設定例

::

    systemProp.http.proxyHost=proxy.your.company.co.jp
    systemProp.http.proxyPort=8080
    systemProp.http.proxyUser=
    systemProp.http.proxyPassword=

    systemProp.https.proxyHost=proxy.your.company.co.jp
    systemProp.https.proxyPort=8080
    systemProp.https.proxyUser=
    systemProp.https.proxyPassword=

    org.gradle.daemon=true

/etc/hosts 編集
~~~~~~~~~~~~~~~

ネームサーバが有効になっていない環境の場合、自身のサーバのアドレス設定をします。

::

    sudo vi /etc/hosts

以下の行を追加します。

::

    XX.XX.XX.XX    自身のサーバのホスト名

社内認証局の証明書インポート
----------------------------

セキュリティ対策で、ウェブサイトのアクセスで認証局による SSL　認証が必要な場合は、社外用認証局証明書をインストールします。

OpenSSLセットアップ
~~~~~~~~~~~~~~~~~~~

社内 IS　部門サイトから、認証局証明書保存ディレクトリに証明書をダウンロードします。
以下作業は全てrootで実行します。以下例では、intra_ssl_cert.zip　という証明書アーカイブファイルをダウンロードして、
intra_ssl_cert.cer　をインポートする例を記します。

root にスイッチユーザします。

::

    sudo su -

.. note::

    /tmpに社内証明書をダウンロードします。

SSL証明書保存ディレクトリに移動して、証明書をダウンロード・解凍します。

::

    cd /tmp
    wget http://xx.xx.xxx.xxx/YYY/intra_ssl_cert.zip --no-proxy

    cd /etc/pki/tls/certs/
    unzip intra_ssl_cert.zip
    rm -f intra_ssl_cert.zip

ca-bundle.crt のバックアップを取ります。

::

    cp -p ca-bundle.crt ca-bundle.crt.bak

解凍した社外の証明書をca-bundle.crt に登録(アペンド)します。

::

    cat intra_ssl_cert.cer >> ca-bundle.crt

Java SSLセットアップ
^^^^^^^^^^^^^^^^^^^^

keytool を用いて、上記でダウンロードした証明書をJavaにインストールします。

::

    keytool -import -alias IntraRootCA -keystore /etc/pki/java/cacerts -file /etc/pki/tls/certs/intra_ssl_cert.cer

Enter keystore password:と聞かれる場合は、CentOS
JDKデフォルトの"changeit"を入力します

.. note::

    keytool が入っていない場合は、 sudo -E yum -y install
    java-1.7.0-openjdk-devel で JDK をインストールしてください

