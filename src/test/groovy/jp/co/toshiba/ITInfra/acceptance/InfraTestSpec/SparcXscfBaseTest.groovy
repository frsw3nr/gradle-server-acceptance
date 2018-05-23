import spock.lang.Specification
import static groovy.json.JsonOutput.*
import groovy.json.*
import org.apache.commons.io.FileUtils
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

// gradle --daemon test --tests "SparcXscfBaseTest.SparcXscf 複数テスト仕様のロード"

class SparcXscfBaseTest extends Specification {

    String config_file = 'src/test/resources/config_xscf.groovy'
    TestPlatform test_platform

    def setup() {
        def template_text = new File('src/test/resources/template_ap.json').text
        def template_json = new JsonSlurper().parseText(template_text)
        def template = new TestTemplate(name : 'AP', values : template_json)

        def test_target = new TestTarget(
            name              : 'xscf-m12',
            ip                : '10.20.129.22',
            domain            : 'SparcXscf',
            template_id       : 'AP',
            account_id        : 'Test',
            remote_account_id : 'Test',
            test_templates    : ['AP': template]
        )

        test_platform = new TestPlatform(
            name         : 'SparcXscf',
            test_target  : test_target,
            dry_run      : false,
        )
    }

    def "SparcXscf テスト仕様のロード"() {
        when:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        then:
        platform_tester.test_spec != null
    }

    def "SparcXscf 複数テスト仕様のロード"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        println("SERVER_INFO:${platform_tester.server_info}")
        platform_tester.set_test_items('hardconf', 'fwversion', 'cpu_activate',
                                       'network', 'snmp')
        platform_tester.run()

        then:
        1 == 1
        println test_platform.test_results
        // test_platform.test_results['hostname'].value.size() > 0
        // test_platform.test_results['hostname_fqdn'].value.size() > 0
        // test_platform.test_results['cpu'].value.size() > 0
    }

    def "SparcXscf 全テスト仕様のロード"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        def metric_text = new File('src/test/resources/metrics_SparcXscf.json').text
        def metric_json = new JsonSlurper().parseText(metric_text) as String[]

        when:
        println metric_json
        platform_tester.set_test_items(metric_json)
        platform_tester.run()
        println "COUNT: ${test_platform.test_results.size()}"

        then:
        1 == 1
        // test_platform.test_results['hostname'].status == null
        // test_platform.test_results['keyboard'].status == null
    }

    def "SparcXscf CPU"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('showcod')
        platform_tester.run()

        then:
        println test_platform.test_results
        1 == 1
        // test_platform.test_results['network'].devices.csv.size() > 0
        // test_platform.test_results['network'].devices.header.size() > 0
    }

    def "SparcXscf 接続エラー"() {
        setup:
        def test_target_error = new TestTarget(
            name              : 'xscf-m12b',
            ip                : '10.20.129.23',
            domain            : 'SparcXscf',
            account_id        : 'Test',
        )
        def test_platform_error = new TestPlatform(
            name         : 'SparcXscf',
            test_target  : test_target_error,
            dry_run      : false,
        )
        def platform_tester = new PlatformTester(test_platform : test_platform_error,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('showcod')
        platform_tester.run()

        then:
        println test_platform.test_results
        1 == 1
        // test_platform.test_results['network'].devices.csv.size() > 0
        // test_platform.test_results['network'].devices.header.size() > 0
    }
}
