import jp.co.toshiba.ITInfra.acceptance.Model.TestMetric
import jp.co.toshiba.ITInfra.acceptance.Model.TestPlatform
import jp.co.toshiba.ITInfra.acceptance.Model.TestRule
import jp.co.toshiba.ITInfra.acceptance.Model.TestTarget
import jp.co.toshiba.ITInfra.acceptance.PlatformTester
import spock.lang.Specification

// gradle --daemon test --tests "PlatformTesterTest.Linuxテストタスク"

class PlatformTesterTest extends Specification {
    // def test_platform_tasks = [:].withDefault{[]}
    // TestRunner test_runner
    String config_file = 'src/test/resources/config.groovy'
    TestPlatform test_platform

    def setup() {
        def test_target = new TestTarget(
            name              : 'ostrich',
            ip                : '192.168.10.1',
            domain            : 'Linux',
            os_account_id     : 'Test',
            remote_account_id : 'Test',
            remote_alias      : 'ostrich',
        )

        def test_rule = new TestRule(name : 'AP',
                                 compare_rule : 'Actual',
                                 compare_source : 'centos7')

        def test_metrics = [
            'uname': new TestMetric(name : 'uname', enabled : true),
            'cpu'  : new TestMetric(name : 'cpu', enabled : true),
        ]

        test_platform = new TestPlatform(
            name         : 'Linux',
            test_target  : test_target,
            test_metrics : test_metrics,
            test_rule    : test_rule,
            dry_run      : true,
        )
    }

    def "初期化"() {
        when:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        then:
        platform_tester.test_spec != null
    }

    def "Linuxテストタスク"() {
        when:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file : config_file)
        platform_tester.init()
        platform_tester.run()

        then:
        test_platform.test_results.size() > 0
    }

    def "Linuxメトリック指定"() {
        when:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file : config_file)
        platform_tester.init()
        platform_tester.set_test_items('hostname', 'hostname_fqdn', 'cpu')
        platform_tester.run()

        then:
        test_platform.test_results['hostname'].value.size() > 0
        test_platform.test_results['hostname_fqdn'].value.size() > 0
        test_platform.test_results['cpu'].value.size() > 0
    }

    def "Linuxデバイステストタスク"() {
        setup:
        def test_metrics = [
            'network': new TestMetric(name : 'network', enabled : true),
        ]
        test_platform.test_metrics = test_metrics

        when:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file : config_file)
        platform_tester.init()
        platform_tester.run()

        then:
        test_platform.test_results.size() > 0
        test_platform.test_results['network'].devices.csv.size() > 0
    }

    // def "テストタスク絞り込み"() {
    //     when:
    //     def test_scheduler = new PlatformTester(filter_server: 'centos7')
    //     def tasks = test_scheduler.make_test_platform_tasks(test_scenario)

    //     then:
    //     tasks.size() == 2
    // }

    // def "シナリオ読み込み"() {
    //     when:
    //     def test_scheduler = new PlatformTester(platform_tester: platform_tester)
    //     test_scenario.accept(test_scheduler)

    //     then:
    //     1 == 1
    // }

    // def "多重実行"() {
    //     when:
    //     def test_scheduler = new PlatformTester(serialize_platforms: ['vCenter': true],
    //                                            parallel_degree: 3,
    //                                            platform_tester: platform_tester)
    //     test_scenario.accept(test_scheduler)

    //     then:
    //     1 == 1
    // }
}
