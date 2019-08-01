@GrabConfig( systemClassLoader=true )
@Grapes( 
// @Grab(group='com.oracle', module='ojdbc7', version='12.1.0.1'))
@Grab(group='com.oracle', module='ojdbc6', version='11.2.0.3'))
import groovy.sql.Sql
import java.sql.SQLException

// // Oracle DB Settings
// def dbSchema = 'orcl'
// def dbServer = '192.168.0.16'
// def dbUser = 'perfstat'
// def dbPassword = 'perfstat' 
// // def dbDriver = 'oracle.jdbc.driver.OracleDriver'
// def dbDriver = 'oracle.jdbc.OracleDriver'
// def dbUrl = 'jdbc:oracle:thin:@' + dbServer + ':' + dbSchema
// sql = Sql.newInstance( dbUrl, dbUser, dbPassword, dbDriver )

// // Read data
// def row = sql.firstRow("SELECT * FROM tab")
// println row

// import groovy.sql.Sql
// def f = new File('/home/psadmin/work/gradle/ojdbc6.jar')
// this.getClass().classLoader.rootLoader.addURL(f.toURL())

sql = Sql.newInstance("jdbc:oracle:thin:@10.51.219.23:1521:orcl", "zabbix", "zabbix")
def row = sql.firstRow("SELECT * FROM tab")
println row

