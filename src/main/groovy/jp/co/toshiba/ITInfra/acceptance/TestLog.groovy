package jp.co.toshiba.ITInfra.acceptance

import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils

// @CompileStatic
// @ToString(includePackage = false)
// public enum LogStage {
//     BASE,
//     PROJECT,
//     CURRENT
// }

@CompileStatic
@Slf4j
// @Singleton
@ToString(includePackage = false)
class TestLog {

    static LinkedHashMap<LogStage,String> logDirs  = [:]
    static LinkedHashMap<LogStage,String> nodeDirs = [:]

    static def rtrimLogDirs() {
        logDirs.each { logStage, logDir ->
            logDirs[logStage] = logDir.replaceAll(/[\/|\\]*$/, '') 
        }
    }
    static def rtrimNodeDirs() {
        nodeDirs.each { logStage, nodeDir ->
            nodeDirs[logStage] = nodeDir.replaceAll(/[\/|\\]*$/, '') 
        }
    }

    static def set_environment(ConfigTestEnvironment env) {
        this.logDirs << [
            (LogStage.BASE)    : env.get_base_test_log_dir(),
            (LogStage.PROJECT) : env.get_project_test_log_dir(),
            (LogStage.CURRENT) : env.get_current_test_log_dir()
        ]
        this.rtrimLogDirs()
        this.nodeDirs << [
            (LogStage.BASE)    : env.get_base_node_dir(),
            (LogStage.PROJECT) : env.get_project_node_dir(),
            (LogStage.CURRENT) : env.get_current_node_dir()
        ]
        this.rtrimNodeDirs()
    }

    static def setLogDirs(Map<LogStage,String> map) {
        this.logDirs << map
        this.rtrimLogDirs()
    }

    static def setNodeDirs(Map<LogStage,String> map) {
        this.nodeDirs << map
        this.rtrimNodeDirs()
    }

    static String getLogDir(LogStage logStage) {
        return this.logDirs[logStage]
    }

    static String getNodeDir(LogStage logStage) {
        return this.nodeDirs[logStage]
    }

    static Boolean defined(LogStage stage) {
        return (this.logDirs.containsKey(stage) && this.nodeDirs.containsKey(stage))
    }

    static Boolean directoryMatch(LogStage stageFrom, LogStage stageTo) {
        Boolean is_match = false
        try {
            is_match = (Paths.get(this.nodeDirs[stageFrom]).toRealPath() == 
                        Paths.get(this.nodeDirs[stageTo]).toRealPath())
        } catch (Exception e) {
            is_match = false
        }
        return is_match
    }

    static String getLogPathCommon(String target, String platform = null, 
                                   String metric = null, Boolean shared = false,
                                   LogStage logStage, Boolean checkExists = false) {
        assert target : "Target should not be null"
        String logPath = this.logDirs[logStage]
        if (!shared) {
            logPath += (platform) ? "/${target}/${platform}" : "/${target}"
        }
        if (metric) {
            logPath += "/${metric}"
        }
        if (checkExists) {
            return (Files.exists(Paths.get(logPath))) ? logPath : null
        } else {
            return logPath
        }
    }

    static String getTargetLogDir(String target) {
        return getLogPathCommon(target, null, null, null, LogStage.CURRENT)
    } 

    static String getTargetPath(String target, String platform, 
                                String metric = null, Boolean shared = false) {
        return getLogPathCommon(target, platform, metric, shared, LogStage.CURRENT)
    } 

    static String getLogPath(String target, String platform, String metric = null) {
        String logPath = getLogPathCommon(target, platform, metric, false, LogStage.PROJECT, true)
        return logPath ?: this.getLogPathV1(target, platform, metric)
    }

    static String getLogPathV1(String target, String platform, String metric = null) {
        String logPath = null
        def stagingDir = new File(this.logDirs[LogStage.PROJECT])
        if (!stagingDir.exists())
            return
        stagingDir.eachDir { domain ->
            def logPathOld = "${domain}/${target}/${platform}"
            if (metric) {
                logPathOld += "/${metric}"
            }
            if (new File(logPathOld).exists()) {
                logPath = logPathOld
            }
        }
        return logPath
    }

    static def copyLogs(String target, String platform,
                          LogStage stageFrom = LogStage.PROJECT,
                          LogStage stageTo = LogStage.CURRENT) throws IOException {
        def sourceDir = new File(getLogPathCommon(target, platform, null, stageFrom))
        def targetDir = new File(getLogPathCommon(target, platform, null, stageTo))
        if (sourceDir.exists()) {
            targetDir.mkdirs()
            FileUtils.copyDirectory(sourceDir, targetDir)
        }
    }

    static String getNodePath(String target, String platform) {
        String nodePath = this.nodeDirs[LogStage.PROJECT]
        nodePath += "/${target}/${platform}.json"
        if (Files.exists(Paths.get(nodePath))) {
            return nodePath
        }
        // String nodePath
        // this.nodeDirs.each { stage, node_dir ->
        //     String checkPath = "${node_dir}/${target}/${platform}.json"
        //     if (Files.exists(Paths.get(checkPath))) {
        //         nodePath = checkPath
        //         return
        //     }
        // }
        // return nodePath
    }

    static def copyNodes(String target, String platform, 
                         LogStage stageFrom = LogStage.PROJECT,
                         LogStage stageTo = LogStage.CURRENT
                         ) throws IOException {
        String nodeDirFrom = this.nodeDirs[stageFrom]
        String nodeDirTo   = this.nodeDirs[stageTo]
        def sourceDir = new File("${nodeDirFrom}/${target}")
        def targetDir = new File("${nodeDirTo}/${target}")
        if (sourceDir.exists()) {
            targetDir.mkdirs()
        }
        def sourceNodeFile = new File("${nodeDirFrom}/${target}__${platform}.json")
        def targetNodeFile = new File("${nodeDirTo}/${target}__${platform}.json")
        if (sourceNodeFile.exists()) {
            FileUtils.copyFile(sourceNodeFile, targetNodeFile)
        }
        def sourceNodeFile2 = new File("${nodeDirFrom}/${target}/${platform}.json")
        def targetNodeFile2 = new File("${nodeDirTo}/${target}/${platform}.json")
        if (sourceNodeFile2.exists()) {
            FileUtils.copyFile(sourceNodeFile2, targetNodeFile2)
        }
    }

    static def copyTargetNodes(String target,
                         LogStage stageFrom = LogStage.PROJECT,
                         LogStage stageTo = LogStage.CURRENT
                         ) throws IOException {
        String nodeDirFrom = this.nodeDirs[stageFrom]
        String nodeDirTo   = this.nodeDirs[stageTo]
        def sourceDir = new File("${nodeDirFrom}/${target}")
        def targetDir = new File("${nodeDirTo}/${target}")
        if (sourceDir.exists()) {
            targetDir.mkdirs()
            FileUtils.copyDirectory(sourceDir, targetDir)
        }
    }
}
