import spock.lang.Specification
import static groovy.json.JsonOutput.*
import groovy.json.*
import org.apache.commons.io.FileUtils
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

// gradle --daemon test --tests "vCenterBaseTest.vCenter テスト仕様のロード"

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
            os_account_id     : 'Test',
            remote_account_id : 'Test',
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
        println test_platform.test_results
        // 1 == 1
        // println test_platform.test_results['os_caption']
        // println test_platform.test_results['os_architecture']
        test_platform.test_results['NumCpu'].status  == ResultStatus.OK
        test_platform.test_results['NumCpu'].verify == ResultStatus.OK
        test_platform.test_results['VMHost'].status  == ResultStatus.OK
        test_platform.test_results['VMHost'].verify == ResultStatus.OK
     }

}
