@GrabConfig(systemClassLoader=true)
// @Grab(group='mysql', module='mysql-connector-java', version='5.1.+')
// @GrabResolver(name="mysql", root="http://jcenter.bintray.com/")
@Grab('mysql:mysql-connector-java')

import java.util.Scanner
import groovy.sql.Sql
import java.sql.*

class RedmineContainer {

    def cmdb

    def csv_item_map = [
        'ホスト名' :         'server_name',
        'IPアドレス' :       'ip',
        'プラットフォーム' : 'platform',
        'OSアカウント' :     'os_account_id',
        'vCenterアカウント': 'remote_account_id',
        'VMエイリアス名' :   'remote_alias',
        '検証ID' :           'verify_id',
        '比較対象' :         'compare_server',
        'CPU割り当て' :      'NumCpu',
        'メモリ割り当て' :   'MemoryGB',
        'ESXiホスト' :       'ESXiHost',
        'ストレージ構成' :   'HDDtype',
    ]
    def custom_fileds_map = [:]
    def server_infos = [:].withDefault{[:]}
    def silent = false

    def initialize() throws IOException, SQLException {
        if (!this.cmdb) {
            this.cmdb = Sql.newInstance("jdbc:mysql://localhost:3306/redmine",
                                        "redmine",
                                        "getperf",
                                        "com.mysql.jdbc.Driver")
            custom_fileds_map = get_custom_fields()
        }
    }

    def get_custom_fields() throws SQLException {
        def costom_fields = cmdb.rows('SELECT id, name FROM custom_fields')
        def custom_fileds_map = [:]
        def csv_item_count = 0
        costom_fields.each { row ->
            if (csv_item_map.containsKey(row['name'])) {
                custom_fileds_map[row['id']] = csv_item_map[row['name']]
                csv_item_count ++
            }
        }
        if (csv_item_count != csv_item_map.size()) {
            def message = "Malformed Redmine custom fields. Please check 'csv_item_map' in config.groovy.\n"
            message += csv_item_map.toString()
            trhow new SQLException(message)
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

        issue_ids.each { issue_id ->
            def id = issue_id['id']
            sql = "SELECT custom_field_id, value FROM custom_values WHERE customized_id = ?"
            def values = cmdb.rows(sql, [issue_id['id']])
            values.each {
                server_infos[id][custom_fileds_map[it['custom_field_id']]] = it['value']
            }
        }
    }

    static void main(String[] args) {
        def redmine = new RedmineContainer()
        redmine.initialize()
        def filters
        if (redmine.silent) {
            filters = redmine.get_default_filter_options(project: 'クラウド基盤VM払出し',
                                                       status: '構築前',
                                                       version: '%',
                                                       tracker: '%')
        } else {
            filters   = redmine.input_filter_options()
        }
println "FILTER:"
println filters
        // def filters = ['project' : 2, 'status' : 1]
        redmine.get_issues(filters)
println "ISSUES:"
println redmine.server_infos.toString()
        // println redmine.projects.toString()
    }
}
