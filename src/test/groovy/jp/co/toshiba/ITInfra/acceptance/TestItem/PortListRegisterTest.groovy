import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

// gradle --daemon test --tests "PortListRegisterTest.ポートリスト登録"

class PortListRegisterTest extends Specification {

    String config_file = 'src/test/resources/config.groovy'
    TestItem test_item

    def setup() {
        def test_metrics = [
            'network': new TestMetric(name : 'network', enabled : true),
        ]

        test_item = new TestItem(test_id  : 'network')
    }


    def "ポートリスト登録"() {
        when:
        test_item.lookuped_port_list('192.168.10.1')

        then:
        // println test_item.port_lists
        test_item.port_lists['192.168.10.1'].ip == '192.168.10.1'
        test_item.port_lists['192.168.10.1'].lookup == true
    }

    def "管理用ポートリスト登録"() {
        when:
        test_item.admin_port_list('192.168.10.1')

        then:
        // println test_item.port_lists
        test_item.port_lists['192.168.10.1'].ip == '192.168.10.1'
        test_item.port_lists['192.168.10.1'].lookup == true
        test_item.port_lists['192.168.10.1'].managed == true
    }

}
