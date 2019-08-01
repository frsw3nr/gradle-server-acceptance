import groovy.transform.CompileStatic

@CompileStatic
class Foo {
    def fooMethod(String target){
      def result = target * 3 // 型推論が効く
      result = 1 // String型だったresultに1を代入できる
      println result
    }

    // 返還前
    // def sql_rows_to_csv(List rows, List header = null) {
    //     def header_keys = [:]
    //     def csv = []
    //     rows.each { row ->
    //         def list = []
    //         row.each { column_name, value ->
    //             list << value
    //             if (!header_keys.containsKey(column_name))
    //                 header_keys[column_name] = true
    //         }
    //         csv << list
    //     }
    //     def headers = header_keys.keySet()
    //     if (header)
    //         headers = header
    //     def text = "${headers.join('\t')}\n"
    //     csv.each { line ->
    //         text += "${line.join('\t')}\n"
    //     }
    //     return text
    // }

    def sql_rows_to_csv(List<Map> rows, List header = null) {
        def header_keys = [:]
        List<List> csv = []
        rows.each { row ->
            List<String> list = []
            println "ROW:$row"
            row.each { column_name, value ->
                // String x = value.toString()
                list << value.toString()
                if (!header_keys.containsKey(column_name))
                    header_keys[column_name] = true
            }
            csv << list
        }
        println csv
        def headers = header_keys.keySet()
        if (header)
            headers = header
        def text = "${headers.join('\t')}\n"
        csv.each { line ->
            text += "${line.join('\t')}\n"
        }
        return text
    }

}

def a = new Foo()
a.fooMethod("TEST")
println a.sql_rows_to_csv([['a':1, 'b':2],['a':3, 'b':4]], ['a', 'b'])
