package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

@Slf4j
@ToString
class TestTarget extends SpecModel {
    String name
    String platform
    String ip
    String os_account_id
    TestDomain test_domains = [:]
    TestRule test_rules = [:]

    def accept(visitor){
        visitor.visit_test_target(this)
    }
}
