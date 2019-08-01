import com.xlson.groovycsv.CsvParser
import jp.co.toshiba.ITInfra.acceptance.Config
import org.apache.commons.io.FileUtils
import spock.lang.Specification

// gradle --daemon test --tests "ConfigTest"

class ConfigTest extends Specification {
    def original_config = 'src/test/resources/config.groovy'
    def source_config   = 'src/test/resources/encode-test.groovy'
    def target_config   = 'src/test/resources/encode-test.groovy-encrypted'
    def original        = new File(original_config)
    def source          = new File(source_config)
    def target          = new File(target_config)

    def setup() {
        FileUtils.copyFile(original, source)
    }

    def "Config execption"() {
        try {
            new Config()
        } catch (e) {
            assert e instanceof RuntimeException
        }
    }

    def "Config test read"() {
        when:
        def config = Config.instance.read(original_config)
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

    def "設定ファイルの暗号化"() {
        when:
        Config.instance.encrypt(source_config, 'encodekey1234567')
        then:
        target.exists()
    }

    def "暗号化した設定ファイルの復元"() {
        setup:
        Config.instance.encrypt(source_config, 'encodekey1234567')
        when:
        Config.instance.decrypt(target_config, 'encodekey1234567')
        then:
        source.exists()
    }

    def "パスワードなしの復元"() {
        setup:
        Config.instance.encrypt(source_config, 'encodekey1234567')
        when:
        Config.instance.decrypt(target_config)
        then:
        thrown(IllegalArgumentException)
    }

    def "暗号化された設定ファイルの読み込み"() {
        setup:
        Config.instance.encrypt(source_config, 'encodekey1234567')
        when:
        def config = Config.instance.readConfigFile(
            'src/test/resources/encode-test.groovy-encrypted',
            'encodekey1234567')
        then:
        config != null
    }

    def "CSV読み込み"() {
        when:
        def config = Config.instance.read('src/test/resources/config_jp.groovy')
        def csv_item_map = config['evidence']['csv_item_map']

        def csv = new File('src/test/resources/issues.csv').getText("MS932")
        def data = new CsvParser().parse(csv, separator: ',', quoteChar: '"')
        def row_num = 0
        data.each { row ->
            csv_item_map.each { column_name, property ->
                def column_pos = row.columns.get(column_name)
                if (column_pos) {
                    def value = row.values[column_pos]
                    println "$row_num : $property : $column_name : $value"
                }
            }
            row_num ++
        }
        then:
        1 == 1
    }
}
