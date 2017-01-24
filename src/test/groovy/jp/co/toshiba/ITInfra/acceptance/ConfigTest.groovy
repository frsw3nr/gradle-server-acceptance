import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.Charset
import org.apache.commons.io.FileUtils

// gradle --daemon clean test --tests "ConfigTest.Config test read"

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
        config['evidence']['source'] == './src/main/resources/root/jp/サーバーチェックシート.xlsx'
    }

    def "設定ファイルの暗号化"() {
        setup:
        def original = new File('src/test/resources/config.groovy')
        def source   = new File('src/test/resources/encode-test.groovy')
        def target   = new File('src/test/resources/encode-test.groovy-encrypted')
        FileUtils.copyFile(original, source)

        when:
        Config.instance.encrypt('src/test/resources/encode-test.groovy', 'encodekey1234567')

        then:
        target.exists()
    }

    def "暗号化した設定ファイルの復元"() {
        setup:
        def original = new File('src/test/resources/config.groovy')
        def source   = new File('src/test/resources/encode-test.groovy')
        def target   = new File('src/test/resources/encode-test.groovy-encrypted')
        FileUtils.copyFile(original, source)
        Config.instance.encrypt('src/test/resources/encode-test.groovy',
                                'encodekey1234567')

        when:
        Config.instance.decrypt('src/test/resources/encode-test.groovy-encrypted',
                                'encodekey1234567')

        then:
        source.exists()
    }

    def "パスワードなしの復元"() {
        setup:
        def original = new File('src/test/resources/config.groovy')
        def source   = new File('src/test/resources/encode-test.groovy')
        def target   = new File('src/test/resources/encode-test.groovy-encrypted')
        FileUtils.copyFile(original, source)
        Config.instance.encrypt('src/test/resources/encode-test.groovy',
                                'encodekey1234567')

        when:
        Config.instance.decrypt('src/test/resources/encode-test.groovy-encrypted')

        then:
        thrown(IllegalArgumentException)
    }

    def "暗号化された設定ファイルの読み込み"() {
        setup:
        def original = new File('src/test/resources/config.groovy')
        def source   = new File('src/test/resources/encode-test.groovy')
        def target   = new File('src/test/resources/encode-test.groovy-encrypted')
        FileUtils.copyFile(original, source)
        Config.instance.encrypt('src/test/resources/encode-test.groovy',
                                'encodekey1234567')
        when:
        def config = Config.instance.read_config_file('src/test/resources/encode-test.groovy-encrypted', 'encodekey1234567')

        then:
        1 == 1
    }

}
