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
        CheckSheet sheet = new CheckSheet();
        
        List<CheckSheetLine> test_items = new ArrayList<>();
        test_items.add(new CheckSheetLine(id: 'ID1', name: 'NAME1', description:'明日は木曜'))
        test_items.add(new CheckSheetLine(id: 'ID2', name: 'NAME2', description:'明日は金曜'))
        sheet.test_items = test_items;
        
        XlsMapper xlsMapper = new XlsMapper();
        xlsMapper.save(
            new FileInputStream("src/test/resources/check_sheet.xlsx"),
            new FileOutputStream(new File('build', "sample_out.xlsx")),
            sheet
            );

        then:
        1 == 1
    }
}
