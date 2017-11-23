/*
 * Copyright 2011-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import static groovyx.javafx.GroovyFX.start

start {
    stage(title: "GroovyFX FXML Demo", visible: true) {
        scene(fill: GROOVYBLUE, width: 1000, height: 800) {
            // vbox(padding: 10) {
            //     stackPane {
                    fxml resource("/TreeTableView.fxml"), {
                        onMouseEntered { println "Entered"}
                    }
            //     }
            // }
        }
    }
    ObservableList<Person> dummyData = [
        new Person(name: "Jim Clarke", age: 29, gender: Gender.MALE, dob: new Date() - 90),
        new Person(name: "Dean Iverson", age: 30, gender: Gender.MALE, dob: new Date() - 45),
        new Person(name: "Angelina Jolie", age: 36, gender: Gender.FEMALE, dob: new Date())
    ]
    table_view = primaryStage.scene.lookup('#treeTableView')

def obj = table_view
println """
ITEM PROP1: ${obj.properties}
ITEM METH1: ${obj.metaClass.methods.name.sort().unique()}
"""
}
