package jp.co.toshiba.ITInfra.acceptance.Model

import groovy.transform.ToString
import groovy.util.logging.Slf4j

@Slf4j
@ToString(includePackage = false)
class RedmineTicketField extends SpecModel {
    String tracker
    String field_name

    def count() { return 1 }
}

