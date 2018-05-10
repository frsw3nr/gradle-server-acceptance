import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

// gradle --daemon test --tests "TestRunnerTest"

class TestRunnerTest extends Specification {

    def "実行オプション"() {
        setup:
        def test = new TestRunner()

        when:
        String[] args = ['--resource', './src/test/resources/', '-c', './src/test/resources/config.groovy']
        test.parse(args)

        then:
        test.test_resource == './src/test/resources/'
        test.config_file == './src/test/resources/config.groovy'
    }

    def "configセット"() {
        setup:
        def test = new TestRunner()
        ConfigTestEnvironment test_env = ConfigTestEnvironment.instance

        when:
        String[] args = ['--resource', './src/test/resources/', '-c', './src/test/resources/config.groovy']
        test.parse(args)
        test_env.read_config(test.config_file)
        test_env.read_test_args(test)

        then:
        test_env.config.getconfig_home == '.'
        test_env.config.dry_run == false
        test_env.config.evidence != null
    }

    def "長い名前のオプション"() {
        setup:
        String[] args = [
            '--dry-run',
            '--verify',
            '-c', './src/test/resources/config.groovy',
            '--resource', './src/test/resources/',
            '--parallel', '3',
        ]

        when:
        def test_runner = new TestRunner()
        test_runner.parse(args)

        then:
        1==1
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
