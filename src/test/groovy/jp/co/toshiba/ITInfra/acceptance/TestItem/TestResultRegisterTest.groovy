import jp.co.toshiba.ITInfra.acceptance.Model.ResultStatus
import jp.co.toshiba.ITInfra.acceptance.Model.TestMetric
import jp.co.toshiba.ITInfra.acceptance.TestItem
import spock.lang.Specification

// gradle --daemon test --tests "TestResultRegisterTest.比較対象除外"

class TestResultRegisterTest extends Specification {

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

    def "エラーメッセージ"() {
        when:
        test_item.results('ostrich')
        test_item.error_msg('ERROR_MESSAGE')

        then:
        test_item.test_results['uname'].error_msg == 'ERROR_MESSAGE'
    }

    def "比較対象除外"() {
        when:
        test_item.results(['uname' : 'ostrich', 'cpu' : '3'])
        test_item.exclude_compare()

        then:
        test_item.test_results['uname'].exclude_compare == true
        test_item.test_results['cpu'].exclude_compare == false
    }

    def "メトリック指定比較対象除外"() {
        when:
        test_item.results(['uname' : 'ostrich', 'cpu' : '3'])
        test_item.exclude_compare('cpu')

        then:
        test_item.test_results['uname'].exclude_compare == false
        test_item.test_results['cpu'].exclude_compare == true
    }

    def "複数メトリック指定比較対象除外"() {
        when:
        test_item.results(['uname' : 'ostrich', 'cpu' : '3'])
        test_item.exclude_compare(['uname', 'cpu'])

        then:
        test_item.test_results['uname'].exclude_compare == true
        test_item.test_results['cpu'].exclude_compare == true
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
        test_item.test_results['uname'].status == ResultStatus.WARNING
    }

    def "空の複数結果登録"() {
        when:
        test_item.results(['uname' : '[]', 'cpu' : '', 'lsb' : null])

        then:
        test_item.test_results['uname'].status == ResultStatus.WARNING
        test_item.test_results['cpu'].status == ResultStatus.WARNING
        test_item.test_results['lsb'].status == ResultStatus.WARNING
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

    def "検証結果サマリ"() {
        when:
        test_item.test_id = 'os'
        test_item.verify(['cpu' : true, 'lsb' : true])

        then:
        test_item.test_results['os'].verify == ResultStatus.OK
    }

    def "検証結果サマリ NG"() {
        when:
        test_item.test_id = 'os'
        test_item.results(['uname' : 'ostrich', 'cpu' : '3'])
        test_item.verify(['cpu' : false, 'lsb' : true])

        then:
        test_item.test_results['os'].verify == ResultStatus.NG

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

    def "サマリ登録"() {
        when:
        test_item.test_id = 'os'
        test_item.results(['uname' : 'ostrich', 'cpu' : '3'])
        test_item.make_summary_text('uname' : 'HOSTNAME', 'cpu' : '#CPU')

        then:
        println(test_item.test_results['os'])
        test_item.test_results['os'].value == '[HOSTNAME:ostrich, #CPU:3]'
        test_item.test_results['cpu'].value == '3'
    }

}
