import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*
import jp.co.toshiba.ITInfra.acceptance.TestScheduler
import jp.co.toshiba.ITInfra.acceptance.Document.ExcelParser
import jp.co.toshiba.ITInfra.acceptance.Model.TestPlatform
import jp.co.toshiba.ITInfra.acceptance.Model.TestTarget
import jp.co.toshiba.ITInfra.acceptance.Model.RunStatus
import jp.co.toshiba.ITInfra.acceptance.Model.TestScenario
import spock.lang.Specification

// gradle --daemon test --tests "TestPlatformFindTest.全検査対象読込み"

class TestPlatformFindTest extends Specification {
    static TestScenario test_scenario

    def setupSpec() {
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)
        def test_scheduler = new TestScheduler()
        test_scheduler.make_test_platform_tasks(test_scenario)
    }

    def "全検査対象読込み"() {
        when:
        List<TestTarget> test_targets = TestTarget.find(test_scenario)

        then:
        test_targets.size() > 0
    }

    def "全検査対象キーワード絞込み"() {
        when:
        List<TestTarget> test_targets = TestTarget.find(test_scenario, 'cent')
        println test_targets
        then:
        test_targets.size() > 0
    }

    def "全検査対象キーワードとステータス絞込み"() {
        when:
        List<TestTarget> test_targets = TestTarget.find(test_scenario, 'cent', [RunStatus.RUN])
        println test_targets
        then:
        test_targets.size() > 0
    }

    def "全検査対象ステータス絞込み"() {
        when:
        List<TestTarget> test_targets = TestTarget.find(test_scenario, null, [RunStatus.INIT])
        println test_targets
        then:
        test_targets.size() > 0
    }

    def "全検査対象ステータス以外絞込み"() {
        when:
        List<TestTarget> test_targets = TestTarget.findExcludeStatus(test_scenario, null, [RunStatus.INIT])
        println test_targets
        then:
        test_targets.size() > 0
    }

    def "全検査プラットフォーム読込み"() {
        when:
        List<TestPlatform> testPlatforms = TestPlatform.find(test_scenario)
        // List<TestTarget> test_targets = TestTarget.find(test_scenario)
        testPlatforms.each { testPlatform ->
            // def test_target = testPlatform.test_target
            // println "${testPlatform.name} ${test_target.name} ${test_target.domain}"
            println "${testPlatform.getDomain()} ${testPlatform.getTarget()} ${testPlatform.getPlatform()}"
        }
        then:
        testPlatforms.size() > 0
    }

}
