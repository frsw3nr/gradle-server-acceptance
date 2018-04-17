package jp.co.toshiba.ITInfra.acceptance.Document;

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FileUtils.*
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import net.sf.jett.transform.ExcelTransformer;

public class ReportMaker {

    public static Workbook toReport(Map<String, Object> params, String excel_template)
        throws IOException {
        Workbook workbook = null;
        ExcelTransformer transformer = new ExcelTransformer();
        new FileInputStream(excel_template).withStream { ins ->
            workbook = transformer.transform(ins, params);
        }
        return workbook;
    }
}
