package jp.co.toshiba.ITInfra.acceptance.Model
import groovy.util.logging.Slf4j
import groovy.transform.ToString
import jp.co.toshiba.ITInfra.acceptance.Document.*

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
