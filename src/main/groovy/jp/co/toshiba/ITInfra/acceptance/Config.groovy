package jp.co.toshiba.ITInfra.acceptance

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
class Config {
    def configs = [:]
    def servers = [:].withDefault{[:]}
    def devices = [:].withDefault{[:].withDefault{[:]}}
    def date = new Date().format("yyyyMMdd_HHmmss")
    final encryption_mode = 'Blowfish'

    def readConfigFile(String config_file, String keyword = null)
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

    def readNodeConfig(String node_config_dir, String domain, String server_name) {
        if (!servers[domain][server_name]) {
            def node_path = "${node_config_dir}/${domain}/${server_name}"
            def server_config_json = new File("${node_path}.json")
            if (server_config_json.exists()) {
                def results = new JsonSlurper().parseText(server_config_json.text)
                def server_items = [:]
                results.each {
                    server_items[it.test_id] = it.value
                }
                servers[domain][server_name] = server_items
            }
            def device_config_dir = new File(node_path)
            if (device_config_dir.exists()) {
                device_config_dir.eachFile { device_config ->
                    ( device_config.name =~ /(.+).json/ ).each { json_file, test_id ->
                        def results = new JsonSlurper().parseText(device_config.text)
                        devices[domain][server_name][test_id] = results
                    }
                }
            }
        }
        return servers[domain][server_name]
    }

    def setHostConfig(String server_name, String domain, Map domain_results) {
        servers[server_name][domain] = domain_results['test']
    }

    def setDeviceConfig(String server_name, String domain, DeviceResultSheet device_results) {
        def test_headers = device_results['headers'][domain]
        device_results['csvs'][domain].each {test_id, server_domain_csvs ->
            if (server_domain_csvs[server_name]) {
                def header =  test_headers[test_id]
                def csvs = server_domain_csvs[server_name]
                def results = []
                csvs.each { csv ->
                    def i = 0
                    def values = [:]
                    header.each { header_name ->
                        values[header_name] = csv[i]
                        i++
                    }
                    results << values
                }
                devices[server_name][domain][test_id] = results
            }
        }
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
