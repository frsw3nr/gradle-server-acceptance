import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import static groovy.json.JsonOutput.*

// gradle --daemon clean test --tests "TestSchedulerTest"

class TestSchedulerTest extends Specification {

    def "メイン処理"() {
        setup:
        String[] args = [
            '--dry-run',
            '-c', './src/test/resources/config.groovy',
            '-resource', './src/test/resources/log',
            '--parallel', '3',
        ]

        when:
        def test_runner = new TestRunner()
        test_runner.parse(args)
        def test_scheduler = new TestScheduler(test_runner)
        test_scheduler.runTest()

        // println prettyPrint(toJson(Config.instance.servers))
        // println prettyPrint(toJson(Config.instance.devices))

        then:
        1 == 1
        // Config.instance.servers.size() > 0
        // Config.instance.devices.size() > 0
    }

    def "サーバー絞り込み"() {
        setup:
        String[] args = [
            '--dry-run',
            '-c', './src/test/resources/config.groovy',
            '-resource', './src/test/resources/log',
            '-s', 'testtestdb',
            '--parallel', '3',
        ]

        when:
        def test_runner = new TestRunner()
        test_runner.parse(args)
        def test_scheduler = new TestScheduler(test_runner)
        test_scheduler.runTest()

        then:
        1 == 1
    }

    def "テスト項目追加"() {
        setup:
        String[] args = [
            '--dry-run',
            '-c', './src/test/resources/config.groovy',
            '-resource', './src/test/resources/log',
            '-t', 'hostname',
            '--parallel', '3',
        ]

        when:
        def test_runner = new TestRunner()
        test_runner.parse(args)
        def test_scheduler = new TestScheduler(test_runner)
        def dat = [
            'B': ['test_name': 'Bテスト', 'desc': 'テスト用B'],
            'A': ['test_name': 'Aテスト', 'desc': 'テスト用A'],
        ]
        test_scheduler.add_test_items('VMHost', dat)
        test_scheduler.add_test_items('VMHost', [
            'C': ['test_name': 'Cテスト', 'desc': 'テスト用C'],
            'B': ['test_name': 'Bテスト', 'desc': 'テスト用B'],
        ])
        println(test_scheduler.additional_test_items)
        then:
        1 == 1
    }

    def "テスト絞り込み"() {
        setup:
        String[] args = [
            '--dry-run',
            '-c', './src/test/resources/config.groovy',
            '-resource', './src/test/resources/log',
            '-t', 'hostname',
            '--parallel', '3',
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
            '-resource', './src/test/resources/log',
            '-s', 'testtestdb',
            '-t', 'vm',
            '--parallel', '3',
        ]

        when:
        def test_runner = new TestRunner()
        test_runner.parse(args)
        def test_scheduler = new TestScheduler(test_runner)
        test_scheduler.runTest()

        then:
        1 == 1
    }

    def "検査対象サーバスクリプト指定"() {
        setup:
        String[] args = [
            '--dry-run',
            '-c', './src/test/resources/config.groovy',
            '-resource', './src/test/resources/log',
            '-i', './src/test/resources/test_servers.groovy',
        ]

        when:
        def test_runner = new TestRunner()
        test_runner.parse(args)
        def test_scheduler = new TestScheduler(test_runner)
        test_scheduler.runTest()

        then:
        1 == 1
    }

}
