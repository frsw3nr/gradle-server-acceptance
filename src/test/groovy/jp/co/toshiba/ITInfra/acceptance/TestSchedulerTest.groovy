import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

// gradle --daemon clean test --tests "TestSchedulerTest"

class TestSchedulerTest extends Specification {

    def "並列実行のテスト"() {
        setup:
        def scheduler = new TestScheduler()

        when:
        scheduler.runTest()

        then:
        1 == 1
    }

}
