import spock.lang.Specification
import static groovy.json.JsonOutput.*
import groovy.json.*
import org.apache.commons.io.FileUtils
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

// gradle --daemon test --tests "WindowsBaseTest.Windows テスト仕様のロード"

class WindowsBaseTest extends Specification {

    String config_file = 'src/test/resources/config.groovy'
    TestPlatform test_platform

    def setup() {
        def template_text = new File('src/test/resources/template_win.json').text
        def template_json = new JsonSlurper().parseText(template_text)
        def template = new TestTemplate(name : 'Win', values : template_json)

        def test_target = new TestTarget(
            name              : 'win2012',
            ip                : '192.168.0.12',
            domain            : 'Windows',
            template_id       : 'Win',
            os_account_id     : 'Test',
            remote_account_id : 'Test',
            remote_alias      : 'win2012.ostrich',
            test_templates    : ['Win': template]
        )

       test_platform = new TestPlatform(
            name         : 'Windows',
            test_target  : test_target,
            dry_run      : true,
        )
    }

    def "Windows テスト仕様 os"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('os')
        platform_tester.run()

        then:
        println test_platform.test_results['os_caption']
        println test_platform.test_results['os_architecture']
        test_platform.test_results['os_caption'].status  == ResultStatus.OK
        test_platform.test_results['os_caption'].verify == ResultStatus.OK
        test_platform.test_results['os_architecture'].status  == ResultStatus.OK
        test_platform.test_results['os_architecture'].verify == ResultStatus.OK
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
        def json = new groovy.json.JsonBuilder()
        json(test_platform.test_results)
        println json.toPrettyString()

        test_platform.test_results['mhz'].value.size() > 0
        test_platform.test_results['model_name'].value.size() > 0

        test_platform.test_results['cpu_total'].status  == ResultStatus.OK
        test_platform.test_results['cpu_total'].verify == ResultStatus.OK
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
        println test_platform.test_results
        println test_platform.test_results['pyhis_mem']
        test_platform.test_results['pyhis_mem'].status  == ResultStatus.OK
        test_platform.test_results['pyhis_mem'].verify == ResultStatus.OK
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

    def "Windows テスト仕様 network"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('network')
        platform_tester.run()

        then:
        def json = new groovy.json.JsonBuilder()
        json(test_platform.test_results)
        println json.toPrettyString()

        test_platform.test_results.size() > 0
        test_platform.test_results['network'].status == ResultStatus.OK
        test_platform.test_results['net_config'].verify == ResultStatus.OK
    }

    def "Windows テスト仕様 filesystem"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('filesystem')
        platform_tester.run()

        then:
        test_platform.test_results.size() > 0
        println test_platform.test_results
        test_platform.test_results['filesystem'].status == ResultStatus.OK
        test_platform.test_results['filesystem'].verify == ResultStatus.OK
         // test_platform.test_results['Domain'].value.size() > 0
    }

    def "Windows テスト仕様 user"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('user')
        platform_tester.run()

        then:
        test_platform.test_results.size() > 0
        println test_platform.test_results
        // test_platform.test_results['user'].status == ResultStatus.OK
        // test_platform.test_results['user'].verify == ResultStatus.OK
         // test_platform.test_results['Domain'].value.size() > 0
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
        println test_platform.test_results
        // test_platform.test_results['service'].value.size() > 0
        test_platform.test_results['service'].devices.csv.size() > 0
        test_platform.test_results['service'].status == ResultStatus.OK
        test_platform.test_results['service'].verify == ResultStatus.OK
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

        def metric_text = new File('src/test/resources/metrics_Windows.json').text
        def metric_json = new JsonSlurper().parseText(metric_text) as String[]

        when:
        println metric_json
        platform_tester.set_test_items(metric_json)
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
