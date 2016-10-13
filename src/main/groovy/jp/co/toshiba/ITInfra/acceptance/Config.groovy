package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j

@Singleton
class Config {
    def configs = [:]
    def date = new Date().format("yyyyMMdd_hhmmss")

    Map read(String config_file) throws IOException {

        if (!configs[config_file]) {
            def text = new File(config_file).toURL()
            def config = new ConfigSlurper().parse(text)
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
