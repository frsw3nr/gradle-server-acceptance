import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

// gradle --daemon clean test --tests "TestRunnerTest"

class TestRunnerTest extends Specification {

    def "メイン処理"() {
        setup:
        def test = new TestRunner()
        def evidence = new EvidenceSheet('src/test/resources/config.groovy')

        when:
        evidence.readSheet()
        def test_servers = evidence.test_servers

        test_servers.each { test_server ->
            test_server.setAccounts('src/test/resources/config.groovy')
            test_server.dry_run = true

            def platform = test_server.platform
            def server_name = test_server.server_name
            def domain_specs = evidence.domain_test_ids[platform]
            domain_specs.each { domain, test_ids ->
                // println "target:${server_name},domain:${domain}"
                def domain_test = new DomainTestRunner(test_server, domain)
                def results  = domain_test.makeTest(test_ids)
                println results.toString()
            }
        }

        then:
        1 == 1
    }

}
