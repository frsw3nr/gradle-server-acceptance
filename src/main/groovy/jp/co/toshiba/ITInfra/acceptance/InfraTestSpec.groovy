package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovy.transform.ToString

// モードの設定
// モードA : loop_exec_parse
// モードB : loop_exec_loop_parse
// サブルーチン
// setup_command
// cleanup_command
// setup_parse
// cleanup_parse
// XXX

public enum EnumTestMode {
    loop_exec_parse,
    loop_exec_loop_parse
}

@Slf4j
class InfraTestSpec {

    def config
    TargetServer test_server
    String title
    String domain
    Boolean dry_run
    int timeout
    Boolean skip_exec
    def mode = EnumTestMode.loop_exec_parse

    def InfraTestSpec(TargetServer test_server, String domain) {
        this.test_server = test_server
        this.domain      = domain
        this.title       = domain + '(' + test_server.info() + ')'
        this.dry_run     = test_server.dry_run
        this.timeout     = test_server.timeout
    }

    def init() {
        log.info("Initialize infra test spec ${title}")
    }

    def finish() {
        log.info("Finish infra test spec ${title}")
    }

    def setup_exec() {
        log.info("Setup infra test exec ${title}")
    }

    def cleanup_exec() {
        log.info("Cleanup infra test exec ${title}")
    }

    def setup_parse() {
        log.info("Setup infra test parse ${title}")
    }

    def cleanup_parse() {
        log.info("Cleanup infra test parse ${title}")
    }
}
