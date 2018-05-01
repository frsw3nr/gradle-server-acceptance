# レポートの出力を抽象化したクラス(抽象戦略)
class Formatter:
  def output_report(self, title, text):
    raise 'Called abstract method !!'

# HTML形式に整形して出力(具体戦略)
class HTMLFormatter(Formatter):
  def output_report(self, report):
    print ("<html><head><title>"+ report.title + "</title></head><body>")
    for line in report.text:
      print("<p>"+ line +"</p>")
    print ('</body></html>')

# PlaneText形式(*****で囲う)に整形して出力(具体戦略)
class PlaneTextFormatter(Formatter):
  def output_report(self, report):
    print ("***** " +report.title + " *****")
    for line in report.text:
      print( line )

# レポートを表す(コンテキスト)
class Report:
  def __init__(self, formatter):
    self.title = 'report title'
    self.text = ['最高', '順調', '普通']
    self.formatter = formatter
  def output_report(self):
    self.formatter.output_report(self)

html_report = Report( HTMLFormatter() )
html_report.output_report()
#<html><head><title>report title</title></head><body>
#<p>最高</p>
#<p>順調</p>
#<p>普通</p>
#</body></html>
plaintext_report = Report( PlaneTextFormatter() ) 
plaintext_report.output_report()
#***** report title *****
#最高
#順調
#普通
