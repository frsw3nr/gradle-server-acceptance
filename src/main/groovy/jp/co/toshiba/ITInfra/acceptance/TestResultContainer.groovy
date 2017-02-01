package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import groovy.transform.ToString
import java.nio.charset.Charset
import java.io.Console
import static groovy.json.JsonOutput.*
import groovy.json.*

@Slf4j
@Singleton
class TestResultContainer {
    def configs = [:]
    def servers = [:].withDefault{[:]}
    def devices = [:].withDefault{[:].withDefault{[:]}}

    def readResultByServer(String server_name, String compare_source = 'local') {
        log.info "Read result : ${server_name}"
    }
}
