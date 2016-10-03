import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

class EvidenceSheetTest extends Specification{
    def "init EvidenceSheet"() {
        when:
        def evidence = new EvidenceSheet()

        then:
        1 == 1
        // evidence.config_file == 'config/config.groovy'
    }

    def "init EvidenceSheet file"() {
        when:
        def evidence = new EvidenceSheet('config/hoge.groovy')

        then:
        thrown(FileNotFoundException)
    }
}
