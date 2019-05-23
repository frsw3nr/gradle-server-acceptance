package jp.co.toshiba.ITInfra.acceptance

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import groovy.transform.ToString
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.Charset
import java.io.Console
import static groovy.json.JsonOutput.*
import groovy.json.*

@Slf4j
@Singleton
// @CompileStatic
class Config {
    def configs = [:]
    // def date = new Date().format("yyyyMMdd_HHmmss")
    final String date_format = "yyyyMMdd_HHmmss"
    final String encryption_mode = 'Blowfish'

    String readConfigFile(String config_file, String keyword = null)
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
            def config_data = readConfigFile(config_file, keyword)
            def config = new ConfigSlurper().parse(config_data)
            def date = new Date().format(this.date_format)
            (config['evidence']).with { evidence ->
                ['target', 'staging_dir', 'csv_export'].each {
                    if (evidence[it]) {
                        evidence[it + '_original'] = evidence[it]
                        evidence[it] = evidence[it].replaceAll(/<date>/, date)
                    }
                }
            }
            config.config_file = config_file
            configs[config_file] = config
        }
        return configs[config_file]
    }

    def get_account(String platform, String id = null) {
        def account = [:]
        def config_account = this?.account
        if (!config_account) {
            def msg = "Not found parameter 'account' in ${this.config_file}"
            log.error(msg)
            throw new IllegalArgumentException(msg)
        }
        if (id) {
            if (!platform) {
                def msg = "Parameter platform required"
                log.error(msg)
                throw new IllegalArgumentException(msg)
            }
            account = config_account[platform][id]
            if (!account) {
                def msg = "Not found parameter 'account.${platform}.${id}' in ${this.config_file}"
                log.error(msg)
                throw new IllegalArgumentException(msg)
            }
        }
        return account
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

    byte[] encryptData(byte[] bytes, SecretKeySpec key) {
      def cph = Cipher.getInstance(encryption_mode)
      cph.init(Cipher.ENCRYPT_MODE, key)

      return cph.doFinal(bytes)
    }

    byte[] decryptData(byte[] bytes, SecretKeySpec key) {
      def cph = Cipher.getInstance(encryption_mode)
      cph.init(Cipher.DECRYPT_MODE, key)

      return cph.doFinal(bytes)
    }

}
