import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

import org.apache.poi.ss.usermodel.Workbook
import com.github.k3286.dto.Invoice
import com.github.k3286.dto.InvoiceDetail
import com.github.k3286.report.ReportMaker

// gradle --daemon clean test --tests "ExcelManageTest3.書き込み処理"

class ExcelManageTest3 extends Specification {

    private static BigDecimal TAX_RATE = new BigDecimal(0.08);

    def "書き込み処理"() {
        when:
        Invoice inv = new Invoice();
        inv.setInvoiceNo("INV-00000001");
        inv.setClientPostCode("〒123-3333");
        inv.setClientAddress("東京都品川区東五反田１丁目６−３ 東京建物五反田ビル 108F");
        inv.setClientName("株式会社 松上電気");
        inv.setSalesRep("営業 太郎");
        inv.setInvoiceDate(new Date());

        // 明細行は5行にしておく
        for (int idx = 1; idx <= 5; idx++) {
            InvoiceDetail dtl = new InvoiceDetail();
            dtl.setItemName("サンプル明細ですよ " + idx);
            dtl.setUnitCost(BigDecimal.valueOf(10000));
            dtl.setQuantity(Double.valueOf(idx));
            dtl.setAmt(dtl.getUnitCost().multiply(//
                    BigDecimal.valueOf(dtl.getQuantity())));
            inv.getDetails().add(dtl);
        }
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceDetail dtl : inv.getDetails()) {
            total = total.add(dtl.getAmt());
        }
        // 立替金
        inv.setAdvancePaid(BigDecimal.valueOf(10800));
        // 税額
        inv.setTaxAmt(total.multiply(TAX_RATE));
        // 請求額（税込）
        inv.setInvoiceAmtTaxin(total.add(inv.getTaxAmt()).add(inv.getAdvancePaid()));
        // 備考
        inv.setNote("これは備考です、サンプルとして備考を記述し、"
                + "そして帳票に出力をしてみました。"
                + "折り返してくれるといいのですが、どうでしょうか");

        // 帳票変換
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("inv", inv);

        Workbook workbook = ReportMaker.toReport(map, "src/test/resources/template_invoice.xlsx");

        // ファイル出力
        final String outPath = "build/output_invoice.xlsx";
        FileOutputStream fileOut = new FileOutputStream(outPath);
        workbook.write(fileOut);
        fileOut.close();

        then:
        1 == 1
    }
}
