import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

class EvidenceSheetTest extends Specification{
    def "既定の設定ファイル"() {
        when:
        def evidence = new EvidenceSheet()

        then:
        evidence.evidence_source == './check_sheet.xlsx'
    }

    def "設定ファイルの日付パラメータ変換"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config.groovy')

        then:
        evidence.evidence_target != './build/check_sheet.xlsx'
    }

    def "設定ファイルなし"() {
        when:
        def evidence = new EvidenceSheet('config/hoge.groovy')

        then:
        thrown(FileNotFoundException)
    }

    def "既定のExcelファイル読み込み"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config.groovy')
        evidence.readSheet()

        then:
        evidence.evidence_source == './src/test/resources/check_sheet.xlsx'
    }

    def "日本語Excelファイル読み込み"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config_jp.groovy')
        evidence.readSheet()

        then:
        evidence.evidence_source == './src/test/resources/チェックシート.xlsx'
    }

    def "Excelシートなし"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config2.groovy')
        evidence.readSheet()

        then:
        thrown(IllegalArgumentException)
    }

    def "ステージングディレクトリの初期化"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config1.groovy')
        File fileName = new File("build/check_sheet.xlsx")

        then:
        evidence.readSheet()
        evidence.prepareTestStage()

        expect:
        fileName.exists()==true
    }

    def "既定のExcelファイル書き込み"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config1.groovy')
        evidence.readSheet()
        evidence.prepareTestStage()

        def data = ['Linux': ['ostrich': ['vCenter': [
            'test': ['NumCpu':2, 'PowerState':'PoweredOn', 'MemoryGB':2],
            'verify':['NumCpu':true, 'MemoryGB':false]
        ]]]]
        evidence.updateTestResult('Linux', 'ostrich', 0, data['Linux']['ostrich'])

        then:
        evidence.evidence_source == './src/test/resources/check_sheet.xlsx'
    }

}
