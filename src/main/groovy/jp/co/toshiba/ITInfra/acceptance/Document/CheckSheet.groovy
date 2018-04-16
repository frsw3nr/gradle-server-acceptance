package jp.co.toshiba.ITInfra.acceptance.Document

import jp.co.toshiba.ITInfra.acceptance.*
import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*

// gradle --daemon clean test --tests "ExcelManageTest2.パース処理"

@XlsSheet(regex="CheckSheet.+")
class CheckSheet {
    @XlsSheetName
    String sheetName

    @XlsHorizontalRecords(headerColumn=0, headerRow=3, recordClass=CheckSheetTestItem.class)
    List<CheckSheetTestItem> test_items
}

class CheckSheetTestItem {
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

@XlsSheet(regex="Target")
class TestTargetSheet {
    @XlsSheetName
    String sheetName

    @XlsVerticalRecords(headerColumn=1, headerRow=3, recordClass=TestTaargetSheetItem.class)
    List<TestTaargetSheetItem> test_target_items
}

class TestTaargetSheetItem {
    @XlsColumn(columnName="platform")
    String platform
    @XlsColumn(columnName="virtualization")
    String virtualization
    @XlsColumn(columnName="server_name")
    String server_name
    @XlsColumn(columnName="ip")
    String ip
    @XlsColumn(columnName="os_account_id")
    String os_account_id
}
