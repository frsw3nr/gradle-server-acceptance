import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import java.nio.charset.Charset
import org.apache.commons.io.FileUtils

// gradle --daemon clean test --tests "ResultContainerTest"

class ResultContainerTest extends Specification {

    def node_dir = 'src/test/resources/node'
    EvidenceManager evidence_manager

    def setup() {
        def params = [
            getconfig_home: '.',
            project_home: 'src/test/resources',
            db_config: 'src/test/resources/cmdb.groovy'
        ]
        this.evidence_manager = new EvidenceManager(params)
    }

    def "ResultContainer execption"() {
        when:
        new ResultContainer()

        then:
        thrown RuntimeException
    }

    def "JSON読み込み"() {
        setup:
        def server = 'ostrich'

        when:
        ResultContainer.instance.test_results   = new ConfigObject()
        ResultContainer.instance.device_results = new ConfigObject()
        ResultContainer.instance.loadNodeConfigJSON(this.evidence_manager, server)

        then:
        def test_results = ResultContainer.instance.test_results
        test_results[server]['Linux']['NumCpu'] == '1'
        test_results[server]['Zabbix']['Host']  == 'ostrich'

        def device_results = ResultContainer.instance.device_results
        device_results[server]['vCenter']['vm_storage']['row1']['CapacityGB'] == '30'
        device_results[server]['Linux']['packages']['row1'].with {
            name == 'perl-Log-Message-Simple'
            arch == 'x86_64'
        }
    }

    def "CMDB読み込み"() {
        setup:
        def server = 'ostrich'
        def cmdb_model = CMDBModel.instance
        cmdb_model.initialize(this.evidence_manager)

        when:
        cmdb_model.export(new File('src/test/resources/node/').getAbsolutePath())
        ResultContainer.instance.getCMDBNodeConfig(evidence_manager, server)

        then:
        def test_results = ResultContainer.instance.test_results
        test_results[server]['Linux']['NumCpu'] == '1'
        test_results[server]['Zabbix']['Host']  == 'ostrich'

        def device_results = ResultContainer.instance.device_results
        device_results[server]['vCenter']['vm_storage']['row1']['CapacityGB'] == '30'
        device_results[server]['Linux']['packages']['row1'].with {
            name == 'perl-Log-Message-Simple'
            arch == 'x86_64'
        }
    }

    def "実績登録"() {
        setup:
        TargetServer test_server = new TargetServer(
            server_name       : 'ostrich',
            ip                : 'localhost',
            platform          : 'Linux',
            os_account_id     : 'Test',
            remote_account_id : 'Test',
            remote_alias      : 'ostrich',
            verify_id         : 'RuleDB',
        )
        test_server.setAccounts('src/test/resources/config.groovy')
        test_server.dry_run = true
        def test = new DomainTestRunner(test_server, 'Linux')
        test.makeTest(['hostname', 'filesystem'])

        when:
        ResultContainer.instance.setNodeConfig('ostrich', 'Linux',
                                               test.result_test_items)

        then:
        def test_results = ResultContainer.instance.test_results
        test_results['ostrich']['Linux']['hostname'] == 'ostrich'
        test_results['ostrich']['Linux']['filesystem']  == '[/boot:500M, /:26.5G, [SWAP]:3G]'

        def device_results = ResultContainer.instance.device_results
        device_results['ostrich']['Linux']['filesystem']['row1']['name'] == 'sr0'
        device_results['ostrich']['Linux']['filesystem']['row2']['name'] == 'sda'
    }

    // def "検査結果の比較"() {
    //     when:

    //     then:
    //     1 == 1
    // }

    // def "検査結果デバイスの比較"() {
    //     when:

    //     then:
    //     1 == 1
    // }
}
