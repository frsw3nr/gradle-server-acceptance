import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

// gradle --daemon test --tests "ResultVerifierTest.結果の検証"

class ResultVerifierTest extends Specification {

    String config_file = 'src/test/resources/config.groovy'
    TestItem test_item
    TestTarget test_target

    def setup() {
        // test_target = new TestTarget(
        //     name              : 'ostrich',
        //     ip                : '192.168.10.1',
        //     domain            : 'Linux',
        //     os_account_id     : 'Test',
        //     remote_account_id : 'Test',
        //     remote_alias      : 'ostrich',
        //     template_id       : 'Linux',
        // )

        def test_metrics = [
            'uname': new TestMetric(name : 'uname', enabled : true),
            'cpu'  : new TestMetric(name : 'cpu', enabled : true),
        ]

        test_item = new TestItem(test_id  : 'uname')
        test_item.platform = 'Linux'
        test_item.verify_test = true
        test_item.server_info = [
            'name' : 'ostrich001',
        ]
    }


    def "テキストの検証"() {
        when:
        test_item.results('ostrich')
        test_item.verify_text_search('name', 'hoge')

        then:
        1 == 1
        // println test_item.test_results['uname']
        test_item.test_results['uname'].verify != null
        // test_item.test_results['uname'].value == 'ostrich'
    }

    def "数値の検証"() {
        setup:
        def test_item_number = new TestItem(test_id  : 'cpu')
        test_item_number.platform = 'Linux'
        test_item_number.verify_test = true
        test_item_number.server_info = ['cpu' : '4']

        when:
        test_item_number.results('4')
        test_item_number.verify_number_equal('cpu', 4)

        then:
        1 == 1
        println test_item.test_results
        // test_item.test_results['uname'].verify != null
        // test_item.test_results['uname'].value == 'ostrich'
    }

    // def "エラーメッセージ"() {
    //     when:
    //     test_item.results('ostrich')
    //     test_item.error_msg('ERROR_MESSAGE')

    //     then:
    //     test_item.test_results['uname'].error_msg == 'ERROR_MESSAGE'
    // }

    // def "複数検査結果登録"() {
    //     when:
    //     test_item.results(['uname' : 'ostrich', 'cpu' : '3'])

    //     then:
    //     test_item.test_results['uname'].value == 'ostrich'
    //     test_item.test_results['cpu'].value == '3'
    // }

    // def "空の結果登録"() {
    //     when:
    //     test_item.results()

    //     then:
    //     test_item.test_results['uname'].status == ResultStatus.WARNING
    // }

    // def "空の複数結果登録"() {
    //     when:
    //     test_item.results(['uname' : '[]', 'cpu' : '', 'lsb' : null])

    //     then:
    //     test_item.test_results['uname'].status == ResultStatus.WARNING
    //     test_item.test_results['cpu'].status == ResultStatus.WARNING
    //     test_item.test_results['lsb'].status == ResultStatus.WARNING
    // }

    // def "結果登録"() {
    //     when:
    //     test_item.results(['uname' : 'ostrich', 'cpu' : '3'])
    //     test_item.status(['cpu' : false, 'lsb' : true])

    //     then:
    //     test_item.test_results['cpu'].status == ResultStatus.NG
    //     test_item.test_results['lsb'].status == ResultStatus.OK
    // }

    // def "検証結果登録"() {
    //     when:
    //     test_item.results(['uname' : 'ostrich', 'cpu' : '3'])
    //     test_item.verify(['cpu' : false, 'lsb' : true])

    //     then:
    //     test_item.test_results['cpu'].verify == ResultStatus.NG
    //     test_item.test_results['lsb'].verify == ResultStatus.OK
    // }

    // def "検証結果サマリ"() {
    //     when:
    //     test_item.test_id = 'os'
    //     test_item.verify(['cpu' : true, 'lsb' : true])

    //     then:
    //     test_item.test_results['os'].verify == ResultStatus.OK
    // }

    // def "検証結果サマリ NG"() {
    //     when:
    //     test_item.test_id = 'os'
    //     test_item.results(['uname' : 'ostrich', 'cpu' : '3'])
    //     test_item.verify(['cpu' : false, 'lsb' : true])

    //     then:
    //     test_item.test_results['os'].verify == ResultStatus.NG

    // }

    // def "デバイス登録"() {
    //     setup:
    //     def csv = [['col1': 1, 'col2': 2, 'col3': 3],
    //                ['col1': 4, 'col2': 5, 'col3': 6],
    //               ]
    //     def header = ['col1', 'col2', 'col3']

    //     when:
    //     test_item.devices(csv, header)

    //     then:
    //     test_item.test_results['uname'].devices != null
    // }

    // def "サマリ登録"() {
    //     when:
    //     test_item.test_id = 'os'
    //     test_item.results(['uname' : 'ostrich', 'cpu' : '3'])
    //     test_item.make_summary_text('uname' : 'HOSTNAME', 'cpu' : '#CPU')

    //     then:
    //     println(test_item.test_results['os'])
    //     test_item.test_results['os'].value == '[HOSTNAME:ostrich, #CPU:3]'
    //     test_item.test_results['cpu'].value == '3'
    // }

}
