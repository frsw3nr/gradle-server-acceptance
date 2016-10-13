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
        config['evidence']['source'] == './check_sheet.xlsx'
    }

    def "Config test read param"() {
        // setup:
        // def config = Config.instance.config
        // assert Config.instance.config['evidence']['source'] == './check_sheet.xlsx'
        // // assert config['evidence']['source'] == './check_sheet.xlsx'
    }
}
