package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

@Slf4j
@ToString
class TestScenario extends SpecModel {
    String name
    TestTarget test_targets = [:]
    TestDomainTemplate test_domain_templates = [:]

    def accept(visitor){
        visitor.visit_test_scenario(this)
    }
}
