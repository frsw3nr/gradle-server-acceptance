import jp.co.toshiba.ITInfra.acceptance.Document.ExcelParser
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec
import jp.co.toshiba.ITInfra.acceptance.ConfigTestEnvironment
import jp.co.toshiba.ITInfra.acceptance.Model.TestPlatform
import jp.co.toshiba.ITInfra.acceptance.Model.TestScenario
import jp.co.toshiba.ITInfra.acceptance.Model.TestTarget
import spock.lang.Specification

// gradle --daemon test --tests "InfraTestSpecTest.検査ログ読込み"

class InfraTestSpecTest extends Specification {

    String config_file = 'src/test/resources/config.groovy'
    TestPlatform test_platform
    TestTarget test_target
    TestScenario test_scenario
    ConfigTestEnvironment test_env

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
            project_test_log_dir : 'src/test/resources/log',
        )

        test_env = ConfigTestEnvironment.instance
        test_env.read_config('src/test/resources/config.groovy')
        test_env.accept(test_platform)
    }

    def "初期化"() {
        when:
        def test_spec = new InfraTestSpec(test_platform)

        then:
        test_spec.project_test_log_dir == './src/test/resources/log'
        test_spec.current_test_log_dir == './build/log/ostrich'
        test_spec.local_dir            == './build/log/ostrich/Linux'
    }

    def "検査ログ読込み"() {
        setup:
        def spec = new InfraTestSpec(test_platform)

        when:
        def log_path = spec.get_log_path('hostname', false)
        println log_path

        then:
        new File(log_path).exists() == true
    }

    def "古い検査ログ読込み"() {
        setup:
        test_platform.project_test_log_dir = 'src/test/resources/log2'
        def spec = new InfraTestSpec(test_platform)

        when:
        def log_path = spec.get_log_path('hostname', false)
        // println log_path

        then:
        log_path == 'src/test/resources/log2/Linux/ostrich/Linux/hostname'
    }

    def "古い検査ログ読込み2"() {
        setup:
        test_target = new TestTarget(
            name              : 'ostrich2',
            domain            : 'Linux',
        )
        test_platform = new TestPlatform(
            name         : 'Linux',
            test_target  : test_target,
            dry_run      : true,
            project_test_log_dir : 'src/test/resources/log2',
        )
        def spec = new InfraTestSpec(test_platform)

        when:
        def log_path = spec.get_log_path('hostname', false)

        then:
        log_path == 'src/test/resources/log2/ostrich2/Linux/hostname'
    }

    // def "サーバ情報"() {
    //     when:
    //     def test_spec = new InfraTestSpec(test_platform)
    //     def test_value = test_spec.target_info('kernel')

    //     then:
    //     test_value.size() > 0
    // }

    // def "数値の比較"() {
    //     setup:
    //     def test_metrics = test_scenario.test_metrics.get('Linux').get('vCenter').get_all()
    //     // def json = new groovy.json.JsonBuilder()
    //     // json(test_metrics)
    //     // println json.toPrettyString()
    //     def test_platform_vcenter = new TestPlatform(
    //         name         : 'vCenter',
    //         test_target  : test_target,
    //         test_metrics : test_metrics,
    //         dry_run      : true,
    //     )

    //     when:
    //     def test_spec = new InfraTestSpec(test_platform_vcenter)
    //     def results = test_spec.verify_data_match(['NumCpu': '2', 'MemoryGB': '2'])
    //     def results2 = test_spec.verify_data_match(['MemoryGB': '2.0'])
    //     def results3 = test_spec.verify_data_match(['MemoryGB': null])
    //     def results4 = test_spec.verify_data_match(['NumCpu':'2', 'PowerState':'PoweredOn', 'MemoryGB':'2', 'VMHost':'esx19.local', 'Cluster':'PC001'])

    //     then:
    //     println "results4:$results4"
    //     1 == 1
    //     // results['MemoryGB'] == true
    //     // results2['MemoryGB'] == true
    //     // results3['MemoryGB'] == false
    // }

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
    //         new File("${test_log_current_dir}/date.txt")
    //     }
    //     println lines
    //     then:
    //     1 == 1
    // }
}
