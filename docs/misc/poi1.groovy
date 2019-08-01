@Grab("org.apache.poi:poi")
import org.apache.poi.hssf.usermodel.*
@Grab("org.apache.poi:poi-ooxml")
import org.apache.poi.ss.usermodel.WorkbookFactory


new FileInputStream("test.xlsx").withStream { ins ->
  WorkbookFactory.create(ins).with { workbook ->
    workbook.getSheetAt(0).with { sheet ->
      def cellObj = sheet?.getRow(0)?.getCell(0)
      println cellObj
    }
  }
}
