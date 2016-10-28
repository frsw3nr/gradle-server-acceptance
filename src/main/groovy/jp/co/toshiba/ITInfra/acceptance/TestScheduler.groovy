package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import jsr166y.ForkJoinPool

public enum SpecTestMode {
    serial,
    parallel,
}

@Slf4j
class TestScheduler {

    TestRunner test_runner
    EvidenceSheet evidence_sheet
    TargetServer[] test_servers
    DeviceResultSheet device_results
    def serialization_domains = [:]
    def test_evidences = [:].withDefault{[:].withDefault{[:]}}

    TestScheduler(TestRunner test_runner) {
        this.test_runner = test_runner
        this.device_results = new DeviceResultSheet()
        setSerializationDomainTasks()
    }

    def setSerializationDomainTasks() {
        def config = Config.instance.read(test_runner.config_file)
        def domains = config['test']['serialization']['tasks']
        if (domains) {
            assert domains in List
            domains.each { domain ->
                this.serialization_domains[domain] = 1
            }
        }
    }

    List filterServer(List servers) {
        def filtered_servers = []
        def target_servers = test_runner.target_servers
        servers.each { server ->
            if (!target_servers || target_servers.containsKey(server.server_name)) {
                filtered_servers << server
            }
        }
        return filtered_servers
    }

    def filterSpecs(List test_ids) {
        def filtered_test_ids = []
        def target_test_ids = test_runner.test_ids
        test_ids.each { test_id ->
            if (!target_test_ids || target_test_ids.containsKey(test_id)) {
                filtered_test_ids << test_id
            }
        }
        return filtered_test_ids
    }

    def runServerTest(TargetServer test_server, SpecTestMode mode) {
        test_server.with {
            long start = System.currentTimeMillis()
            setAccounts(test_runner.config_file)
            it.dry_run = test_runner.dry_run

            def domain_specs = evidence_sheet.domain_test_ids[platform]
            domain_specs.each { domain, test_ids ->
                def is_serial = serialization_domains.containsKey(domain)
                if ((mode == SpecTestMode.serial   && is_serial) ||
                    (mode == SpecTestMode.parallel && !is_serial)) {
                    log.info "${mode} ON  : ${domain} ${server_name}"
                    def filtered_test_ids = filterSpecs(test_ids)
                    def domain_test = new DomainTestRunner(it, domain)
                    domain_test.with {
                        makeTest(filtered_test_ids)
                        if (test_runner.verify_test) {
                            verify()
                        }
                        log.debug "Set Device results '${domain},${server_name}'"
                        device_results.setResults(domain, server_name, result_test_items)

                        test_evidences[platform][server_name][domain] = [
                            'test' : getResults(),
                            'verify' : getVerifyStatuses(),
                        ]
                    }
                    log.info "${mode} OFF : ${domain} ${server_name}"
                }
            }
            long elapsed = System.currentTimeMillis() - start
            log.info "Finish infra test '${server_name}', Elapsed : ${elapsed} ms"
        }
    }

    def runTest() {
        log.info "Initialize test schedule"
        long run_test_start = System.currentTimeMillis()
        evidence_sheet = new EvidenceSheet(test_runner.config_file)
        evidence_sheet.evidence_source = test_runner.sheet_file
        evidence_sheet.readSheet()
        evidence_sheet.prepareTestStage()
        test_servers = filterServer(evidence_sheet.test_servers)
        def verifier = VerifyRuleGenerator.instance
        verifier.setVerifyRule(evidence_sheet.verify_rules)

println "[test_platforms]"
println evidence_sheet.test_platforms
println "[test_domains]"
println evidence_sheet.test_domains
println "[domain_test_ids]"
println evidence_sheet.domain_test_ids

        if (serialization_domains) {
            test_servers.each { test_server ->
                runServerTest(test_server, SpecTestMode.serial)
            }
        }

        GParsPool.withPool(test_runner.parallel_degree) { ForkJoinPool pool ->
            test_servers.eachParallel { test_server ->
                runServerTest(test_server, SpecTestMode.parallel)
            }
        }
        log.debug "Evidence : " + test_evidences
        test_evidences.each { platform, platform_evidence ->
            def server_index = 0
            platform_evidence.each { server_name, server_evidence ->
                evidence_sheet.updateTestResult(platform, server_name, server_index, server_evidence)
                server_index ++
            }
        }
        evidence_sheet.device_test_ids.each { domain,test_ids->
            test_ids.each { test_id, flag->
                def header = device_results.getHeaders(domain, test_id)
                def csv = device_results.getCSVs(domain, test_id)
                if (header && csv) {
                    evidence_sheet.insertDeviceSheet(domain, test_id, header, csv)
                }
            }
        }
        long run_test_elapsed = System.currentTimeMillis() - run_test_start
        log.info "Finish server acceptance test, Total Elapsed : ${run_test_elapsed} ms"
    }
}
