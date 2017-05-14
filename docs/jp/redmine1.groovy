@GrabConfig(systemClassLoader=true)
@Grab(group='mysql', module='mysql-connector-java', version='5.1.+')

import java.util.Scanner
import groovy.sql.Sql
import java.sql.*

class RedmineContainer {

    def cmdb
    def projects
    def statuses
    def versions

    def initialize() throws IOException, SQLException {
        if (!this.cmdb) {
            this.cmdb = Sql.newInstance("jdbc:mysql://localhost:3306/redmine",
                                        "redmine",
                                        "getperf",
                                        "com.mysql.jdbc.Driver")
        }
    }

    def readline1(List options) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in))

        int id = 1
        println "Select projects"
        options.each {
            println "\t${it.id} : ${it.name}"
        }
        print "Enter the number of project (${id}): "
        String userInput = input.readLine()

        //if the user entered non-whitespace characters then
        //actually parse their input
        if(!"".equals(userInput.trim()))
        {
            try
            {
                id = Integer.parseInt(userInput);
            }
            catch(NumberFormatException nfe)
            {
                //notify user their input sucks  ;)
            }
        }
        return id
    }

    def select_status(List options) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in))

        def choices = [:]
        choices['%'] = '%'
        println "Select status"
        int id = 1
        options.each {
            choices[id] = it.id
            println "\t${id} : ${it.name}"
            id ++
        }
println choices.toString()
        print "Enter the number of status [%]: "
        String userInput = input.readLine()

        def found = false
        while (!found) {

            if(userInput.trim() == "")
                userInput = '%'
println "userInput1=${userInput}"

            if (choices.containsKey(userInput)) {
println "Found"
                found = true
            } else {
                print "Enter the number of status [%]: "
                userInput = input.readLine()
            }
        }
println "userInput2=${userInput}"
        // //if the user entered non-whitespace characters then
        // //actually parse their input
        // if(!"".equals(userInput.trim()))
        // {
        //     try
        //     {
        //         id = Integer.parseInt(userInput);
        //     }
        //     catch(NumberFormatException nfe)
        //     {
        //         //notify user their input sucks  ;)
        //     }
        // }
        // return id
    }

    def get_master_db() throws SQLException {
        // projects = cmdb.rows("SELECT id, name FROM projects")
        // def id = this.readline1(projects)
        // println "id = ${id}"
        statuses = cmdb.rows("SELECT id, name FROM issue_statuses")
        def id = this.select_status(statuses)
        versions = cmdb.rows("SELECT project_id, id, name FROM versions")
        // println projects.toString()

        // cmdb.eachRow() {
        //     println "${it.id}: ${it.name}"
        // }
    }

    static void main(String[] args) {
        def redmine = new RedmineContainer()
        redmine.initialize()
        redmine.get_master_db()
        // println redmine.projects.toString()
    }
}
