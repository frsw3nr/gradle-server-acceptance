package jp.co.toshiba.ITInfra.acceptance

import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils

@CompileStatic
@Slf4j
@ToString(includePackage = false)
class LogFile {
    static TestLogBase testLogs = new TestLogBase()

    static def set_environment(ConfigTestEnvironment env) {
        testLogs.setLogDirs([
            (LogStage.BASE)    : env.get_base_test_log_dir(),
            (LogStage.PROJECT) : env.get_project_test_log_dir(),
            (LogStage.CURRENT) : env.get_current_test_log_dir()
        ])
    }

    static def setLogDirs(Map<LogStage,String> map) {
        testLogs.setLogDirs(map)
    }

    static String getLogDir(LogStage stage) {
        return testLogs.getLogDir(stage)
    }

    static Boolean defined(LogStage stage) {
        return testLogs.defined(stage)
    }

    static Boolean matchDir(LogStage stageFrom, LogStage stageTo) {
        return testLogs.matchDir(stageFrom, stageTo)
    }

    static String getTargetDir(String target, LogStage stage = LogStage.PROJECT) {
        return testLogs.getTargetDir(target, stage)
    }

    static String searchTargetDir(String target, LogStage stage = LogStage.PROJECT) {
        return testLogs.searchTargetDir(target, stage)
    }

    static String getPlatformDir(String target, String platform,
                                 LogStage stage = LogStage.PROJECT) {
        return testLogs.getPlatformDir(target, platform, stage)
    }

    static String searchPlatformDir(String target, String platform,
                                    LogStage stage = LogStage.PROJECT) {
        return testLogs.searchPlatformDir(target, platform, stage)
    }

    static String getPath(String target, String platform, String metric,
                          LogStage stage = LogStage.PROJECT) {
        assert target : "Target should not be null"
        assert platform : "Platform should not be null"
        assert metric : "Metric should not be null"
        String logPath = testLogs.logDirs[stage]
        return logPath + '/' + target + '/' + platform + '/' + metric
    }

    static String searchPath(String target, String platform, String metric,
                             LogStage stage = LogStage.PROJECT) {
        String logPath = this.getPath(target, platform, metric, stage)
        return (Files.exists(Paths.get(logPath))) ? 
            logPath : searchPathV1(target, platform, metric, stage)
    }

    static String searchPathV1(String target, String platform, String metric,
                               LogStage stage = LogStage.PROJECT) {
        String logPath = null
        def stagingDir = new File(testLogs.logDirs[stage])
        if (!stagingDir.exists())
            return
        stagingDir.eachDir { domain ->
            def logPathOld = "${domain}/${target}/${platform}/${metric}"
            if (new File(logPathOld).exists()) {
                logPath = logPathOld
            }
        }
        return logPath
    }

    static def copyAll(LogStage stageFrom, LogStage stageTo) {
        testLogs.copyAll(stageFrom, stageTo)
    }

    static def copyTarget(String target,
                          LogStage stageFrom = LogStage.PROJECT,
                          LogStage stageTo = LogStage.CURRENT
                          ) throws IOException {
        def sourceDirPath = this.searchTargetDir(target, stageFrom)
        if (sourceDirPath) {
            def targetDir = new File(this.getTargetDir(target, stageTo))
            if (!(targetDir.exists())) {
                targetDir.mkdirs()
            }
            def sourceDir = new File(sourceDirPath)
            FileUtils.copyDirectory(sourceDir, targetDir)
        }
    }

    static def copyPlatform(String target, String platform,
                            LogStage stageFrom = LogStage.PROJECT,
                            LogStage stageTo = LogStage.CURRENT
                            ) throws IOException {
        def sourceDirPath = this.searchPlatformDir(target, platform, stageFrom)
        if (sourceDirPath) {
            def platformDir = new File(this.getPlatformDir(target, platform, stageTo))
            if (!(platformDir.exists())) {
                platformDir.mkdirs()
            }
            def sourceDir = new File(sourceDirPath)
            FileUtils.copyDirectory(sourceDir, platformDir)
        }
    }
}
