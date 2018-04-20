package jp.co.toshiba.ITInfra.acceptance.Document

import jp.co.toshiba.ITInfra.acceptance.*
import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*

@XlsSheet(regex="CheckSheet")
class ResultSheet {
    @XlsSheetName
    String sheetName

    @XlsHorizontalRecords(headerColumn=0,
                          headerRow=3,
                          recordClass=ResultSheetLine.class)

    List<ResultSheetLine> test_items

    // def clone_sheet() {
    //     // シートのクローン
    //     Workbook workbook = WorkbookFactory.create(new FileInputStream("template.xlsx"));
    //     Sheet templateSheet = workbook.getSheet("XlsSheet(regexp)");
    //     for(SampleSheet sheetObj : sheets) {
    //         int sheetIndex = workbook.getSheetIndex(templateSheet);
    //         Sheet cloneSheet = workbook.cloneSheet(sheetIndex);
    //         workbook.setSheetName(workbook.getSheetIndex(cloneSheet), sheetObj.sheetName);
    //     }
    // }

}

class ResultSheetLine {
    @XlsColumn(columnName="Test")
    String enabled
    @XlsColumn(columnName="ID")
    String id
    @XlsColumn(columnName="項目")
    String name
    @XlsColumn(columnName="分類")
    String domain
    @XlsColumn(columnName="デバイス")
    String device_enabled
    @XlsColumn(columnName="採取情報")
    String description
}
