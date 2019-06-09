package jp.co.toshiba.ITInfra.acceptance.Model

import groovy.transform.ToString
import groovy.util.logging.Slf4j

@Slf4j
@ToString(includePackage = false)
class TestResultLine extends SpecModel {
    List csv
    List header

    public Map asMap() {
        def map = [csv : csv, header : header]
        map << this.custom_fields
        return map
    }
}
