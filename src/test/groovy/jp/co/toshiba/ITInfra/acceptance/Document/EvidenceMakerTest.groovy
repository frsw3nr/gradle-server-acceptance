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

// gradle --daemon test --tests "EvidenceMakerTest.Excel 出力"

class EvidenceMakerTest extends Specification {

    def config_file = 'src/test/resources/config.groovy'
    def excel_file = 'src/test/resources/check_sheet.xlsx'
    def json_dir = 'src/test/resources/json'
    def excel_parser
    def test_scenario
    def evidence_maker

    def setup() {
        excel_parser = new ExcelParser(excel_file)
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)

        def test_result_reader = new TestResultReader(json_dir: json_dir)
        test_result_reader.read_entire_scenario(test_scenario)

        // evidence_maker = new EvidenceMaker(excel_parser: excel_parser)
        evidence_maker = new EvidenceMaker()
    }

    def "実行結果変換"() {
        when:
        test_scenario.accept(evidence_maker)

        then:
        def summary_sheet = evidence_maker.summary_sheets['Linux']
        summary_sheet.rows.size() > 0
        summary_sheet.cols.size() > 0
        summary_sheet.results['vCenter']['NumCpu']['ostrich'] != null
    }

    def "Excel 出力"() {
        when:
        test_scenario.accept(evidence_maker)
        def excel_sheet_maker = new ExcelSheetMaker(
                                    excel_parser: excel_parser,
                                    evidence_maker: evidence_maker)
        excel_sheet_maker.output('build/check_sheet.xlsx')

        then:
        1 == 1
    }
}

