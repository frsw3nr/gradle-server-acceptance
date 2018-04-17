import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Document.*
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*

// gradle --daemon clean test --tests "TestTargetSheetTest.パース処理"

class TestTargetSheetTest extends Specification {

    def "パース処理"() {
        when:
        XlsMapper xlsMapper = new XlsMapper();
        TestTargetSheet test_target_sheet = xlsMapper.load(
            new FileInputStream("src/test/resources/check_sheet.xlsx"),
            TestTargetSheet.class
        );

        def row = 0
        for(TestTargetSheetLine line : test_target_sheet.lines) {
            row ++
            if (row == 1 || line.is_empty())
                continue
            println("${line.platform} ${line.server_name}")
            println("${line}")
        }

        then:
        test_target_sheet.lines.size() > 0
    }

}
