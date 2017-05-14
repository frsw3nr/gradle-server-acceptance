import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVFormat

// gradle --daemon clean test --tests "CSVExporterTest"

class CSVExporterTest extends Specification {
    def excel_source = './src/test/resources/check_sheet_20170512_143424.xlsx'

    def "検査結果の読み込み"() {
        setup:
        def test = new TestRunner()
        def xls_files = [excel_source]

        when:
        String[] args = []
        test.parse(args)
        def exporter = new CSVExporter(test)
        def csv = exporter.readTestResultSheet(xls_files)

        then:
        csv.size() > 0
    }

    def "検査結果のエクスポート"() {
        setup:
        def test = new TestRunner()
        String[] args = []
        test.parse(args)
        test.export_files = excel_source

        when:
        new CSVExporter(test).run()

        then:
        1 == 1
    }

    def "OpenCSVライター"() {
        when:
        CSVPrinter printer = new CSVPrinter(
            new PrintWriter(System.out),
            CSVFormat.EXCEL
        )

        def answer = [ [5,6,"asdf",7],[9,10,"a\nb","z,x",12]]

        answer.each {
          printer.printRecord(it)
        }

        printer.close()

        then:
        1 == 1
    }
}
