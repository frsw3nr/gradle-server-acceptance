import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

// gradle --daemon test --tests "TestItemTest"

class TestItemTest extends Specification {

    String config_file = 'src/test/resources/config.groovy'
    TestItem test_item

    def setup() {
        def test_metrics = [
            'uname': new TestMetric(name : 'uname', enabled : true),
            'cpu'  : new TestMetric(name : 'cpu', enabled : true),
        ]

        test_item = new TestItem(test_id  : 'uname')
    }


    def "検査結果登録"() {
        when:
        test_item.results('ostrich')

        then:
        test_item.test_results['uname'].value == 'ostrich'
    }

    def "複数検査結果登録"() {
        when:
        test_item.results(['uname' : 'ostrich', 'cpu' : '3'])

        then:
        test_item.test_results['uname'].value == 'ostrich'
        test_item.test_results['cpu'].value == '3'
    }

    def "空の結果登録"() {
        when:
        test_item.results()

        then:
        test_item.test_results['uname'].status == ResultStatus.NG
    }

    def "空の複数結果登録"() {
        when:
        test_item.results(['uname' : '[]', 'cpu' : '', 'lsb' : null])

        then:
        test_item.test_results['uname'].status == ResultStatus.NG
        test_item.test_results['cpu'].status == ResultStatus.NG
        test_item.test_results['lsb'].status == ResultStatus.NG
    }

    def "結果登録"() {
        when:
        test_item.results(['uname' : 'ostrich', 'cpu' : '3'])
        test_item.status(['cpu' : false, 'lsb' : true])

        then:
        test_item.test_results['cpu'].status == ResultStatus.NG
        test_item.test_results['lsb'].status == ResultStatus.OK
    }

    def "検証結果登録"() {
        when:
        test_item.results(['uname' : 'ostrich', 'cpu' : '3'])
        test_item.verify(['cpu' : false, 'lsb' : true])

        then:
        test_item.test_results['cpu'].verify == ResultStatus.NG
        test_item.test_results['lsb'].verify == ResultStatus.OK
    }

    def "デバイス登録"() {
        setup:
        def csv = [['col1': 1, 'col2': 2, 'col3': 3],
                   ['col1': 4, 'col2': 5, 'col3': 6],
                  ]
        def header = ['col1', 'col2', 'col3']

        when:
        test_item.devices(csv, header)

        then:
        test_item.test_results['uname'].devices != null
    }
}
