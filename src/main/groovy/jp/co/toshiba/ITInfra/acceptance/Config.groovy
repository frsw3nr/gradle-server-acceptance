package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import groovy.transform.ToString
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.Charset

// v0.1.7機能
//     設定ファイルを暗号化する
//     暗号化機能
//         オプションで設定ファイルを指定
//             -p,--password {config}
//         キーコード入力
//             入力
//                 確認
//         暗号化ファイル作成
//             {config}-encrypted
//         平文ファイル削除
//     復元オプション
//         オプションで設定ファイルを指定
//             -p,--password {config}
//         キーコード入力
//             入力
//                 確認
//         妥当性チェック
//             JSONエンコードでエラーがないかチェック
//         平文ファイル復元
//         暗号化ファイル削除
//     実行時に復元
//         config_fileがない場合は、{config_file}-encryptedを探す
//         キーコード入力
//             -k,--keyword {password} 指定で省略可
//         妥当性チェック
//             平文に復元してJSONエンコード

// テストコード

// final String keyword = "encodekey1234567"
// SecretKeySpec key = new SecretKeySpec(keyword.getBytes(Charset.forName("UTF-8")), "AES")

// // 文字列を暗号化 → 復号化
// def target = "testtest_test"
// def encodeKey = crypt(target.getBytes(), key)
// println new String(decrypt(encodeKey, key))

// // ファイルを暗号化 → 復号化
// def fileByte = new File("config.groovy").getBytes()
// def encFile = crypt(fileByte, key)

// new File("config_encrypt.groovy").setBytes(encFile)

// def decFile = decrypt(encFile, key)
// new File("config_decrypt.groovy").setBytes(decFile)

// // 暗号化
// def crypt(byte[] bytes, SecretKeySpec key) {
//   def cph = Cipher.getInstance("AES")
//   cph.init(Cipher.ENCRYPT_MODE, key)

//   return cph.doFinal(bytes)
// }

// // 復号化
// def decrypt(byte[] bytes, SecretKeySpec key) {
//   def cph = Cipher.getInstance("AES")
//   cph.init(Cipher.DECRYPT_MODE, key)

//   return cph.doFinal(bytes)
// }


@Singleton
class Config {
    def configs = [:]
    def date = new Date().format("yyyyMMdd_HHmmss")

    Map read(String config_file) throws IOException {

        if (!configs[config_file]) {
            def config = new ConfigSlurper().parse(new File(config_file).getText("MS932"))
            (config['evidence']).with { evidence ->
                ['target', 'staging_dir'].each {
                    if (evidence[it]) {
                        evidence[it] = evidence[it].replaceAll(
                                                 /<date>/, this.date)
                    }
                }
            }
            configs[config_file] = config
        }
        return configs[config_file]
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
    def encrypt(String config_file, String keyword = null)
        throws IOException, IllegalArgumentException {

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
    def decrypt(String config_file_encrypted, String keyword = null)
        throws IOException, IllegalArgumentException {

    }

//     実行時に復元
//         config_fileがない場合は、{config_file}-encryptedを探す
//         キーコード入力
//             -k,--keyword {password} 指定で省略可
//         妥当性チェック
//             平文に復元してJSONエンコード
    Map read_config_file_encrypted(String config_file_encrypted, String keyword = null)
        throws IOException, IllegalArgumentException {

    }

}
