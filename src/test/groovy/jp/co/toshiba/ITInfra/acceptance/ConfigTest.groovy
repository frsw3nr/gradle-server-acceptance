import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*

// gradlew --daemon clean test --tests "ConfigTest.Config test read"

class ConfigTest extends Specification {
    def "Config execption"() {
        try {
            new Config()
        } catch (e) {
            assert e instanceof RuntimeException
        }
    }

    def "Config test read"() {
        when:
        def config = Config.instance.read('src/test/resources/config.groovy')
        then:
        config['evidence']['target'] != './build/check_sheet_<date>.xlsx'
        config['evidence']['source'] == './src/test/resources/check_sheet.xlsx'
    }

    def "Config test read shift-jis"() {
        when:
        def config = Config.instance.read('src/test/resources/config_jp.groovy')
        then:
        config['evidence']['source'] == './サーバーチェックシート.xlsx'
    }
}
