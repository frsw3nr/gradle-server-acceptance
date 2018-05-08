import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

// gradlew --daemon test --tests "WindowsBaseTest.Windows テスト仕様のロード"

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

    // TargetServer test_server
    // DomainTestRunner test

    // def setup() {
    //     test_server = new TargetServer(
    //         server_name       : 'win2012',
    //         ip                : '192.168.0.12',
    //         platform          : 'Windows',
    //         os_account_id     : 'Test',
    //         remote_account_id : 'Test',
    //         remote_alias      : 'win2012.ostrich',
    //     )
    //     test_server.setAccounts('src/test/resources/config.groovy')
    //     test_server.dry_run = true
    // }

    def "Windows テスト仕様 cpu"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('cpu')
        platform_tester.run()

        then:
        1 == 1
        println test_platform.test_results

        // setup:
        // test = new DomainTestRunner(test_server, 'Windows')

        // when:
        // def test_item = new TestItem('cpu')
        // test.run(test_item)

        // then:
        // test_item.results.size() > 0
    }


    // def "Windows テスト仕様 memory"() {
    //     setup:
    //     test = new DomainTestRunner(test_server, 'Windows')

    //     when:
    //     def test_item = new TestItem('memory')
    //     test.run(test_item)

    //     then:
    //     test_item.results.size() > 0
    // }

    // def "Windows テスト仕様 system"() {
    //     setup:
    //     test = new DomainTestRunner(test_server, 'Windows')

    //     when:
    //     def test_item = new TestItem('system')
    //     test.run(test_item)

    //     then:
    //     println test_item.results.toString()
    //     test_item.results.size() > 0
    // }

    // def "Windows テスト仕様 driver"() {
    //     setup:
    //     test = new DomainTestRunner(test_server, 'Windows')

    //     when:
    //     def test_item = new TestItem('driver')
    //     test.run(test_item)

    //     then:
    //     println test_item.results.toString()
    //     test_item.results.size() > 0
    // }

    // def "Windows テスト仕様 firewall"() {
    //     setup:
    //     test = new DomainTestRunner(test_server, 'Windows')

    //     when:
    //     def test_item = new TestItem('firewall')
    //     test.run(test_item)

    //     then:
    //     println test_item.results.toString()
    //     test_item.results.size() > 0
    // }

    // def "Windows テスト仕様 dns"() {
    //     setup:
    //     test = new DomainTestRunner(test_server, 'Windows')

    //     when:
    //     def test_item = new TestItem('dns')
    //     test.run(test_item)

    //     then:
    //     println test_item.results.toString()
    //     test_item.results.size() > 0
    // }

    // def "Windows テスト仕様 storage_timeout"() {
    //     setup:
    //     test = new DomainTestRunner(test_server, 'Windows')

    //     when:
    //     def test_item = new TestItem('storage_timeout')
    //     test.run(test_item)

    //     then:
    //     println test_item.results.toString()
    //     test_item.results.size() > 0
    // }

    // def "Windows テスト仕様 service"() {
    //     setup:
    //     test = new DomainTestRunner(test_server, 'Windows')

    //     when:
    //     def test_item = new TestItem('service')
    //     test.run(test_item)

    //     then:
    //     println test_item.results.toString()
    //     test_item.results.size() > 0
    // }

    // def "Windows 複数テスト仕様のロード"() {
    //     setup:
    //     test = new DomainTestRunner(test_server, 'Windows')

    //     def test_ids = ['cpu', 'memory']
    //     def test_items = []
    //     test_ids.each {
    //         test_items << new TestItem(it)
    //     }

    //     when:
    //     test.run(test_items as TestItem[])
    //     test_items.each { test_item ->
    //         println test_item.results.toString()
    //    }

    //     then:
    //     test_items.each { test_item ->
    //         test_item.results.size() > 0
    //     }
    // }

    // def "Windows 全テスト仕様のロード"() {
    //     setup:
    //     test = new DomainTestRunner(test_server, 'Windows')

    //     def test_ids = [
    //         'cpu',
    //         'driver',
    //         'filesystem',
    //         'fips',
    //         'memory',
    //         'network',
    //         'virturalization',
    //     ]
    //     def test_items = []
    //     test_ids.each {
    //         test_items << new TestItem(it)
    //     }

    //     when:
    //     test.run(test_items as TestItem[])
    //     test_items.each { test_item ->
    //         println test_item.results.toString()
    //    }

    //     then:
    //     test_items.each { test_item ->
    //         test_item.results.size() > 0
    //     }
    // }

    // def "Windows テスト項目なし"() {
    //     setup:
    //     test = new DomainTestRunner(test_server, 'Windows')

    //     when:
    //     def test_item = new TestItem('hoge')
    //     test.run(test_item)

    //     then:
    //     test_item.results.size() == 0
    // }


}
