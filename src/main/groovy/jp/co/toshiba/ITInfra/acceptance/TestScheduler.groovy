package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import jsr166y.ForkJoinPool

@Slf4j
class TestScheduler {

    final created
    def owner
    EvidenceSheet evidence
    TargetServer[] test_servers

    TestScheduler() {
        created = new Date().format("yyyyMMdd-HHmmss")
    }

    Boolean runTest() {
        println "START"
        GParsPool.withPool(3) { ForkJoinPool pool ->
            (1..5).eachParallel {
                sleep(1000)
                println it
            }
        }
    }
}
