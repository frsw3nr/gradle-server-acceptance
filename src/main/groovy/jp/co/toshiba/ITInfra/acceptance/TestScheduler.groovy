package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import jsr166y.ForkJoinPool

@Slf4j
class TestScheduler {

    TestRunner test_runner
    EvidenceSheet evidence_sheet
    TargetServer[] test_servers

    TestScheduler(TestRunner test_runner) {
        this.test_runner = test_runner
    }

    Boolean runTest() {
        log.info "Initialize test schedule"
        def evidence_sheet = new EvidenceSheet(test_runner.config_file)
        evidence_sheet.evidence_source = test_runner.sheet_file
        evidence_sheet.readSheet()
        def test_servers = evidence_sheet.test_servers
        def verify_rule = new VerifyRuleGenerator(evidence_sheet.verify_rules)
        def test_evidences = [:].withDefault{[:].withDefault{[:]}}
        GParsPool.withPool(test_runner.parallel_degree) { ForkJoinPool pool ->
            test_servers.eachParallel { test_server ->
                test_server.setAccounts(test_runner.config_file)
                test_server.dry_run = test_runner.dry_run

                def platform    = test_server.platform
                def server_name = test_server.server_name
                def verify_id   = test_server.verify_id
                def domain_specs = evidence_sheet.domain_test_ids[platform]
                domain_specs.each { domain, test_ids ->
                    def domain_test = new DomainTestRunner(test_server, domain)
                    def test_results = domain_test.makeTest(test_ids)
                    def verify_results = [:]
                    if (test_runner.verify_test) {
                        verify_results = domain_test.verifyResults(verify_rule)
                    }
                    test_evidences[platform][server_name][domain] =
                        ['test' : test_results, 'verify' : verify_results]
                    log.info 'RESULT(Test) :' + test_results.toString()
                    log.info 'RESULT(Verify) :' + verify_results.toString()
                }
            }
        }
        log.info "Evidence : " + test_evidences
        // println "START"
        // GParsPool.withPool(3) { ForkJoinPool pool ->
        //     (1..5).eachParallel {
        //         sleep(1000)
        //         println it
        //     }
        // }
    }
}
