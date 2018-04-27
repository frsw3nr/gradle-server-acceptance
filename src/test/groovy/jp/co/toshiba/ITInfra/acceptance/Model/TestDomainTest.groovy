import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*

// gradle --daemon clean test --tests "TestDomainTest.ビジターパターン"

class TestDomainTest extends Specification {

    def "初期化"() {
        setup:
        def param1 = ['name': 'Linux', "enabled": false]

        when:
        TestDomain d1 = new TestDomain(param1)
        TestDomain d2 = new TestDomain(name: 'Linux', enabled: false)
        TestDomain d3 = new TestDomain()
        d3.with {name = 'Windows' }

        then:
        [d1.name, d1.enabled]  == ['Linux', false]
        [d2.name, d2.enabled]  == ['Linux', false]
        [d3.name, d3.enabled] == ['Windows', null]
    }

    def "ビジターパターン"() {
        when:
        def test_scenario = new TestScenario(name: 'OS情報採取')
        test_scenario.accept(new ExcelParser('src/test/resources/check_sheet.xlsx'))

        then:
        1 == 1
    }
}
