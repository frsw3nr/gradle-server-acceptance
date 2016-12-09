import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

// gradle --daemon clean test --tests "TestRunnerTest"

class TestRunnerTest extends Specification {


    def "実行オプション"() {
        setup:
        def test = new TestRunner()

        when:
        String[] args = ['-r', './src/test/resources/', '-c', './src/test/resources/config.groovy']
        test.parse(args)

        then:
        test.test_resource == './src/test/resources/'
        test.config_file == './src/test/resources/config.groovy'
    }

    def "長い名前のオプション"() {
        setup:
        String[] args = [
            '--dry-run',
            '--verify',
            '-c', './src/test/resources/config.groovy',
            '-r', './src/test/resources/',
            '-p', '3',
        ]

        when:
        def test_runner = new TestRunner()
        test_runner.parse(args)

        then:
        1==1
    }

    def "テストリソース指定"() {
        setup:
        def test = new TestRunner()

        when:
        String[] args = [
            '-r', './hoge/',
        ]
        test.parse(args)

        then:
        1 == 1
    }

    def "サーバ指定"() {
        setup:
        def test = new TestRunner()

        when:
        String[] args = [
            '-s', 'server1,server2,server3',
            '-c', './src/test/resources/config.groovy',
        ]
        test.parse(args)

        then:
        1 == 1
    }
}
