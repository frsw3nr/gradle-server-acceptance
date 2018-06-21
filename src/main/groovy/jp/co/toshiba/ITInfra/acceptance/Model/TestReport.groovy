package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

@Slf4j
@ToString(includePackage = false)
class TestReport extends SpecModel {
    String name
    String description
    String platform
    Boolean enabled

    def count() { return 1 }
}

@Slf4j
@ToString(includePackage = false)
class TestReportSet extends SpecCompositeModel {
    String name

    def accept(visitor) {
        visitor.visit_test_report_set(this)
    }
}
