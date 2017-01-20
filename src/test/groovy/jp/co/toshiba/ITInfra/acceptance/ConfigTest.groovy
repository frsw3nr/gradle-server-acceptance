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
        config['evidence']['source'] == './サーバーチェックシート.xlsx'
    }

//     暗号化機能
//         オプションで設定ファイルを指定
//             -y,--crypt {config.groovy}
//         キーコード入力
//             入力
//                 確認
//         暗号化ファイル作成
//             {config.groovy}-encrypted
//         平文ファイル削除
    // def encrypt(String config_file)
    //     throws IOException, IllegalArgumentException {

    // }
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
        !source.exists()
    }

    def "短いパスワード"() {
        when:
        Config.instance.encrypt('src/test/resources/config.groovy', 'key1')
        then:
        thrown(InvalidKeyException)
    }

//     復元オプション
//         オプションで設定ファイルを指定
//             -y,--crypt {config.groovy}-encrypted
//         キーコード入力
//             入力
//                 確認
//         妥当性チェック
//             JSONエンコードでエラーがないかチェック
//         平文ファイル復元
//         暗号化ファイル削除
    // def decrypt(String config_file_encrypted)
    //     throws IOException, IllegalArgumentException {

    // }
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
        !target.exists()
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
        source.exists()
        !target.exists()
    }

//     実行時に復元
//         config_fileがない場合は、{config_file}-encryptedを探す
//         キーコード入力
//             -k,--keyword {password} 指定で省略可
//         妥当性チェック
//             平文に復元してJSONエンコード
    // def read_config_file_encrypted(String config_file_encrypted, String keyword = null)
    //     throws IOException, IllegalArgumentException {

    // }
    def "暗号化された設定ファイルの読み込み"() {
        setup:
        Config.instance.encrypt('src/test/resources/config.groovy', 'encodekey1234567')

        when:
        def config = Config.instance.read_config_file_encrypted('src/test/resources/config.groovy-encrypted', 'key1')

        then:
        1 == 1
    }

}
