import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*

// gradle --daemon clean test --tests "TestMetricTest.初期化"

class TestMetricTest extends Specification {

    def "初期化"() {
        setup:
        def param1 = ['name': 'Linux', "enabled": false]

        when:
        TestPlatform linux = new TestPlatform(name: 'Linux', enabled: false)
        def test_metrics = [:]
        (1..3).each { idx ->
            def id = "test_${idx}"
            test_metrics[id] = new TestMetric(name: id)
        }
        linux.test_metrics = test_metrics
        println("$linux")

        then:
        1 == 1
    }

}
