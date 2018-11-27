@Grab('org.gebish:geb-core')
@Grab('org.seleniumhq.selenium:selenium-java')
@Grab('org.seleniumhq.selenium:selenium-chrome-driver')

import geb.Browser
import org.openqa.selenium.chrome.ChromeDriver

{->
  def home = System.properties['user.home']
  System.setProperty('webdriver.chrome.driver',
    "chromedriver.exe"
    // "${home}/apps/selenium/chromedriver"
  )
}()

Browser.drive(driver:new ChromeDriver()) {
    go "https://192.168.0.12:4343/officescan/"

    // assert title == "Geb - Very Groovy Browser Automation" 
    // userBox = driver.find_element_by_css_selector("#labelUsername")
    // userBox.send_keys("root")
    // passBox = driver.find_element_by_css_selector("#labelPassword")
    // passBox.send_keys("P@ssw0rd")

    // loginButton = driver.find_element_by_css_selector("#btn-signin")
    // loginButton.click()

    println "TEST0:" + $("#labelUsername")

    $("#labelUsername").value("root") 
    $("#labelPassword").value("P@ssw0rd") 
    $("#btn-signin").click()

    // waitForExist('iframe#menu', 20000)
    withFrame('menu') {

// <span class="label clickable" menubar="clients" op="1000" rbac="7">クライアント管理</span>
// #osce_nav > ul:nth-child(3) > li.menubar-item.menubar-clients.expand > div > div > ul > li:nth-child(1) > span

        def menu = $("#osce_nav")
        println menu
    }
}

