import spock.lang.Specification
import static groovy.json.JsonOutput.*
import groovy.json.*
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import jp.co.toshiba.ITInfra.acceptance.Model.*

// gradle --daemon test --tests "EvidenceMakerTest.JSON 実行結果ファイル読み込み"

class EvidenceMakerTest extends Specification {

    def config_file = 'src/test/resources/config.groovy'
    def excel_parser
    def test_scenario
    def evidence_maker

    def setup() {
        excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)

        def test_result_reader = new TestResultReader(json_dir: 'src/test/resources/json')
        test_result_reader.read_entire_scenario(test_scenario)

        evidence_maker = new EvidenceMaker(excel_parser: excel_parser)
        def test_env = ConfigTestEnvironment.instance
        test_env.read_config(config_file)
        // test_env.set_env('json_dir', 'src/test/resources/json')
        // test_env.set_env('evidence_source', 'src/test/resources/check_sheet.xlsx')
        // test_env.set_env('evidence_target', 'build/check_sheet.xlsx')
        test_env.set_evidence_environment(evidence_maker)
    }

    def "実行結果変換"() {
        when:
        // println test_scenario.test_targets.get_all()
        // evidence_maker.command = EvidenceMakerCommand.READ_JSON
        // test_scenario.accept(evidence_maker)
        evidence_maker.command = EvidenceMakerCommand.OUTPUT_EXCEL
        test_scenario.accept(evidence_maker)
        println evidence_maker.summary_sheets['Linux'].rows

        then:
        1 == 1
        // def targets = test_scenario.test_targets.get_all()
        // targets.each { target_name, domain_targets ->
        //     domain_targets.each { domain, test_target ->
        //         test_target.test_platforms.each { platform_name, test_platform ->
        //             println "$target_name, $domain, $platform_name"
        //             test_platform.test_results.size() > 0
        //         }
        //     }
        // }
    }

}

