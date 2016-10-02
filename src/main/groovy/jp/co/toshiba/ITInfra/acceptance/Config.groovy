package jp.co.toshiba.ITInfra.acceptance

@Singleton(strict=false)
class Config {
    def config_file = 'config/config.groovy'
    Map config

    public Config() {
        this.config_file = config_file
        this.config = new ConfigSlurper().parse(new File(config_file).toURL())
    }

}
