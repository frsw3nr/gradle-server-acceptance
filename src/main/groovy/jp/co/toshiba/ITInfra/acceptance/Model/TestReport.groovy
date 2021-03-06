package jp.co.toshiba.ITInfra.acceptance.Model

import groovy.transform.ToString
import groovy.util.logging.Slf4j

@Slf4j
@ToString(includePackage = false)
class TestReport extends SpecModel {
    String name
    String metric_type
    String default_name
    RedmineTicketField redmine_ticket_field
    Map platform_metrics

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
