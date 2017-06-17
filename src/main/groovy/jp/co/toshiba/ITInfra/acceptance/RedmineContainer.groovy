package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import groovy.transform.ToString
import groovy.util.ConfigObject
import groovy.json.*
import groovy.sql.Sql
import java.sql.*

@Slf4j
@Singleton
class RedmineContainer {

    def cmdb
    def csv_item_map
    def custom_fileds_map = [:]
    def server_infos = [:].withDefault{[:]}
    def silent
    def redmine_config

    def initialize(EvidenceManager evidence_manager) throws IOException, SQLException {
        if (!this.cmdb) {
            def db_config = evidence_manager.db_config
            def config_db = Config.instance.read(db_config)
            def config_ds = config_db?.cmdb?.dataSource
            if (!config_ds) {
                def msg = "Config not found cmdb.dataSource: ${db_config}"
                throw new IllegalArgumentException(msg)
            }
            this.cmdb = Sql.newInstance(config_ds['url'], config_ds['username'],
                                        config_ds['password'], config_ds['driver'])
            this.csv_item_map = config_db?.cmdb?.redmine?.custom_fields
            this.silent       = evidence_manager?.silent ?: false
            custom_fileds_map = get_custom_fields()
        }
        return this
    }

    def generate_server_sheet(TestRunner test_runner)
        throws IOException, IllegalArgumentException {
        set_default_config(test_runner.config_file)
        def filters
        if (silent) {
            def default_filter_options = redmine_config?.default_filter_options
            if (!default_filter_options)
                throw new IllegalArgumentException("Not found 'default_filter_options' "+
                                                   "in config.groovy")
            filters = get_default_filter_options(default_filter_options)
        } else {
            filters   = input_filter_options()
        }
        log.info "FILTER: ${filters}"
        def redmine_server_infos = get_issues(filters)
        log.info "ISSUES: ${redmine_server_infos}"
        if (!redmine_server_infos) {
            log.info 'Not found target servers.'
            return
        }
        def evidence_sheet = new EvidenceSheet(test_runner.config_file)
        evidence_sheet.evidence_source = test_runner.sheet_file
        evidence_sheet.updateTestTargetSheet(redmine_server_infos)
    }

    def set_default_config(String config_file) {
        def config = Config.instance.read(config_file)
        redmine_config = config['redmine']
    }

    def get_custom_fields() throws SQLException {
        def costom_fields = cmdb.rows('SELECT id, name FROM custom_fields')
        def custom_fileds_map = [:]
        def csv_item_count = 0
        costom_fields.each { custom_field ->
            custom_field.with {
                if (csv_item_map.containsKey(name)) {
                    custom_fileds_map[id] = csv_item_map[name]
                    csv_item_count ++
                }
            }
        }
        if (csv_item_count == 0) {
            throw new SQLException("Malformed Redmine custom fields." +
                                   "Please check 'cmdb.redmine.custom_fields' in cmdb.groovy.")
        }
        return custom_fileds_map
    }

    def input_filter(HashMap settings = [:], String title, List options) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in))

        def default_value = 0
        if (settings.containsKey('default_value'))
            default_value = settings['default_value']

        def choices   = [:]
        def min_range = 1
        println "'${title}' を選択してください"
        if (!settings.containsKey('disable_nofilter')) {
            choices[0] = 0
            min_range  = 0
            println "\t0 : (指定なし)"
        }
        int max_range = 1
        options.each {
            choices[max_range] = it.id
            println "\t${max_range} : ${it.name}"
            max_range ++
        }
        max_range --

        while (true) {
            print "Enter the number of ${title} [${default_value}]: "
            String userInput = input.readLine()

            if(userInput.trim() == "")
                userInput = "${default_value}"

            try {
                int select = userInput.toInteger()
                if (min_range <= select && select <= max_range)
                    return choices[select]
                else
                    throw new NumberFormatException(
                        "Please input the number [${min_range}..${max_range}].")
            } catch (NumberFormatException e) {
                println e
            }
        }
    }

    def input_isok(message = null, default_value = 'y') throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in))

        if (message)
            println message
        while (true) {
            print "Enter 'y' or 'n' [${default_value}]: "
            String userInput = input.readLine().trim()

            if(userInput == "")
                userInput = "${default_value}"

            try {
                if (userInput == 'y' || userInput == 'n')
                    return userInput
                else
                    throw new IllegalArgumentException("Please input 'y' or 'n'.")
            } catch (IllegalArgumentException e) {
                println e
            }
        }
    }

    def get_filter_option(String title, List master_records, Map options, Boolean not_null = false) {
        def id = null
        if (options.containsKey(title)) {
            if (options[title] == '%') {
                return
            }
            def check_count = 0
            master_records.each {
                if (it['name'] == options[title]) {
                    id = it['id']
                    return
                }
                check_count ++
            }
            if (master_records.size() == check_count)
                throw new IllegalArgumentException("'${options[title]}' not found in ${title}")

        } else if (not_null) {
            throw new IllegalArgumentException("Option '${title}' must be specified")
        }
        if (not_null && !id){
            def message = "Not found '${options[title]}' record in master '${title}'."
            throw new IllegalArgumentException(message)
        }
        return id
    }

    def get_default_filter_options(Map options = [:])
        throws SQLException, IllegalArgumentException {
        def filters = [:]

        def projects   = cmdb.rows("SELECT id, name FROM projects ORDER BY created_on DESC")
        def project_id = get_filter_option('project', projects, options, true)
        filters['project'] = project_id

        def statuses = cmdb.rows("SELECT id, name FROM issue_statuses")
        filters['status'] = get_filter_option('status', statuses, options)

        def versions = cmdb.rows("SELECT versions.id, versions.name " +
                                 "FROM versions " +
                                 "WHERE versions.project_id = ?" ,
                                 [project_id] )
        filters['version'] = get_filter_option('version', versions, options)

        def trackers   = cmdb.rows("SELECT trackers.id, trackers.name " +
                                   "FROM trackers, projects_trackers " +
                                   "WHERE projects_trackers.tracker_id = trackers.id " +
                                   "AND projects_trackers.project_id = ?" ,
                                   [project_id])
        filters['tracker'] = get_filter_option('tracker', trackers, options)

        return filters
    }

    def input_filter_options(Map options = [:]) throws SQLException {
        def filters = [:]

        def projects   = cmdb.rows("SELECT id, name FROM projects ORDER BY created_on DESC")
        def project_id = input_filter('Project', projects, disable_nofilter: true, default_value: 1)
        filters['project'] = project_id

        def statuses  = cmdb.rows("SELECT id, name FROM issue_statuses")
        def status_id = input_filter('Status', statuses, default_value: 1)
        if (status_id)
            filters['status'] = status_id

        def versions   = cmdb.rows("SELECT versions.id, versions.name " +
                                   "FROM versions " +
                                   "WHERE versions.project_id = ?",
                                   [project_id])
        def version_id = input_filter('Version', versions)
        if (version_id)
            filters['version'] = version_id

        def trackers = cmdb.rows("SELECT trackers.id, trackers.name " +
                                 "FROM trackers, projects_trackers " +
                                 "WHERE projects_trackers.tracker_id = trackers.id " +
                                 "AND projects_trackers.project_id = ?",
                                 [project_id])
        def tracker_id = input_filter('Tracker', trackers)
        if (tracker_id)
            filters['tracker'] = tracker_id

        return filters
    }

    def get_issues(Map filter_options) throws SQLException {

        def sql = "SELECT id FROM issues " +
        "WHERE project_id = ${filter_options['project']} ";

        if (filter_options['status'])
            sql += "AND status_id = ${filter_options['status']} "

        if (filter_options['version'])
            sql += "AND assigned_to_id = ${filter_options['version']} "

        if (filter_options['tracker'])
            sql += "AND tracker_id = ${filter_options['tracker']} "

        def issue_ids = cmdb.rows(sql)
        if (issue_ids.size() == 0) {
            throw new SQLException('Not found targets')
        }

        def server_names = []
        issue_ids.each { issue_id ->
            def id = issue_id['id']
            sql = "SELECT custom_field_id, value FROM custom_values WHERE customized_id = ?"
            def custom_values = cmdb.rows(sql, [id])
            def server_info = [:]
            custom_values.each { custom_value ->
                custom_value.with {
                    def item_name = custom_fileds_map[custom_field_id]
                    if (item_name)
                        server_info[item_name] = value
                }
            }
            if (server_info['platform'] && server_info['server_name'] &&
                server_info['ip'] && server_info['os_account_id']) {
                server_infos[id] = server_info
                server_names << server_info['server_name']
            }
            else
                log.warn "Issue : #${id}. Malformed input, Skip."
        }
        if (silent || input_isok("検索したサーバは以下の通りです。よろしいですか?\n${server_names}", 'y') == 'y')
            return server_infos
    }

    static void run(String[] args) {
        def redmine = new RedmineContainer()
        redmine.initialize()
        def filters   = redmine.input_filter_options()
        println "FILTER: \n${filters}"
        println filters
        redmine.get_issues(filters)
        println "ISSUES: \n${redmine.server_infos}"
    }

}
