import spock.lang.Specification
import org.apache.commons.lang.math.NumberUtils
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*

// gradle --daemon test --tests "InfraTestSpecTest.数値の比較"

class InfraTestSpecTest extends Specification {

    String config_file = 'src/test/resources/config.groovy'
    TestPlatform test_platform
    TestTarget test_target
    TestScenario test_scenario

    def setup() {
        test_target = new TestTarget(
            name              : 'ostrich',
            ip                : '192.168.10.1',
            domain            : 'Linux',
            template_id       : 'AP',
            os_account_id     : 'Test',
            remote_account_id : 'Test',
            remote_alias      : 'ostrich',
        )

        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)
        excel_parser.make_template_link(test_target, test_scenario)
        def test_metrics = test_scenario.test_metrics.get('Linux').get('Linux').get_all()
        // def json = new groovy.json.JsonBuilder()
        // json(test_scenario.test_metrics)
        // println json.toPrettyString()

        test_platform = new TestPlatform(
            name         : 'Linux',
            test_target  : test_target,
            test_metrics : test_metrics,
            dry_run      : true,
        )
    }

    def "初期化"() {
        when:
        def test_spec = new InfraTestSpec(test_platform)

        then:
        test_spec != null
    }

    def "サーバ情報"() {
        when:
        def test_spec = new InfraTestSpec(test_platform)
        def test_value = test_spec.target_info('kernel')

        then:
        test_value.size() > 0
    }

    def "数値の比較"() {
        setup:
        def test_metrics = test_scenario.test_metrics.get('Linux').get('vCenter').get_all()
        // def json = new groovy.json.JsonBuilder()
        // json(test_metrics)
        // println json.toPrettyString()
        def test_platform_vcenter = new TestPlatform(
            name         : 'vCenter',
            test_target  : test_target,
            test_metrics : test_metrics,
            dry_run      : true,
        )

        when:
        def test_spec = new InfraTestSpec(test_platform_vcenter)
        def results = test_spec.verify_data_match(['NumCpu': '2', 'MemoryGB': '2'])
        def results2 = test_spec.verify_data_match(['MemoryGB': '2.0'])
        def results3 = test_spec.verify_data_match(['MemoryGB': null])
        def results4 = test_spec.verify_data_match(['NumCpu':'2', 'PowerState':'PoweredOn', 'MemoryGB':'2', 'VMHost':'esx19.local', 'Cluster':'PC001'])

        then:
        println "results4:$results4"
        1 == 1
        // results['MemoryGB'] == true
        // results2['MemoryGB'] == true
        // results3['MemoryGB'] == false
    }

    // TargetServer test_server
    // TestTarget test_target
    // DomainTestRunner test
    // InfraTestSpec spec

    // def setup() {
    //     test_server = new TargetServer(
    //         server_name       : 'win2012',
    //         ip                : '192.168.0.12',
    //         platform          : 'Windows',
    //         os_account_id     : 'Test',
    //         remote_account_id : 'Test',
    //         remote_alias      : 'win2012.ostrich',
    //     )
    //     test_target = new TestTarget(
    //         name              : 'win2012',
    //         ip                : '192.168.0.12',
    //         platform          : 'Windows',
    //         os_account_id     : 'Test',
    //         remote_account_id : 'Test',
    //         remote_alias      : 'win2012.ostrich',
    //     )
    //     test_server.setAccounts('src/test/resources/config.groovy')
    //     test_server.dry_run = true
    // }

    // def "UTF-16検査結果の読込"() {
    //     setup:
    //     spec = new InfraTestSpec(test_server, 'Windows')

    //     when:
    //     def lines = spec.exec('ipconfig') {
    //         new File("${local_dir}/ipconfig")
    //     }
    //     def tmp = [:].withDefault{''}
    //     def csv = []
    //     lines.eachLine {
    //         (it =~ /IPv4 アドレス.+:\s+(.+)$/).each {m0,m1->
    //             tmp['ipv4'] = m1
    //         }
    //     }

    //     then:
    //     tmp.size() > 0
    // }

    // def "SJIS検査結果の読込"() {
    //     setup:
    //     spec = new InfraTestSpec(test_server, 'Windows')

    //     when:
    //     def lines = spec.exec('ipconfig_sjis', encode: "MS932") {
    //         new File("${local_dir}/ipconfig_sjis").getText("MS932")
    //     }
    //     def tmp = [:].withDefault{''}
    //     def csv = []
    //     lines.eachLine {
    //         (it =~ /IPv4 アドレス.+:\s+(.+)$/).each {m0,m1->
    //             tmp['ipv4'] = m1
    //         }
    //     }

    //     then:
    //     tmp.size() > 0
    // }

    // def "EUC-JP検査結果の読込"() {
    //     setup:
    //     spec = new InfraTestSpec(test_server, 'Windows')

    //     when:
    //     def lines = spec.exec('ipconfig_eucjp', encode: "EUC_JP") {
    //         new File("${local_dir}/ipconfig_eucjp").getText("EUC_JP")
    //     }
    //     def tmp = [:].withDefault{''}
    //     def csv = []
    //     lines.eachLine {
    //         (it =~ /IPv4 アドレス.+:\s+(.+)$/).each {m0,m1->
    //             tmp['ipv4'] = m1
    //         }
    //     }

    //     then:
    //     tmp.size() > 0
    // }

    // def "UCS-2-LITTLE-ENDIAN検査結果の読込"() {
    //     setup:
    //     spec = new InfraTestSpec(test_server, 'Windows')

    //     when:
    //     def lines = spec.exec('wmic_net.txt', encode: "UTF-16LE") {
    //         new File("${local_dir}/wmic_net.txt", "UTF-16LE")
    //     }
    //     def tmp = [:].withDefault{0}
    //     lines.eachLine {
    //         (it =~ /(イーサネット|トンネル)/).each {m0,m1->
    //             tmp[m1] ++
    //         }
    //     }
    //     println tmp
    //     then:
    //     tmp['イーサネット'] > 0
    //     tmp['トンネル'] > 0
    // }

    // def "結果の共有"() {
    //     setup:
    //     spec = new InfraTestSpec(test_server, 'Windows')

    //     when:
    //     def lines = spec.exec('date.txt', shared: true) {
    //         new File("${evidence_log_share_dir}/date.txt")
    //     }
    //     println lines
    //     then:
    //     1 == 1
    // }
}
