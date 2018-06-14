import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.Document.*

import org.apache.poi.ss.usermodel.Workbook
import com.github.k3286.dto.Invoice
import com.github.k3286.dto.InvoiceDetail
// import com.github.k3286.report.ReportMaker

// gradle --daemon test --tests "EvidenceManagerTest.初期化"

class EvidenceManagerTest extends Specification {
    TestScenario test_scenario
    ConfigTestEnvironment test_env
    // def 

    def setup() {
        String[] args = [
            '--dry-run',
            '-c', './src/test/resources/config.groovy',
            '-excel', 'src/test/resources/check_sheet.xlsx'
        ]

        test_env = ConfigTestEnvironment.instance
        def test_runner = new TestRunner()
        println "TEST_RUNNER1"
        test_runner.parse(args)
        def json = new groovy.json.JsonBuilder()
        json(test_runner)
        println json.toPrettyString()
        test_env.read_from_test_runner(test_runner)
        test_env.read_config('src/test/resources/config.groovy')

    }

    def 初期化() {
        when:
        def evidence_manager = new EvidenceManager()
        test_env.accept(evidence_manager)

        then:
        1 == 1
    }

}
