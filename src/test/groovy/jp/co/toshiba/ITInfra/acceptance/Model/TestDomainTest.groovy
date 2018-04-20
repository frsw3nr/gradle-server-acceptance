import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*

// gradle --daemon clean test --tests "TestDomainTest.初期化"

class TestDomainTest extends Specification {

    def "初期化"() {
        when:
        TestDomain domain1 = new TestDomain(['name': 'test1'])
        println("domain1: $domain1")

        // TestDomain domain2 = new TestDomain(['name': 'test1', 'desc': '記述'])
        // println("domain2: $domain2")

        then:
        1 == 1
        // a.name == 'test' 
    }
}
