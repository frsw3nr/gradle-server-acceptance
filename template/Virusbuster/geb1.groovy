@Grab('org.gebish:geb-core')
@Grab('org.seleniumhq.selenium:selenium-java')
@Grab('org.seleniumhq.selenium:selenium-chrome-driver')

import static groovy.json.JsonOutput.*
import geb.Browser
import org.openqa.selenium.chrome.ChromeDriver

{->
  def home = System.properties['user.home']
  System.setProperty('webdriver.chrome.driver', "chromedriver.exe")

  // ToDo: 指定したディレクトリにダウンロードできるようにする
  // https://stackoverflow.com/questions/18439851/how-can-i-download-a-file-on-a-click-event-using-selenium
  // System.setProperty('browser.download.folderList', 2) // custom location
  // System.setProperty('browser.download.manager.showWhenStarting', False)
  System.setProperty('browser.download.dir', new File("").getAbsolutePath())
  // System.setProperty('browser.helperApps.neverAsk.saveToDisk', 'text/csv')

}()

Browser.drive(driver:new ChromeDriver()) {
    // 管理コンソールに接続
    go "https://192.168.0.12:4343/officescan/"

    // ログイン認証
    $("#labelUsername").value("root") 
    $("#labelPassword").value("P@ssw0rd") 
    $("#btn-signin").click()

    // iFrame で階層化されているため、withFrame()でフレーム内を探索する
    // menu(メインメニュー) -> main(ボディー)

    // メインメニューから"クライアント管理"を選択
    withFrame('menu') {
        // "クライアント"メニュークリック
        def menu = $("li.menubar-item.menubar-clients")
        println "メインメニュー : '${menu}'"
        menu.collect {
            println it.text()
        }
        menu.click()

        // 次の階層の"クライアント管理"をクリック
        def menu2 = menu.find(text: "クライアント管理")
        println "クライアント管理メニュー : '${menu2}'"
        menu2[0].click()
    }

    // エクスポート ボタンをクリックし、管理対象クライアントリストをCSVダウンロード
    withFrame('menu') {
        withFrame('main') {
            def my_menu = $("#myMenu")
            println "マイメニュー: ${my_menu}"
            my_menu.collect {
                println it.text()
            }

            def export = my_menu.find(text: "エクスポート")
            println "エクスポート : ${export}"
            println export.size()
            export.collect {
                println it.text()
            }
            export[0].click()
        }
    }
    close()
    quit()
}
