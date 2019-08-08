package jp.co.toshiba.ITInfra.acceptance

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils

import java.sql.SQLException

@CompileStatic
@Slf4j
@ToString(includePackage = false)
class EvidenceManager {

    def set_environment(ConfigTestEnvironment env) {
    }

    def export_cmdb() throws IOException, SQLException {
        CMDBModel.instance.initialize()
        CMDBModel.instance.export(new File(NodeFile.getLogDir(LogStage.CURRENT)).getAbsolutePath())
    }

    def export_cmdb_all() throws IOException, SQLException {
        CMDBModel.instance.initialize()
        CMDBModel.instance.export(new File(NodeFile.getLogDir(LogStage.PROJECT)).getAbsolutePath())
    }

    def archive_json() {
        NodeFile.copyAll(LogStage.CURRENT, LogStage.PROJECT)
        if (!NodeFile.matchDir(LogStage.PROJECT, LogStage.BASE)) {
            NodeFile.copyAll(LogStage.CURRENT, LogStage.BASE)
        }
    }

    def update_evidence_log() {
        log.info "Copy test evidence to '${LogFile.getLogDir(LogStage.PROJECT)}'"
        LogFile.copyAll(LogStage.CURRENT, LogStage.PROJECT)
        if (!LogFile.matchDir(LogStage.PROJECT, LogStage.BASE)) {
            LogFile.copyAll(LogStage.CURRENT, LogStage.BASE)
        }
    }

    def update(String export_type) {
        switch (export_type) {
            case 'local' :
                this.update_evidence_log()
                this.archive_json()
                break

            case 'db' :
                this.export_cmdb()
                break

            case 'db-all' :
                this.export_cmdb_all()
                break

            default :
                def msg = "Unkown export type : ${export_type}"
                throw new IllegalArgumentException(msg)
                break
        }
    }
}
