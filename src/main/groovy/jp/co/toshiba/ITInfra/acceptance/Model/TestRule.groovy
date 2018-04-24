package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

@Slf4j
@ToString
class TestRule extends SpecModel {
    String name
    String definition
    CompareRule compare_rule

    def accept(visitor){
        visitor.visit_test_rule(this)
    }
}
