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
class NodeFile {
    static TestLogBase testLogs = new TestLogBase()

    static def set_environment(ConfigTestEnvironment env) {
        testLogs.setLogDirs([
            (LogStage.BASE)    : env.get_base_node_dir(),
            (LogStage.PROJECT) : env.get_project_node_dir(),
            (LogStage.CURRENT) : env.get_current_node_dir()
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

    static String getPath(String target, String platform,
                          LogStage stage = LogStage.PROJECT) {
        String nodePath = testLogs.logDirs[stage]
        return nodePath + '/' + target + '/' + platform + '.json'
    }

    static String searchPath(String target, String platform,
                             LogStage stage = LogStage.PROJECT) {
        String nodePath = this.getPath(target, platform, stage)
        if (Files.exists(Paths.get(nodePath))) {
            return nodePath
        }
    }

    static def copyAll(LogStage stageFrom, LogStage stageTo) {
        testLogs.copyAll(stageFrom, stageTo)
    }

    static def copyPlatform(String target, String platform,
                    LogStage stageFrom = LogStage.PROJECT,
                    LogStage stageTo = LogStage.CURRENT
                    ) throws IOException {
        def sourcePath = this.searchPath(target, platform, stageFrom)
        if (sourcePath) {
            def targetDir = new File(this.getTargetDir(target, stageTo))
            if (!(targetDir.exists())) {
                targetDir.mkdirs()
            }
            def targetPath = this.getPath(target, platform, stageTo)
            FileUtils.copyFile(new File(sourcePath), new File(targetPath))
        }
    }

    static def copyTargetJsons(String target,
                         LogStage stageFrom = LogStage.PROJECT,
                         LogStage stageTo = LogStage.CURRENT
                         ) throws IOException {
        new File(this.getLogDir(stageFrom)).eachFile { json ->
            (json.name =~ /${target}__.+.json/).each {
                def targetPath = this.getLogDir(stageTo) + '/' + json.name
                FileUtils.copyFile(json, new File(targetPath))
            }
        }
    }

    static def copyTarget(String target,
                         LogStage stageFrom = LogStage.PROJECT,
                         LogStage stageTo = LogStage.CURRENT
                         ) throws IOException {
        this.copyTargetJsons(target, stageFrom, stageTo)
        def sourceDir = new File(this.getTargetDir(target, stageFrom))
        def targetDir = new File(this.getTargetDir(target, stageTo))
        if (sourceDir.exists()) {
            targetDir.mkdirs()
            FileUtils.copyDirectory(sourceDir, targetDir)
        }
    }
}
