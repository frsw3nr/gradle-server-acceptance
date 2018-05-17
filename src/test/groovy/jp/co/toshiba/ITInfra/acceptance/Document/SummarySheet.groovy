import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.Model.*
import jp.co.toshiba.ITInfra.acceptance.Document.*

import org.apache.poi.ss.usermodel.Workbook
import com.github.k3286.dto.Invoice
import com.github.k3286.dto.InvoiceDetail
// import com.github.k3286.report.ReportMaker

// gradle --daemon test --tests "SummarySheet.初期化"

class SummarySheet extends Specification {
    TestScenario test_scenario

    def setup() {
    }

    def 初期化() {
        setup:
        def excel_parser = new ExcelParser('src/test/resources/check_sheet.xlsx')
        excel_parser.scan_sheet()
        test_scenario = new TestScenario(name: 'root')
        test_scenario.accept(excel_parser)

        def platform_tester = new PlatformTester()

        def test_scheduler = new TestScheduler(platform_tester: platform_tester)
        test_scenario.accept(test_scheduler)

        when:
        println 'Test'
        def test_targets = test_scenario.test_targets
        test_targets.get_all().each { target_name, test_target ->
            test_target.test_platforms.each { platform_name, platform_sets ->
                println "PLATFORM:${target_name},${platform_name}"
                // println "RES:${platform_sets}"
            }
        }

        then:
        1 == 1
    }

    def テンプレート作成() {
        when:
        def root_domains = new TestTargetSet(name : 'root')
        def linux_domains = new TestTargetSet(name : 'Linux')
        def windows_domains = new TestTargetSet(name : 'Windows')
        def linux_target = new TestTarget(name : 'ostrich')
        def windows_target = new TestTarget(name : 'win2012')
        linux_domains.add(linux_target)
        windows_domains.add(windows_target)
        root_domains.add(linux_domains)
        root_domains.add(windows_domains)

        def test_scenario = new TestScenario(name : 'root', test_targets : root_domains)

        then:
        1 == 1
    }

    // private static BigDecimal TAX_RATE = new BigDecimal(0.08);

    // def "エビデンス書き込み"() {
    //     when:
    //     [10,20,40,100].each { row ->
    //         long start = System.currentTimeMillis()

    //         ServerAcceptanceEvidence evidence = new ServerAcceptanceEvidence();
    //         evidence.system = "Aシステム";
    //         (1..row).each { idx ->
    //             ServerAcceptanceConfigs config = new ServerAcceptanceConfigs();
    //             config.system              = "Aシステム";
    //             config.model               = "DL360 G9";
    //             config.user                = "root";
    //             config.password            = "root";
    //             config.ui_type             = "GNOME";
    //             config.os_type             = "CentOS6(64bit)";
    //             config.os_version          = "6.8";
    //             config.cpu_size            = 8;
    //             config.memory_size         = 64;
    //             config.raid_config         = "600GB(RAID-1)";
    //             config.disk_size           = "600GB";
    //             config.disk_partition      = "/";
    //             config.disk_partition_size = "600GB";
    //             config.managed_hostname    = "test${idx}-iLO";
    //             config.managed_ip          = "192.168.10.${idx}";
    //             config.managed_subnet      = "255.255.255.0";
    //             config.managed_gateway     = "192.168.10.254";
    //             config.nic1_hostname       = "test${idx}-eth0";
    //             config.nic1_ip             = "192.168.0.${idx}";
    //             evidence.configs << config;
    //         }
    //         // 帳票変換
    //         Map<String, Object> map = new HashMap<String, Object>();
    //         map.put("evidence", evidence);
    //         println(map);

    //         Workbook workbook = ReportMaker.toReport(map, "src/test/resources/template_evidence2.xlsx");

    //         // ファイル出力
    //         final String outPath = "build/output_invoice.xlsx";
    //         FileOutputStream fileOut = new FileOutputStream(outPath);
    //         workbook.write(fileOut);
    //         fileOut.close();

    //         long elapsed = System.currentTimeMillis() - start
    //         println "Export ${row}, Elapsed : ${elapsed} ms"
    //     }
    //     then:
    //     1 == 1
    // }

    // def "エビデンス書き込みグルーピング"() {
    //     when:
    //     long start = System.currentTimeMillis()

    //     ServerAcceptanceEvidence evidence = new ServerAcceptanceEvidence();
    //     evidence.system = "Aシステム";
    //     def row = 10
    //     (1..row).each { idx ->
    //         def system_idx = idx/5 as int
    //         ServerAcceptanceConfigs config = new ServerAcceptanceConfigs();
    //         config.system              = "Aシステム${system_idx}";
    //         config.model               = "DL360 G9";
    //         config.user                = "root";
    //         config.password            = "root";
    //         config.ui_type             = "GNOME";
    //         config.os_type             = "CentOS6(64bit)";
    //         config.os_version          = "6.8";
    //         config.cpu_size            = 8;
    //         config.memory_size         = 64;
    //         config.raid_config         = "600GB(RAID-1)";
    //         config.disk_size           = "600GB";
    //         config.disk_partition      = "/";
    //         config.disk_partition_size = "600GB";
    //         config.managed_hostname    = "test${idx}-iLO";
    //         config.managed_ip          = "192.168.10.${idx}";
    //         config.managed_subnet      = "255.255.255.0";
    //         config.managed_gateway     = "192.168.10.254";
    //         config.nic1_hostname       = "test${idx}-eth0";
    //         config.nic1_ip             = "192.168.0.${idx}";
    //         evidence.configs << config;
    //     }
    //     // 帳票変換
    //     Map<String, Object> map = new HashMap<String, Object>();
    //     map.put("evidence", evidence);
    //     println(map);

    //     Workbook workbook = ReportMaker.toReport(map, "src/test/resources/template_evidence2.xlsx");

    //     // ファイル出力
    //     final String outPath = "build/output_invoice.xlsx";
    //     FileOutputStream fileOut = new FileOutputStream(outPath);
    //     workbook.write(fileOut);
    //     fileOut.close();

    //     long elapsed = System.currentTimeMillis() - start
    //     println "Export ${row}, Elapsed : ${elapsed} ms"

    //     then:
    //     1 == 1
    // }

    // def "書き込み処理"() {
    //     when:
    //     Invoice inv = new Invoice();

    //     for (int idx = 1; idx <= 5; idx++) {
    //         InvoiceDetail dtl = new InvoiceDetail();
    //         dtl.setItemName("サンプル明細ですよ " + idx);
    //         dtl.setUnitCost(BigDecimal.valueOf(10000));
    //         dtl.setQuantity(Double.valueOf(idx));
    //         dtl.setAmt(dtl.getUnitCost().multiply(//
    //                 BigDecimal.valueOf(dtl.getQuantity())));
    //         inv.getDetails().add(dtl);
    //     }
    //     // 帳票変換
    //     Map<String, Object> map = new HashMap<String, Object>();
    //     map.put("config", inv);

    //     Workbook workbook = ReportMaker.toReport(map, "src/test/resources/template_evidence.xlsx");

    //     // ファイル出力
    //     final String outPath = "build/output_invoice.xlsx";
    //     FileOutputStream fileOut = new FileOutputStream(outPath);
    //     workbook.write(fileOut);
    //     fileOut.close();

    //     then:
    //     1 == 1
    // }

    // def "書き込み処理2"() {
    //     when:
    //     Invoice inv = new Invoice();
    //     inv.setInvoiceNo("INV-00000001");
    //     inv.setClientPostCode("〒123-3333");
    //     inv.setClientAddress("東京都品川区東五反田１丁目６−３ 東京建物五反田ビル 108F");
    //     inv.setClientName("株式会社 松上電気");
    //     inv.setSalesRep("営業 太郎");
    //     inv.setInvoiceDate(new Date());

    //     // 明細行は5行にしておく
    //     for (int idx = 1; idx <= 5; idx++) {
    //         InvoiceDetail dtl = new InvoiceDetail();
    //         dtl.setItemName("サンプル明細ですよ " + idx);
    //         dtl.setUnitCost(BigDecimal.valueOf(10000));
    //         dtl.setQuantity(Double.valueOf(idx));
    //         dtl.setAmt(dtl.getUnitCost().multiply(//
    //                 BigDecimal.valueOf(dtl.getQuantity())));
    //         inv.getDetails().add(dtl);
    //     }
    //     BigDecimal total = BigDecimal.ZERO;
    //     for (InvoiceDetail dtl : inv.getDetails()) {
    //         total = total.add(dtl.getAmt());
    //     }
    //     // 立替金
    //     inv.setAdvancePaid(BigDecimal.valueOf(10800));
    //     // 税額
    //     inv.setTaxAmt(total.multiply(TAX_RATE));
    //     // 請求額（税込）
    //     inv.setInvoiceAmtTaxin(total.add(inv.getTaxAmt()).add(inv.getAdvancePaid()));
    //     // 備考
    //     inv.setNote("これは備考です、サンプルとして備考を記述し、"
    //             + "そして帳票に出力をしてみました。"
    //             + "折り返してくれるといいのですが、どうでしょうか");

    //     // 帳票変換
    //     Map<String, Object> map = new HashMap<String, Object>();
    //     map.put("inv", inv);

    //     Workbook workbook = ReportMaker.toReport(map, "src/test/resources/template_check_sheet.xlsx");

    //     // ファイル出力
    //     final String outPath = "build/output_invoice.xlsx";
    //     FileOutputStream fileOut = new FileOutputStream(outPath);
    //     workbook.write(fileOut);
    //     fileOut.close();

    //     then:
    //     1 == 1
    // }
}
