import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import java.nio.charset.Charset
import org.apache.commons.io.FileUtils

// gradle --daemon clean test --tests "ResultContainerTest"

class ResultContainerTest extends Specification {

    def node_dir = 'src/test/resources/node'

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
        ResultContainer.instance.loadNodeConfigJSON(node_dir, server)

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
        def params = [
            getconfig_home: '.',
            project_home: 'src/test/resources',
            db_config: 'src/test/resources/cmdb.groovy'
        ]
        def evidence_manager = new EvidenceManager(params)
        def cmdb_model = CMDBModel.instance
        cmdb_model.initialize(evidence_manager)

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
