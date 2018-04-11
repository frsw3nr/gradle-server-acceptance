import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import net.java.amateras.xlsbeans.*
import net.java.amateras.xlsbeans.annotation.*

// gradle --daemon clean test --tests "ExcelManageTest.パース処理"

@Sheet(name="Sheet1")
public class Nikki {
  @HorizontalRecords(tableLabel="日記", recordClass=NikkiLine.class)
  public List<NikkiLine> lines;
}
 
class NikkiLine {
    @Column(columnName="日付")
    public String date
    @Column(columnName="天気")
    public String tenki
    @Column(columnName="コンテンツ")
    public String content
}

class ExcelManageTest extends Specification {

    def params = [
        getconfig_home:  '.',
        project_home:    'src/test/resources',
        db_config:       'src/test/resources/cmdb.groovy',
        last_run_config: 'src/test/resources/log/.last_run',
    ]

    def "パース処理"() {
        when:
        def evidence = new EvidenceManager(params)
        Nikki nikki = (new XLSBeans()).load(
            new FileInputStream("src/test/resources/日記.xls"), Nikki.class)
        for (line in nikki.lines) {
            println("${line.date} ${line.tenki} ${line.content}")
        }
        // 出力結果:
        // 2011-08-213 晴 今日は何ごともなかった
        // 2011-08-214 曇り 今日も何ごともなかった
        // 問題点:
        // 日付がおかしい
        // xlsxファイルが読めない。日記.xlsx にすると、OLEパースエラーになる

        // 継続調査
        // jett-example
        // xlsmapper
        then:
        1 == 1
    }


}
