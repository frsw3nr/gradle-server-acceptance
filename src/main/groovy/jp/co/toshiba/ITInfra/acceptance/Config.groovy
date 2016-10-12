package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j

@Singleton
class Config {
    def configs = [:]
    def date = new Date().format("yyyyMMdd_hhmmss")

    Map read(String config_file) throws IOException {

        if (!configs[config_file]) {
            configs[config_file] = new ConfigSlurper().parse(new File(config_file).toURL())
        }
        return configs[config_file]
    }
}
