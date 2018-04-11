import spock.lang.Specification
import jp.co.toshiba.ITInfra.acceptance.*
import groovy.sql.Sql
import groovy.xml.MarkupBuilder
import com.gh.mygreen.xlsmapper.*
import com.gh.mygreen.xlsmapper.annotation.*

// gradle --daemon clean test --tests "ExcelManageTest2.パース処理"

@XlsSheet(name="Sheet1")
public class Diary {
  @XlsHorizontalRecords(tableLabel="日記", recordClass=DiaryLine.class)
  public List<DiaryLine> lines;
}
 
class DiaryLine {
    @XlsColumn(columnName="日付")
    public String date
    @XlsColumn(columnName="天気")
    public String tenki
    @XlsColumn(columnName="コンテンツ")
    public String content
}

class ExcelManageTest2 extends Specification {


    def params = [
        getconfig_home:  '.',
        project_home:    'src/test/resources',
        db_config:       'src/test/resources/cmdb.groovy',
        last_run_config: 'src/test/resources/log/.last_run',
    ]

    def "パース処理"() {
        when:
        XlsMapper xlsMapper = new XlsMapper();
        Diary sheet = xlsMapper.load(
            new FileInputStream("src/test/resources/日記.xlsx"), // 読み込むExcelファイル。
            Diary.class                     // シートマッピング用のPOJOクラス。
            );

        for(DiaryLine line : sheet.lines) {
            println("${line.date} ${line.tenki} ${line.content}")
        }

        then:
        1 == 1
    }
        // 出力結果:
        // 2011-08-01 晴 今日は何ごともなかった
        // 2011-08-02 曇り 今日も何ごともなかった
        // 問題点:
        // 日付は正しく表示されている。xlsxファイルでも読める

        // 継続調査
        // jett-example
        // xlsmapper

    def "書き込み処理"() {
        when:
        Diary sheet = new Diary();
        
        List<DiaryLine> dairys = new ArrayList<>();
        
        // 1レコード分の作成
        DiaryLine record1 = new DiaryLine();
        record1.date    = "2018/4/11";
        record1.tenki   = "晴れ";
        record1.content = "明日は木曜";
        dairys.add(record1);
        
        DiaryLine record2 = new DiaryLine();
        record2.date    = "2018/4/12";
        record2.tenki   = "晴れ";
        record2.content = "明日は金曜";
        dairys.add(record2);
        
        sheet.lines = dairys;
        
        // シートの書き込み
        XlsMapper xlsMapper = new XlsMapper();
        xlsMapper.save(
            new FileInputStream("src/test/resources/日記.xlsx"), // テンプレートのExcelファイル
            new FileOutputStream(new File('build', "sample_out.xlsx")),     // 書き込むExcelファイル
            sheet                                // 作成したデータ
            );

        then:
        1 == 1
    }
}
