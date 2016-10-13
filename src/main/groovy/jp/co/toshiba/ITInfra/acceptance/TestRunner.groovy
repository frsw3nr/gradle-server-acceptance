package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.util.CliBuilder

// gradlew run -Pargs="ls -alt *.groovy"

// サーバ情報収集結合
// ==================

// ToDo
// ----

// Windows Git
// プロパティ取得(test_resource)
// ルール定義シート
// TestRunner実装

// WindowsGit
// ----------

// リファレンス

// http://d.hatena.ne.jp/m-hiyama/20140203/1391381365

// Git for Windows
// https://git-for-windows.github.io/


// SourceTree for Windowsのインストール

// 一応インストール、Git bashのCUIのみでもよさそう

// git clone https://github.com/frsw3nr/gradle-server-acceptance.git

// $ java -version
// java version "1.8.0_101"
// Java(TM) SE Runtime Environment (build 1.8.0_101-b13)
// Java HotSpot(TM) 64-Bit Server VM (build 25.101-b13, mixed mode)

// 環境変数 PATH に . を追加

//     「コントロールパネル」を起動し
//     「システムとセキュリティ」をクリックします。
//     「システム」をクリックします。
//     左メニューの「システムの詳細設定」をクリックします。

// ついでに TEST_RESOURCE も追加

// C:\Users\minoru\Desktop\sfw\resource

// set PATH=.;$PATH
// set TEST_RESOURCE=C:\Users\minoru\Desktop\sfw\resource

// $ env|grep -i resource
// TEST_RESOURCE=C:\Users\minoru\Desktop\sfw\resource

// MinGW文字化け対策

// http://qiita.com/narupo/items/0f560c291dc65e09c62d

// 「右クリック」→「Options...」→「Text」の「locale」を「ja_JP」、「Character set」を「SJIS」

// Gradle テスト

// minoru@rooms2 MINGW64 ~
// $ cd Desktop/sfw/

// minoru@rooms2 MINGW64 ~/Desktop/sfw (master)
// $ gradlew --daemon clean test --tests "TestItemTest"

// git commit 手順

// vi .git/config

//  url = https://frsw3nr@github.com/frsw3nr/gradle-server-acceptance.git

// プロパティ取得
// ---------------

// gradlew jar

// ./build/libs/gradle-server-acceptance-0.1.0.jar
// ./gradle/wrapper/gradle-wrapper.jar

// ./build/scripts/gradle-server-acceptance.bat TEST_RESOURCE

// 実行可能なFatJarを配布する

// 実行に必要なファイルを1つのJarにまとめて配布します。
// これにより、ユーザは java -jar コマンドを叩くだけでアプリケーションを実行できるため、
// アーカイブの展開や配置といったインストールの手間が省けます。
// ただし、JVMのデフォルト引数はJarに組み込めないため、ユーザが指定する必要があります。

// https://github.com/johnrengelman/shadow

// 1.2.3

// ./gradlew shadowJar

// $ java -jar ./build/libs/gradle-server-acceptance-0.1.0-all.jar TEST_RESOURCE
// SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
// SLF4J: Defaulting to no-operation (NOP) logger implementation
// SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
// C:\Users\minoru\Desktop\sfw\resource

// java -jar ./build/libs/gradle-server-acceptance-0.1.0-all.jar \
// TEST_RESOURCE \
// -Dlogback.configurationFile=./config/logback.xml\
// -Dtest.config=./config/config.groovy 

// CLIBuilder
// -------------

// http://qiita.com/opengl-8080/items/4c1aa85b4737bd362d9e

// gradle run -Pargs="-c 3 -m Hello"

@Slf4j
class TestRunner {

    final created
    EvidenceSheet evidence

    TestRunner() {
        created = new Date().format("yyyyMMdd-HHmmss")
    }

    Boolean readEvidence() {
        //
    }

    Boolean writeEvidence() {
        //
    }

    Boolean runTest(String[] args) {
        def cli = new CliBuilder(usage:'ls')
        cli.a('display all files')
        cli.l('use a long listing format')
        cli.t('sort by modification time')
        def options = cli.parse(args)

        log.info('===========')
        log.info("opt a=" + options.a)
        cli.usage()
    }

    static void main(String[] args) {
        def test = new TestRunner()
        test.runTest(args)
        // def v=args[0]
        // println System.getenv()[v]
        // println test.created
    }
}
