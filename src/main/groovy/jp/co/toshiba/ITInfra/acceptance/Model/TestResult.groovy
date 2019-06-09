package jp.co.toshiba.ITInfra.acceptance.Model

import groovy.transform.ToString
import groovy.util.logging.Slf4j

enum ResultStatus {
  OK, NG, WARNING, MATCH, UNMATCH, UNKOWN
}

@Slf4j
@ToString(includePackage = false)
class TestResult extends SpecModel {
    String name
    def value
    String error_msg
    String compare_server
    ResultStatus status
    ResultStatus verify
    ResultStatus comparision
    TestResultLine devices

    public Map asMap() {
        def map = [name : name, value : value, error_msg : error_msg,
                   compare_server : compare_server, status : status,
                   verify : verify, comparision : comparision]
        map << this.custom_fields

        if (this.devices) {
            map['devices'] = this.devices.asMap()
        }

        return map
    }

}
