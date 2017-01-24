@Grab('com.h2database:h2')
@GrabConfig(systemClassLoader=true)	 //	 JDBCはシステムクラスローダから探されるので必要
import	 groovy.sql.Sql	  //	 別途DataSourceやConnectionを用意するなら、Sqlのコンストラクタに渡せばOK

def	 db	 =	 Sql.newInstance("jdbc:h2:mem:sample",	 "org.h2.Driver")	  //  Sql#execute()でDDLを実行する
db.execute	 """
create
table	 person	 (
name  varchar(255),
age	 int	  )
"""	  //	 Sql#executeUpdate()使うと、変更した行数が返る

insertSql	 =	 "insert	 into	 person	 (name,	 age)	 values	 (?,	 ?)"
assert  db.executeUpdate(insertSql,	 ['Mike',	 	 13])	 ==	 1
assert	 db.executeUpdate(insertSql,  ['Junko',	 14])	 ==	 1	  //	 Sql#eachRow()は1行ごとに処理する(リソース低負荷)
db.eachRow('select	 *	 from	 person')	 {	 rows ->
	println	 row.name
}
//=>	 Mike	  //	 	   Junko	  //	 Sql#rows()は結果をすべてメモリ上に取得する(リソース高負荷)
println	 db.rows('select  *	 from	 person').collect	 {	 it.name	 }	  //=>	 [Mike,	 Junko]
