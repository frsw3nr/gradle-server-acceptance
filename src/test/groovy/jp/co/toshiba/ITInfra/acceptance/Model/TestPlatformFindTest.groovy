import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*
import jp.co.toshiba.ITInfra.acceptance.TestScheduler
import jp.co.toshiba.ITInfra.acceptance.Document.ExcelParser
import jp.co.toshiba.ITInfra.acceptance.Model.SpecModelQueryBuilder
import jp.co.toshiba.ITInfra.acceptance.Model.TestPlatform
import jp.co.toshiba.ITInfra.acceptance.Model.TestTarget
import jp.co.toshiba.ITInfra.acceptance.Model.TestMetric
import jp.co.toshiba.ITInfra.acceptance.Model.TestMetricSet
import jp.co.toshiba.ITInfra.acceptance.Model.RunStatus
import jp.co.toshiba.ITInfra.acceptance.Model.TestScenario
import spock.lang.Specification

// gradle --daemon test --tests "TestPlatformFindTest.全検査メトリック読込み"

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

    def "検索条件ビルダー"() {
        when:
        def query = new SpecModelQueryBuilder().target("hoge")
                                               .run_statuses([RunStatus.RUN])
                                               .exclude_status(true)
                                               .build()

        then:
        query.target == 'hoge'
        query.exclude_status == true
        query.run_statuses == [RunStatus.RUN]
    }

    def "全検査メトリック読込み"() {
        when:
        def test_metrics = TestMetric.search(test_scenario, 'Linux')

        then:
        1 == 1
        println test_metrics
        // def target_names = test_targets.collect { it.name }
        // target_names.sort() == ['cent7', 'cent7g', 'cent65a', 'ostrich', 'win2012'].sort()
    }

    def "全検査対象読込み"() {
        when:
        List<TestTarget> test_targets = TestTarget.search(test_scenario)

        then:
        def target_names = test_targets.collect { it.name }
        target_names.sort() == ['cent7', 'cent7g', 'cent65a', 'ostrich', 'win2012'].sort()
    }

    def "全検査対象キーワード絞込み"() {
        when:
        def query = new SpecModelQueryBuilder().target('cent')
                                               .build()
        List<TestTarget> test_targets = TestTarget.search(test_scenario, query)

        then:
        def target_names = test_targets.collect { it.name }
        target_names.sort() == ['cent7', 'cent7g', 'cent65a'].sort()
    }

    def "全検査対象キーワードとステータス絞込み"() {
        when:
        def query = new SpecModelQueryBuilder().target('cent')
                                               .run_statuses([RunStatus.RUN])
                                               .build()
        List<TestTarget> test_targets = TestTarget.search(test_scenario, query)

        then:
        def target_names = test_targets.collect { it.name }
        target_names.sort() == ['cent7', 'cent7g'].sort()
    }

    def "全検査対象ステータス絞込み"() {
        when:
        def query = new SpecModelQueryBuilder().run_statuses([RunStatus.INIT])
                                               .build()
        println("TEST1")
        List<TestTarget> test_targets = TestTarget.search(test_scenario, query)
        println("TEST2")

        then:
        def target_names = test_targets.collect { it.name }
        target_names.sort() == ['cent65a'].sort()
    }

    def "全検査対象ステータス以外絞込み"() {
        when:
        def query = new SpecModelQueryBuilder().run_statuses([RunStatus.INIT])
                                               .exclude_status(true)
                                               .build()
        List<TestTarget> test_targets = TestTarget.search(test_scenario, query)

        then:
        def target_names = test_targets.collect { it.name }
        target_names.sort() == ['ostrich', 'cent7', 'cent7g', 'win2012'].sort()
    }

    def "全検査プラットフォーム読込み"() {
        when:
        List<TestPlatform> testPlatforms = TestPlatform.search(test_scenario)

        then:
        def results = testPlatforms.collect { [it.getDomain(), it.getTarget(), it.getPlatform()] }
        results.sort() == [
            ['Linux', 'ostrich', 'vCenter'], 
            ['Linux', 'ostrich', 'Linux'], 
            ['Linux', 'cent7', 'vCenter'], 
            ['Linux', 'cent7', 'Linux'], 
            ['Linux', 'cent7g', 'vCenter'], 
            ['Linux', 'cent7g', 'Linux'], 
            ['Windows', 'win2012', 'vCenter'], 
            ['Windows', 'win2012', 'Windows']].sort()
    }

    def "全検査プラットフォームステータス絞り込み"() {
        when:
        def query = new SpecModelQueryBuilder().run_statuses([RunStatus.INIT])
                                               .exclude_status(true)
                                               .build()
        List<TestPlatform> testPlatforms = TestPlatform.search(test_scenario, query)

        then:
        def results = testPlatforms.collect { [it.getDomain(), it.getTarget(), it.getPlatform()] }
        println results
        results.sort() == [
            ['Linux', 'ostrich', 'vCenter'], 
            ['Linux', 'ostrich', 'Linux'], 
            ['Linux', 'cent7', 'vCenter'], 
            ['Linux', 'cent7', 'Linux'], 
            ['Linux', 'cent7g', 'vCenter'], 
            ['Linux', 'cent7g', 'Linux'], 
            ['Windows', 'win2012', 'vCenter'], 
            ['Windows', 'win2012', 'Windows']].sort()
    }

    def "全検査プラットフォームステータスとプラットフォーム絞り込み"() {
        when:
        def query = new SpecModelQueryBuilder().run_statuses([RunStatus.RUN])
                                               .platform('Linux')
                                               .build()
        List<TestPlatform> testPlatforms = TestPlatform.search(test_scenario, query)

        then:
        def results = testPlatforms.collect { [it.getDomain(), it.getTarget(), it.getPlatform()] }
        // println results
        results.sort() == [
            ['Linux', 'ostrich', 'Linux'], 
            ['Linux', 'cent7', 'Linux'], 
            ['Linux', 'cent7g', 'Linux']].sort()
    }
}
