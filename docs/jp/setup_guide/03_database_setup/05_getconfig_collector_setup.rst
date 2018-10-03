Getconfigインストール(オプション)
=================================

.. note::

   構成管理データベースでインベントリ収集をする場合は、以下手順でツールをインストールします。

構成概要
--------

* ビルドツール Gradle を /opt/gradle インストールします
* GitHub サイトからソースをダウンロードします
* psadmin 管理者ユーザのホームの直下に、Getconfig インベントリ収集ツールソースを配布します
* ソースから Getconfig インベントリ収集ツールをビルドします

ビルドツール Gradleインストール
-------------------------------

JDK(Java-1.8.0) をインストールします。

::

   sudo -E yum install -y java-1.8.0-openjdk-devel

Gradle ダウンロードサイトから最新版バイナリを確認します。

::

   https://services.gradle.org/distributions/

最新版のzipファイルをダウンロードします。

::

   cd /tmp
   wget https://services.gradle.org/distributions/gradle-4.10.2-all.zip

/opt/gradle 配下に解凍します。

::

   sudo mkdir -p /opt/gradle
   sudo unzip gradle-4.10.2-all.zip -d /opt/gradle

リンクを作成します。

::

   sudo ln -s /opt/gradle/gradle-4.10.2 /opt/gradle/gradle

環境変数を設定します。

::

   vi ~/.bash_profile

最終行に以下の行を追加します。

::

   export GRADLE_HOME=/opt/gradle/gradle
   export PATH=$GRADLE_HOME/bin:$PATH

source コマンドで環境変数を読込みます。

::

   source ~/.bash_profile

gradle -version で、gradle の実行を確認します。

::

   gradle -version

Getconfig インベントリ収集ツールのビルド
----------------------------------------

GitHub サイトから、ソースをダウンロードします。

::

   cd $HOME
   git clone https://github.com/frsw3nr/gradle-server-acceptance.git

gradle コマンドでビルドします。

::

   cd gradle-server-acceptance
   gradle zip

.. note::

   ユニットテストを実行する場合は、「gradle test」を実行してください。

環境変数を設定します。

::

   vi ~/.bash_profile

最終行に以下の行を追加します。

::

   export PATH=$HOME/gradle-server-acceptance:$PATH

source コマンドで環境変数を読込みます。

::

   source ~/.bash_profile

getconfig --help で、Getconfig の実行を確認します。

::

   getconfig --help
