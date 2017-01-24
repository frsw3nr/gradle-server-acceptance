package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import groovy.transform.ToString
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.Charset
import java.io.Console

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
// SecretKeySpec key = new SecretKeySpec(keyword.getBytes(Charset.forName("UTF-8")), "Blowfish")

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
//   def cph = Cipher.getInstance("Blowfish")
//   cph.init(Cipher.ENCRYPT_MODE, key)

//   return cph.doFinal(bytes)
// }

// // 復号化
// def decrypt(byte[] bytes, SecretKeySpec key) {
//   def cph = Cipher.getInstance("Blowfish")
//   cph.init(Cipher.DECRYPT_MODE, key)

//   return cph.doFinal(bytes)
// }

@Slf4j
@Singleton
class Config {
    def configs = [:]
    def date = new Date().format("yyyyMMdd_HHmmss")
    final encryption_mode = 'Blowfish'

    def read_config_file(String config_file, String keyword = null)
        throws IOException, IllegalArgumentException {
        new File(config_file).with {
            def decrypte_file_name = it.name.replaceAll(/-encrypted$/, "")
            if (decrypte_file_name == it.name) {
                return it.getText("MS932")
            }
            def password = keyword ?: inputPassword()
            SecretKeySpec key = new SecretKeySpec(password.getBytes(), encryption_mode)
            return new String(decryptData(it.getBytes(), key), "MS932")
        }
    }

    Map read(String config_file, String keyword = null) throws IOException {
        if (!configs[config_file]) {
            def config_data = read_config_file(config_file, keyword)
            def config = new ConfigSlurper().parse(config_data)
            (config['evidence']).with { evidence ->
                ['target', 'staging_dir'].each {
                    if (evidence[it]) {
                        evidence[it + '_original'] = evidence[it]
                        evidence[it] = evidence[it].replaceAll(
                                                 /<date>/, this.date)
                    }
                }
            }
            configs[config_file] = config
        }
        return configs[config_file]
    }

    String inputPassword(Map options = [:]) {
        def keyword = null
        Console console = System.console()
        if (!console) {
            throw new IllegalArgumentException("Can not enter console")
        }
        while (!keyword) {
            def password = console.readPassword("Password: ")
            if (password.size() == 0)
                continue
            if (options['confirm']) {
                def confirm = console.readPassword("Confirm: ")
                if (password != confirm) {
                    println "Not match"
                    continue
                }
            }
            keyword = password
        }
        return "$keyword"
    }

    def encrypt(String config_file, String keyword = null)
        throws IOException, IllegalArgumentException {
        try {
            new ConfigSlurper().parse(new File(config_file).getText("MS932"))
        } catch (Exception e) {
            throw new IllegalArgumentException("Parse error '${config_file}'. It must be groovy plain code.")
        }
        def password = keyword ?: inputPassword(confirm : true)
        SecretKeySpec key = new SecretKeySpec(password.getBytes(), encryption_mode)
        // SecretKeySpec key = new SecretKeySpec(keyword, encryption_mode)

        new File(config_file).with {
            def data = encryptData(it.getBytes(), key)
            def config_file_encrypted = new File(it.parent, it.name + '-encrypted')
            config_file_encrypted.setBytes(data)
            // it.delete()
            log.info "OK\nEncrypted ${config_file_encrypted}"
        }
    }

    def decrypt(String config_file_encrypted, String keyword = null)
        throws IOException, IllegalArgumentException {
        def password = keyword ?: inputPassword()
        SecretKeySpec key = new SecretKeySpec(password.getBytes(), encryption_mode)
        new File(config_file_encrypted).with {
            def decrypte_file_name = it.name.replaceAll(/-encrypted/, "")
            if (decrypte_file_name == it.name) {
                throw new IllegalArgumentException("File name must be include '-encrypted' : ${it.name}")
            }
            def data = decryptData(it.getBytes(), key)
            def config_file_decrypted = new File(it.parent, decrypte_file_name)
            config_file_decrypted.setBytes(data)
            try {
                new ConfigSlurper().parse(config_file_decrypted.getText("MS932"))
            } catch (Exception e) {
                throw new IllegalArgumentException("Parse error '${decrypte_file_name}' : " + e)
            }
            // it.delete()
            log.info "OK\nDecrypted ${config_file_decrypted}"
        }
    }

    def encryptData(byte[] bytes, SecretKeySpec key) {
      def cph = Cipher.getInstance(encryption_mode)
      cph.init(Cipher.ENCRYPT_MODE, key)

      return cph.doFinal(bytes)
    }

    def decryptData(byte[] bytes, SecretKeySpec key) {
      def cph = Cipher.getInstance(encryption_mode)
      cph.init(Cipher.DECRYPT_MODE, key)

      return cph.doFinal(bytes)
    }

}
