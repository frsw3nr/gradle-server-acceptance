import static groovy.json.JsonOutput.*
import groovy.json.*

def result_info = [['A':1, 'B':2]]
result_info[0]['C'] = ['D', 'E']
def json = JsonOutput.toJson(result_info)
println JsonOutput.prettyPrint(json)
