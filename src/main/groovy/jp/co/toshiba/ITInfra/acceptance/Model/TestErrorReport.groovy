package jp.co.toshiba.ITInfra.acceptance.Model

import groovy.transform.ToString
import groovy.util.logging.Slf4j

@Slf4j
@ToString(includePackage = false)
class TestErrorReport extends SpecModel {
    String name
    int colnum

    def count() { return 1 }
}

@Slf4j
@ToString(includePackage = false)
class TestErrorReportSet extends SpecCompositeModel {
    String name

    def accept(visitor) {
        visitor.visit_test_error_report_set(this)
    }
}
