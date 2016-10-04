import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

class EvidenceSheetTest extends Specification{
    def "既定の設定ファイル"() {
        when:
        def evidence = new EvidenceSheet()

        then:
        evidence.evidence_source == './check_sheet.xlsx'
    }

    def "設定ファイルパラメータ不足"() {
        when:
        def evidence = new EvidenceSheet('src/test/resources/config1.groovy')

        then:
        evidence.evidence_source == './check_sheet.xlsx'
    }

    def "設定ファイルなし"() {
        when:
        def evidence = new EvidenceSheet('config/hoge.groovy')

        then:
        thrown(FileNotFoundException)
    }
}
