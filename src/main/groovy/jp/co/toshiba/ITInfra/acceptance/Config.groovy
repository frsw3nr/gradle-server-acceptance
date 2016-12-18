package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import groovy.transform.ToString

@Singleton
class Config {
    def configs = [:]
    def date = new Date().format("yyyyMMdd_HHmmss")

    Map read(String config_file) throws IOException {

        if (!configs[config_file]) {
            def config = new ConfigSlurper().parse(new File(config_file).getText("Shift_JIS"))
            ['target', 'staging_dir'].each {
                if (config['evidence'][it]) {
                    config['evidence'][it] = config['evidence'][it].replaceAll(
                                             /<date>/, this.date)
                }
            }
            configs[config_file] = config
        }
        return configs[config_file]
    }

}
