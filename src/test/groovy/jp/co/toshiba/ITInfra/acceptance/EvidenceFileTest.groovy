import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import groovy.sql.Sql

// gradle --daemon clean test --tests "EvidenceFileTest.メイン処理"

class EvidenceFileTest extends Specification {

    def "メイン処理"() {
        setup:
        String[] args = [
            '--dry-run',
            '-c', './src/test/resources/config.groovy',
            '-r', './src/test/resources/log',
        ]

        when:
        def test_runner = new TestRunner()
        test_runner.parse(args)
        def test_scheduler = new TestScheduler(test_runner)
        test_scheduler.runTest()

        then:
        1 == 1
    }

    def "サーバー絞り込み"() {
        setup:
        String[] args = [
            '--dry-run',
            '-c', './src/test/resources/config.groovy',
            '-r', './src/test/resources/log',
            '-s', 'testtestdb',
            '-p', '3',
        ]

        when:
        def test_runner = new TestRunner()
        test_runner.parse(args)
        def test_scheduler = new TestScheduler(test_runner)
        test_scheduler.runTest()

        then:
        1 == 1
    }

    def "テスト絞り込み"() {
        setup:
        String[] args = [
            '--dry-run',
            '-c', './src/test/resources/config.groovy',
            '-r', './src/test/resources/log',
            '-t', 'hostname',
            '-p', '3',
        ]

        when:
        def test_runner = new TestRunner()
        test_runner.parse(args)
        def test_scheduler = new TestScheduler(test_runner)
        test_scheduler.runTest()

        then:
        1 == 1
    }

    def "サーバ、テスト絞り込み"() {
        setup:
        String[] args = [
            '--dry-run',
            '-c', './src/test/resources/config.groovy',
            '-r', './src/test/resources/log',
            '-s', 'testtestdb',
            '-t', 'vm',
            '-p', '3',
        ]

        when:
        def test_runner = new TestRunner()
        test_runner.parse(args)
        def test_scheduler = new TestScheduler(test_runner)
        test_scheduler.runTest()

        then:
        1 == 1
    }

    def "DB登録"() {
        setup:
        def home = System.getProperty("user.dir")

        when:
        def config   = 'src/test/resources/config_db.groovy'
        def last_run = 'src/test/resources/log/.last_run'
        def evidence = new EvidenceFile(home: home, db_config: config,
                                        last_run_config: last_run)
        evidence.exportCMDB()

        then:
        1 == 1
    }

    def "DB全体登録"() {
        setup:
        def home = System.getProperty("user.dir")

        when:
        def config   = 'src/test/resources/config_db.groovy'
        def last_run = 'src/test/resources/log/.last_run'
        def evidence = new EvidenceFile(home: home, db_config: config,
                                        last_run_config: last_run)
        evidence.exportCMDBAll()

        then:
        thrown(IOException)
    }
}
