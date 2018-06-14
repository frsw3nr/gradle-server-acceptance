import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

// gradle --daemon test --tests "TestRunnerTest.実行オプション"

class TestRunnerTest extends Specification {

    def "実行オプション"() {
        // test_runner で設定された変数が優先される
        // set_test_runner_config()内で、config_file を先に読み込む
        //
        // def test_env = ConfigTestEnvironment.instance
        // test_env.read_test_runner_config(test_runner)
        //     test_env.read_config(config_file)

        setup:
        def test_runner = new TestRunner()

        when:
        String[] args = ['-c', './src/test/resources/config.groovy']
        test_runner.parse(args)
        def test_env = ConfigTestEnvironment.instance
        test_env.read_from_test_runner(test_runner)
        def test_platform = new TestPlatform(name : 'Linux')
        test_env.accept(test_platform)

        then:
        def json = new groovy.json.JsonBuilder()
        json(test_platform)
        println json.toPrettyString()

        1 == 1
    }

    def "ドライランオプション"() {
        setup:
        def test_runner = new TestRunner()

        when:
        String[] args = ['-c', './src/test/resources/config.groovy', '-d']
        test_runner.parse(args)
        def test_env = ConfigTestEnvironment.instance
        test_env.read_from_test_runner(test_runner)
        def test_platform = new TestPlatform(name : 'Linux')
        test_env.accept(test_platform)

        then:
        def json = new groovy.json.JsonBuilder()
        json(test_platform)
        println json.toPrettyString()
        test_platform.dry_run == true
    }

    def "スケジューラオプション"() {
        setup:
        def test_runner = new TestRunner()

        when:
        String[] args = ['-c', './src/test/resources/config.groovy',
                         '-e', './src/test/resources/check_list.xlsx',
                         '-o', './build/test.xlsx',
                         '-parallel', '3',
                         '-s', 'win2012']
        test_runner.parse(args)
        def test_env = ConfigTestEnvironment.instance
        test_env.read_from_test_runner(test_runner)

        def test_scheduler = new TestScheduler()
        test_env.accept(test_scheduler)

        then:
        def json = new groovy.json.JsonBuilder()
        json(test_scheduler)
        println json.toPrettyString()
        test_scheduler.filter_server == 'win2012'
        test_scheduler.excel_file == './src/test/resources/check_list.xlsx'
        test_scheduler.output_evidence == './build/test.xlsx'
        test_scheduler.parallel_degree == 3
        1 == 1
    }

    def "スケジューラオプション 2"() {
        setup:
        def test_runner = new TestRunner()

        when:
        String[] args = ['-c', './src/test/resources/config.groovy',
                        '-dry-run',
                        ]
        test_runner.parse(args)
        def test_env = ConfigTestEnvironment.instance
        test_env.read_from_test_runner(test_runner)

        def test_scheduler = new TestScheduler()
        test_env.accept(test_scheduler)

        then:
        def json = new groovy.json.JsonBuilder()
        json(test_scheduler)
        println json.toPrettyString()
        test_scheduler.filter_server == null
        test_scheduler.excel_file == './src/test/resources/check_sheet.xlsx'
        // test_scheduler.output_evidence == './build/test.xlsx'
        test_scheduler.parallel_degree == 0
    }

    def "スケジュール実行"() {
        setup:
        def test_runner = new TestRunner()

        when:
        String[] args = ['-c', './src/test/resources/config.groovy',
                        '-dry-run',
                        ]
        test_runner.parse(args)
        def test_env = ConfigTestEnvironment.instance
        test_env.read_from_test_runner(test_runner)

        def test_scheduler = new TestScheduler()
        test_env.accept(test_scheduler)
        test_scheduler.init()
        test_scheduler.run()
        test_scheduler.finish()

        then:
        1 == 1
        // def json = new groovy.json.JsonBuilder()
        // json(test_scheduler)
        // println json.toPrettyString()
        // test_scheduler.filter_server == null
        // test_scheduler.excel_file == './src/test/resources/check_sheet.xlsx'
        // // test_scheduler.output_evidence == './build/test.xlsx'
        // test_scheduler.parallel_degree == 0
    }
}
