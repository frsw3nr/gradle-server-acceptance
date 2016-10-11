import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

// gradle --daemon clean test --tests "TestItemTest"

class TestItemTest extends Specification {

    def "検査項目の初期化"() {
        when:
        def test_item = new TestItem('cpu')

        then:
        test_item.test_id == 'cpu'
    }

    def "検査結果の登録"() {
        setup:
        def test_item = new TestItem('cpu')

        when:
        test_item.results('1')

        then:
        test_item.results['cpu'] == '1'
    }

    def "検査結果配列の登録"() {
        setup:
        def test_item = new TestItem('cpu')

        when:
        test_item.results(['cpu': '4', 'core' : '2'])

        then:
        test_item.results['cpu']  == '4'
        test_item.results['core'] == '2'
    }
}
