package jp.co.toshiba.ITInfra.acceptance.Document

import jp.co.toshiba.ITInfra.acceptance.*
import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*

@XlsSheet(regex="CheckSheet.+")
class CheckSheet {
    @XlsSheetName
    String sheetName

    @XlsHorizontalRecords(headerColumn=0,
                          headerRow=3,
                          recordClass=CheckSheetLine.class)

    List<CheckSheetLine> test_items
}

class CheckSheetLine {
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

