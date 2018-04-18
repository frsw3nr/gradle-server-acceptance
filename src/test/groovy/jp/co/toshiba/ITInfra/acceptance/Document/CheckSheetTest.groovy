import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*

// gradle --daemon clean test --tests "CheckSheetTest.パース処理"

class CheckSheetTest extends Specification {

    def "パース処理"() {
        when:
        XlsMapper xlsMapper = new XlsMapper();
        CheckSheet check_sheet = xlsMapper.load(
            new FileInputStream("src/test/resources/check_sheet.xlsx"),
            CheckSheet.class
        );

        for(CheckSheetLine test_item : check_sheet.test_items) {
            println("${test_item.id} ${test_item.name} ${test_item.description}")
        }

        then:
        check_sheet.test_items.size() > 0
    }

    def "複数チェックシートのパース処理"() {
        when:
        XlsMapper xlsMapper = new XlsMapper();
        Object[] check_sheets = xlsMapper.loadMultiple(
            new FileInputStream("src/test/resources/check_sheet.xlsx"),
            CheckSheet.class
        );

        println(check_sheets.size())
        for (CheckSheet check_sheet : check_sheets) {
            println(check_sheet.sheetName)
        }

        then:
        check_sheets.size() > 0
    }

    def "書き込み処理"() {
        when:
        [10,20,40,100].each { row ->
            long start = System.currentTimeMillis()
            ResultSheet sheet = new ResultSheet();
            // sheet.sheetName = '検査結果(Linux)'
            List<ResultSheetLine> test_items = new ArrayList<>();
            (1..row).each { idx ->
                test_items.add(new ResultSheetLine(id: "ID${idx}", name: "NAME${idx}", description:'明日は木曜'))
            }
            sheet.test_items = test_items;
            
            XlsMapper xlsMapper = new XlsMapper();
            xlsMapper.save(
                new FileInputStream("src/test/resources/result_sheet.xlsx"),
                new FileOutputStream(new File('build', "sample_out.xlsx")),
                sheet
                );
            long elapsed = System.currentTimeMillis() - start
            println "Export ${row}, Elapsed : ${elapsed} ms"
        }

        then:
        1 == 1
    }
}
