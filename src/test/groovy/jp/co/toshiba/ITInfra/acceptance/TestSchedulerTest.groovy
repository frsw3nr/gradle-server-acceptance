import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

// gradle --daemon clean test --tests "TestSchedulerTest"

class TestSchedulerTest extends Specification {

    def "メイン処理"() {
        setup:
        String[] args = [
            '--dry-run',
            '--verify',
            '-r', './src/test/resources/',
            '-p', '1',
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
