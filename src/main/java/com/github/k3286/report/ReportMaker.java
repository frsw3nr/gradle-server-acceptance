package com.github.k3286.report;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;

import net.sf.jett.transform.ExcelTransformer;

public class ReportMaker {

    /**
     * パラメータと、テンプレートファイル名を指定し、帳票変換を行う
     * @param params パラメータ
     * @param templateName テンプレートファイル名
     * @return 変換したWorkbook
     */
    public static Workbook toReport(Map<String, Object> params, String templateName) {
        Workbook workbook = null;
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(templateName));

            // is = ReportMaker.class.getResourceAsStream("/" + templateName);
            System.out.println(templateName);
            System.out.println(is);
            System.out.println(params);
            ExcelTransformer transformer = new ExcelTransformer();
            workbook = transformer.transform(is, params);
        } catch (InvalidFormatException | IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return workbook;
    }
}
