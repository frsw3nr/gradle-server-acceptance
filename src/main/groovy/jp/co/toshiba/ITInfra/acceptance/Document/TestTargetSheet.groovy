package jp.co.toshiba.ITInfra.acceptance.Document

import groovy.transform.ToString
import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*
import jp.co.toshiba.ITInfra.acceptance.*

@XlsSheet(regex="Target")
class TestTargetSheet {
    @XlsSheetName
    String sheetName

    @XlsVerticalRecords(headerColumn=1, 
                        headerRow=3,
                        recordClass=TestTargetSheetLine.class)
    List<TestTargetSheetLine> lines
}

@ToString
class TestTargetSheetLine {
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
    @XlsMapColumns(previousColumnName="os_account_id")
    Map<String, String> attendedMap

    def is_empty() {
        // 必須項目が ない場合は True を返す
        return (platform == null || server_name == null)
    }


}
