package jp.co.toshiba.ITInfra.acceptance

import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils

@CompileStatic
@ToString(includePackage = false)
public enum LogStage {
    BASE,
    PROJECT,
    CURRENT
}

@CompileStatic
@Slf4j
// @Singleton
@ToString(includePackage = false)
class TestLogBase {

    LinkedHashMap<LogStage,String> logDirs  = [:]

    def rtrimLogDirs() {
        logDirs.each { logStage, logDir ->
            logDirs[logStage] = logDir.replaceAll(/[\/|\\]*$/, '') 
        }
    }

    def setLogDirs(Map<LogStage,String> map) {
        this.logDirs << map
        this.rtrimLogDirs()
    }

    String getLogDir(LogStage logStage) {
        return this.logDirs[logStage]
    }

    Boolean defined(LogStage stage) {
        return this.logDirs.containsKey(stage)
    }

    Boolean matchDir(LogStage stageFrom, LogStage stageTo) {
        Boolean is_match = false
        try {
            is_match = (Paths.get(this.logDirs[stageFrom]).toRealPath() == 
                        Paths.get(this.logDirs[stageTo]).toRealPath())
        } catch (Exception e) {
            is_match = false
        }
        return is_match
    }

    String getTargetDir(String target,
                        LogStage stage = LogStage.PROJECT) {
        assert target : "Target should not be null"
        return this.logDirs[stage] + '/' + target
    }

    String searchTargetDir(String target,
                           LogStage stage = LogStage.PROJECT) {
        String logPath = this.getTargetDir(target, stage)
        if (Files.exists(Paths.get(logPath))) {
            return logPath
        }
    }

    String getPlatformDir(String target, String platform,
                          LogStage stage = LogStage.PROJECT) {
        assert target : "Target should not be null"
        assert platform : "Platform should not be null"
        return this.logDirs[stage] + '/' + target+ '/' + platform
    }

    String searchPlatformDir(String target, String platform,
                             LogStage stage = LogStage.PROJECT) {
        String logPath = this.getPlatformDir(target, platform, stage)
        if (Files.exists(Paths.get(logPath))) {
            return logPath
        }
    }

    def copyAll(LogStage stageFrom, LogStage stageTo) {
        def target_path = new File(this.logDirs[stageTo]).getAbsolutePath()
        FileUtils.copyDirectory(new File(this.logDirs[stageFrom]), new File(target_path))
    }
}
