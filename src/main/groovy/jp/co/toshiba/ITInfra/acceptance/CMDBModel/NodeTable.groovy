package jp.co.toshiba.ITInfra.acceptance.CMDBModel

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.apache.commons.io.FileUtils.*
import static groovy.json.JsonOutput.*
import groovy.io.FileType
import groovy.sql.Sql
import java.sql.*
import jp.co.toshiba.ITInfra.acceptance.*
import jp.co.toshiba.ITInfra.acceptance.CMDBModel.*

// 基本セット

// SELECT *
// FROM NODES, GROUPS
// WHERE NODES.GROUP_ID = GROUPS.ID
// AND GROUP_NAME LIKE 'System01'
// AND NODE_NAME LIKE '%'

// プラットフォーム情報

// SELECT NODE_CONFIGS.ID AS NODE_CONFIG_ID, NODE_ID, PLATFORM_ID, PLATFORM_NAME
// FROM PLATFORMS,NODE_CONFIGS
// WHERE PLATFORMS.ID = NODE_CONFIGS.PLATFORM_ID
// AND NODE_ID IN (1, 2)

// プラットフォーム情報詳細

// SELECT NODE_CONFIG_DETAILS.ID AS NODE_CONFIG_DETAIL_ID,
// NODE_ID, PLATFORM_ID, PLATFORM_NAME,
// ITEM_NAME, VALUE
// FROM PLATFORMS,NODE_CONFIGS, NODE_CONFIG_DETAILS
// WHERE PLATFORMS.ID = NODE_CONFIGS.PLATFORM_ID
// AND NODE_CONFIGS.NODE_ID = NODE_ID
// AND NODE_CONFIG_DETAILS.NODE_CONFIG_ID = NODE_CONFIGS.ID
// AND NODE_ID IN (1, 2)

@Slf4j
@InheritConstructors
class NodeTable extends MasterTable {
    def nodes
    def tag_nodes
    def node_configs
    def node_config_details

    def find(Map conditions, int page = 0) throws SQLException {
        def query = "select * from nodes, groups where nodes.group_id = groups.id"
        log.info "QUERY:${query}"
        model.cmdb.rows(query)
    }
}
