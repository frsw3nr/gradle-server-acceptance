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
class TestLog {

    String server_name
    String platform
    String base_test_log_dir
    String project_test_log_dir
    String current_test_log_dir
    static LinkedHashMap<LogStage,String> logDirs  = [:]
    static LinkedHashMap<LogStage,String> nodeDirs = [:]

    static def set_environment(ConfigTestEnvironment env) {
        this.logDirs << [
            (LogStage.BASE)    : env.get_base_test_log_dir(),
            (LogStage.PROJECT) : env.get_project_test_log_dir(),
            (LogStage.CURRENT) : env.get_current_test_log_dir()
        ]
        this.nodeDirs << [
            (LogStage.BASE)    : env.get_base_node_dir(),
            (LogStage.PROJECT) : env.get_project_node_dir(),
            (LogStage.CURRENT) : env.get_current_node_dir()
        ]
    }

    static def setLogDirs(Map<LogStage,String> map) {
        this.logDirs << map
    }

    static def setNodeDirs(Map<LogStage,String> map) {
        this.nodeDirs << map
    }

    static Boolean defined(LogStage stage) {
        return (this.logDirs.containsKey(stage) && this.nodeDirs.containsKey(stage))
    }

    static String getLogPathCommon(String target, String platform, 
                                   String metric = null, Boolean shared = false,
                                   LogStage logStage) {
        assert target : "Target should not be null"
        assert platform : "Platform should not be null"
        String logPath = this.logDirs[logStage]
        if (!shared) {
            logPath += "/${target}/${platform}"
        }
        if (metric) {
            logPath += "/${metric}"
        }
        return logPath
    }

    static String getTargetPath(String target, String platform, 
                                String metric = null, Boolean shared = false) {
        return getLogPathCommon(target, platform, metric, shared, LogStage.CURRENT)
    } 

    static String getLogPath(String target, String platform, String metric = null) {
        String logPath = getLogPathCommon(target, platform, metric, false, LogStage.PROJECT)
        if (Files.exists(Paths.get(logPath))) {
            return logPath
        } else {
            return this.getLogPathV1(target, platform, metric)
        }
    }

    static Boolean directoryMatch(LogStage stageFrom, LogStage stageTo) {
        Boolean is_match = false
        try {
            String nodePathFrom = Paths.get(this.nodeDirs[stageFrom]).toRealPath()
            String nodePathTo = Paths.get(this.nodeDirs[stageTo]).toRealPath()
            if (nodePathFrom == nodePathTo)
                is_match = true
        } catch (Exception e) {
            is_match = false
        }
        return is_match
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
        String sourcePath = getLogPathCommon(target, platform, null, stageFrom)
        def sourceDir = new File(sourcePath)
        String targetPath = getLogPathCommon(target, platform, null, stageTo)
        def targetDir = new File(targetPath)
        if (sourceDir.exists()) {
            targetDir.mkdirs()
            FileUtils.copyDirectory(sourceDir, targetDir)
        }
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

    // def copyNodeJson(String target, String platform,
    //                  LogStage stageFrom = LogStage.PROJECT,
    //                  LogStage stageTo = LogStage.CURRENT
    //                  ) throws IOException {
    //     def json_file = "${target}__${platform}.json"
    //     def source_json = new File("${this.base_node_dir}/${json_file}")
    //     def target_json = new File("${this.project_node_dir}/${json_file}")
    //     if (source_json.exists())
    //         target_json << source_json.text
    // }


    // TestLog() 

    // TestLog(String server_name, String platform) {
    //     this.server_name = server_name
    //     this.platform = platform
    // }

    // def get_log_path_v1(String server_name, String platform, String test_id, Boolean shared = false) 

    // def get_log_path_v1(String test_log_dir, String test_id, Boolean shared = false) {
    //     def log_path = null
    //     def staging_dir = new File(test_log_dir)
    //     if (!staging_dir.exists())
    //         return
    //     staging_dir.eachDir { old_domain ->
    //         def old_log_path = test_log_dir + '/' + old_domain.name
    //         if (!shared) {
    //             old_log_path += "/${server_name}/${platform}"
    //         }
    //         old_log_path += '/' + test_id
    //         if (new File(old_log_path).exists()) {
    //             log_path = old_log_path
    //         }
    //     }
    //     return log_path
    // }

    // // def get_log_path(String server_name, String platform, String test_id, Boolean shared = false) 

    // def get_log_path(String test_log_dir, String test_id, Boolean shared = false) {
    //     def log_path = test_log_dir
    //     if (!shared) {
    //         log_path += "/${this.server_name}/${this.platform}"
    //     }
    //     log_path += '/' + test_id
    //     if (new File(log_path).exists()) {
    //         return log_path
    //     }
    // }

    // // 廃止

    // def get_source_log_path(String test_id, Boolean shared = false) {
    //     return this.get_log_path(this.project_test_log_dir, test_id, shared) ?:
    //            this.get_log_path(this.base_test_log_dir, test_id, shared) ?:
    //            this.get_log_path(this.current_test_log_dir, test_id, shared) ?:
    //            this.get_log_path_v1(this.project_test_log_dir, test_id, shared) ?: 
    //            this.get_log_path_v1(this.base_test_log_dir, test_id, shared) ?:
    //            this.get_log_path_v1(this.current_test_log_dir, test_id, shared)
    // }

    // // def get_local_dir(String server_name, String platform)

    // def get_local_dir() {
    //     return "${this.current_test_log_dir}/${this.server_name}/${this.platform}"
    // }

    // def get_target_log_path(String test_id, Boolean shared = false) {
    //     def target_path = this.current_test_log_dir
    //     if (shared == false) {
    //         target_path += "/${this.server_name}/${this.platform}"
    //     }
    //     target_path += '/' + test_id
    //     return target_path
    // }

    // copy_project_log_path(server, platform)
    // copy_project_log_path(server, platform)
}
