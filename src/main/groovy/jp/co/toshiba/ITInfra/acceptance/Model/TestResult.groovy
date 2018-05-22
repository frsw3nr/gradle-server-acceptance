package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

enum ResultStatus {
  OK, NG, WARNING, MATCH, UNMATCH, UNKOWN
}

@Slf4j
@ToString(includePackage = false)
class TestResult extends SpecModel {
    String name
    def value
    String error_msg
    ResultStatus status
    ResultStatus verify
    TestResultLine devices
}
