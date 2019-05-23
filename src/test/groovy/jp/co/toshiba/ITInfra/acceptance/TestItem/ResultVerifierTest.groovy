import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

// gradle --daemon test --tests "ResultVerifierTest.数値検証小なり"

class ResultVerifierTest extends Specification {

    String config_file = 'src/test/resources/config.groovy'
    TestItem test_item
    TestTarget test_target

    def setup() {
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

    def "数値検証"() {
        setup:
        def test = new TestItem(test_id : 'cpu', verify_test: true)
        test.server_info = ['cpu' : parameterA]
        test.verify_number_equal('cpu', parameterB)
        println test.test_results

        expect:
        test?.test_results['cpu']?.verify == expectedValue

        where:
        expectedValue   || parameterA | parameterB
        ResultStatus.OK || '4'        | '4'
        ResultStatus.NG || '4'        | '2'
        null            || '4'        | null
        null            || null       | '4'
    }

    def "数値検証小なり"() {
        setup:
        def test = new TestItem(test_id : 'cpu', verify_test: true)
        test.server_info = ['cpu' : parameterA]
        test.verify_number_lower('cpu', parameterB)
        println test.test_results

        expect:
        test?.test_results['cpu']?.verify == expectedValue

        where:
        expectedValue   || parameterA | parameterB
        ResultStatus.OK || '4'        | '4'
        ResultStatus.OK || '4'        | '2'
        ResultStatus.NG || '4'        | '6'
        null            || '4'        | 'Hoge'
        null            || null       | '4'
    }

    def "数値検証大なり"() {
        setup:
        def test = new TestItem(test_id : 'cpu', verify_test: true)
        test.server_info = ['cpu' : parameterA]
        test.verify_number_higher('cpu', parameterB)
        println test.test_results

        expect:
        test?.test_results['cpu']?.verify == expectedValue

        where:
        expectedValue   || parameterA | parameterB
        ResultStatus.OK || '4'        | '4'
        ResultStatus.OK || '4'        | '6'
        ResultStatus.NG || '4'        | '2'
        null            || '4'        | 'Hoge'
        null            || null       | '4'
    }

    def "マップテキストの検証"() {
        setup:
        def test = new TestItem(test_id : 'test_map', verify_test: true)
        test.server_info = ['test_map' : parameterA]
        test.verify_text_search_map('test_map', parameterB)

        expect:
        test?.test_results['test_map']?.verify == expectedValue

        where:
        expectedValue   || parameterA                         | parameterB
        ResultStatus.OK || ['key1':'value1', 'key2':'value2'] | ['key1':'value1', 'key2':'value2']
        ResultStatus.NG || ['key1':'value1', 'key2':'value2'] | ['key1':'hoge', 'key3':'value2']
        ResultStatus.NG || ['key1':'value1', 'key2':'value2'] | ['key3':'value2']
    }

    def "マップ値の検証"() {
        setup:
        def test = new TestItem(test_id : 'test_map', verify_test: true)
        test.server_info = ['test_map' : parameterA]
        test.verify_number_equal_map('test_map', parameterB)

        expect:
        test?.test_results['test_map']?.verify == expectedValue

        where:
        expectedValue   || parameterA               | parameterB
        ResultStatus.OK || ['key1':'4', 'key2':'2'] | ['key1':'4', 'key2':'2']
        ResultStatus.NG || ['key1':'4', 'key2':'2'] | ['key1':'4', 'key2':'3']
        ResultStatus.NG || ['key1':'4', 'key2':'2'] | ['key3':'4']
    }

    def "リストの検証"() {
        setup:
        def test = new TestItem(test_id : 'test_list', verify_test: true)
        test.server_info = ['test_list' : parameterA]
        test.verify_text_search_list('test_list', parameterB)

        expect:
        test?.test_results['test_list']?.verify == expectedValue

        where:
        expectedValue   || parameterA            | parameterB
        ResultStatus.OK || ["a":1, "b":1, "c":1] | ["a", "b", "c"]
        ResultStatus.OK || ["a":1, "b":1, "c":1] | ["a", "b", "c", "d"]
        ResultStatus.NG || ["a":1, "b":1, "c":1] | ["a", "b"]
        null || ["a", "b", "c"] | ["a":1, "b":1]
    }


}
