package jp.co.toshiba.ITInfra.acceptance

import org.apache.commons.io.FileUtils.*
import groovy.transform.ToString
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import static groovy.json.JsonOutput.*

class EvidenceSheet {

    String evidence_source
    String evidence_target
    String sheet_name_server
    String sheet_name_checks = [:]

    def platforms    = [:]
    def domains      = [:]

    TestServer test_servers = [:]
    // テストサーバ、ドメイン、検査項目をキーにした配列
    TestItem   test_specs
       // = [:].withDefault([:]).withDefault([:])

    EvidenceSheet(String config_file = 'config/config.groovy') {
        def config = Config.instance.read(config_file)
        println config
    }

    // エクセル検査結果列のセルフォーマット
    // 検査結果列に対して罫線を追加して、行幅をオートスケールに設定
    private static CellStyle createBorderedStyle(Workbook wb) {
        BorderStyle thin = BorderStyle.THIN;
        short black = IndexedColors.BLACK.getIndex();

        CellStyle style = wb.createCellStyle();
        style.setWrapText(true);
        style.setBorderRight(thin);
        style.setRightBorderColor(black);
        style.setBorderBottom(thin);
        style.setBottomBorderColor(black);
        style.setBorderTop(thin);
        style.setTopBorderColor(black);
        return style;
    }

    def readServerSheet() {
    }

    def readSpecSheet() {
    }

    def updateTestResult(TestItem[] test_specs) {

    }
}
