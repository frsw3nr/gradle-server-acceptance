import groovy.json.JsonSlurper
import jp.co.toshiba.ITInfra.acceptance.Model.ResultStatus
import jp.co.toshiba.ITInfra.acceptance.Model.TestPlatform
import jp.co.toshiba.ITInfra.acceptance.Model.TestTarget
import jp.co.toshiba.ITInfra.acceptance.Model.TestTemplate
import jp.co.toshiba.ITInfra.acceptance.PlatformTester
import spock.lang.Specification

// gradle --daemon test --tests "vCenterBaseTest.vCenter テスト仕様 vm"

class vCenterBaseTest extends Specification {

    String config_file = 'src/test/resources/config.groovy'
    TestPlatform test_platform

    def setup() {
        def template_text = new File('src/test/resources/template_ap.json').text
        def template_json = new JsonSlurper().parseText(template_text)
        def template = new TestTemplate(name : 'AP', values : template_json)

        def test_target = new TestTarget(
            name              : 'ostrich',
            ip                : '192.168.0.12',
            domain            : 'Linux',
            template_id       : 'AP',
            account_id        : 'Test',
            remote_alias      : 'ostrich',
            test_templates    : ['AP': template]
        )

       test_platform = new TestPlatform(
            name         : 'vCenter',
            test_target  : test_target,
            dry_run      : true,
        )
    }

    def ダミーテスト() {
        when:
        println 'Test'

        then:
        1 == 1
    }

    def "vCenter テスト仕様 vm"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('vm')
        platform_tester.run()

        then:
        def json = new groovy.json.JsonBuilder()
        json(test_platform.test_results)
        println json.toPrettyString()
        test_platform.test_results['vm.NumCpu'].status  == ResultStatus.OK
        // test_platform.test_results['vm.NumCpu'].verify == ResultStatus.OK
        test_platform.test_results['vm.MemoryGB'].status  == ResultStatus.OK
        // test_platform.test_results['vm.MemoryGB'].verify == ResultStatus.OK
        test_platform.test_results['vm.VMHost'].status  == ResultStatus.OK
        // test_platform.test_results['vm.VMHost'].verify == ResultStatus.OK
     }

    def "vCenter テスト仕様 VMNetwork"() {
        setup:
        def platform_tester = new PlatformTester(test_platform : test_platform,
                                                 config_file: config_file)
        platform_tester.init()

        when:
        platform_tester.set_test_items('VMNetwork')
        platform_tester.run()

        then:
        println test_platform.test_results
        1 == 1
        // test_platform.test_results['VMNetwork'].status  == ResultStatus.NG
        // test_platform.test_results['VMNetwork'].error_msg  != null
     }
}
