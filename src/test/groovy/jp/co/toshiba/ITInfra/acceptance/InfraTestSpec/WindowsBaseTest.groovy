import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

// gradle --daemon test --tests "WindowsBaseTest.Windows テスト仕様のロード"

class WindowsBaseTest extends Specification {

    String config_file = 'src/test/resources/config.groovy'
    TestPlatform test_platform

    def setup() {
        def test_target = new TestTarget(
            name              : 'win2012',
            ip                : '192.168.0.12',
            domain            : 'Windows',
            os_account_id     : 'Test',
            remote_account_id : 'Test',
            remote_alias      : 'win2012.ostrich',
        )

        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        def test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)
        def test_metrics = test_scenario.test_metrics.get('Windows').get('Windows').get_all()

        test_platform = new TestPlatform(
            name         : 'Windows',
            test_target  : test_target,
            test_metrics : test_metrics,
            dry_run      : true,
        )
    }

    def "Windows テスト仕様 cpu"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('cpu')
        platform_tester.run()

        then:
        test_platform.test_results['mhz'].value.size() > 0
        test_platform.test_results['model_name'].value.size() > 0
    }

    def "Windows テスト仕様 memory"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('memory')
        platform_tester.run()

        then:
        test_platform.test_results.size() > 0
        test_platform.test_results['total_virtual'].value.size() > 0
    }

    def "Windows テスト仕様 system"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('system')
        platform_tester.run()

        then:
        test_platform.test_results.size() > 0
        // println test_platform.test_results
        test_platform.test_results['Domain'].value.size() > 0
    }

    def "Windows テスト仕様 driver"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('driver')
        platform_tester.run()

        then:
        test_platform.test_results.size() > 0
        test_platform.test_results['driver'].devices.csv.size() > 0
    }

    def "Windows テスト仕様 firewall"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('firewall')
        platform_tester.run()

        then:
        test_platform.test_results.size() > 0
        // println test_platform.test_results
        test_platform.test_results['firewall'].devices.csv.size() > 0
    }

    def "Windows テスト仕様 dns"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('dns')
        platform_tester.run()

        then:
        test_platform.test_results.size() > 0
        test_platform.test_results['dns'].value.size() > 0
    }

    def "Windows テスト仕様 storage_timeout"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('storage_timeout')
        platform_tester.run()

        then:
        test_platform.test_results.size() > 0
        // println test_platform.test_results
        test_platform.test_results['storage_timeout'].value.size() > 0
    }

    def "Windows テスト仕様 service"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('service')
        platform_tester.run()

        then:
        test_platform.test_results.size() > 0
        // println test_platform.test_results
        // test_platform.test_results['service'].value.size() > 0
        test_platform.test_results['service'].devices.csv.size() > 0
    }

    def "Windows 複数テスト仕様のロード"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('cpu', 'memory')
        platform_tester.run()

        then:
        test_platform.test_results.size() > 0
        println test_platform.test_results
        test_platform.test_results['mhz'].value.size() > 0
        test_platform.test_results['total_virtual'].value.size() > 0
    }

    def "Windows 全テスト仕様のロード"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.run()

        then:
        test_platform.test_results.size() > 0
    }


    def "Windows テスト項目なし"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items()
        platform_tester.run()

        then:
        test_platform.test_results.size() == 0
    }

}
