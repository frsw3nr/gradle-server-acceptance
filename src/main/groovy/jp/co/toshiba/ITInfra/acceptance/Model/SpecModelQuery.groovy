package jp.co.toshiba.ITInfra.acceptance.Model

import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import groovy.transform.builder.Builder
import groovy.transform.builder.ExternalStrategy

@CompileStatic
@Slf4j
@ToString(includePackage = false)
 
class SpecModelQuery {
    String target
    String platform
    Boolean exclude_status = false
    List<RunStatus> run_statuses = new ArrayList<>()
}
 
@Builder(builderStrategy = ExternalStrategy, forClass = SpecModelQuery)
class SpecModelQueryBuilder {}
