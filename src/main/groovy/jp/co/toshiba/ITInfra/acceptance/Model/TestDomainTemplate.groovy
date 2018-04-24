package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

@Slf4j
@ToString
class TestDomainTemplate extends SpecModel {
    String name
    TestMetric test_metrics = [:]

    def accept(visitor){
        visitor.visit_check_sheet(this)
    }
}
