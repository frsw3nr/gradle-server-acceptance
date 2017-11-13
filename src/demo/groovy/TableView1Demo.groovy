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
import groovy.transform.Canonical
import groovyx.javafx.beans.FXBindable

import java.text.SimpleDateFormat

import static groovyx.javafx.GroovyFX.start

enum Gender {
    MALE, FEMALE
}

@Canonical
class Person {
    @FXBindable String name
    @FXBindable int age
    @FXBindable Gender gender
    @FXBindable Date dob
}

def persons = [
        new Person(name: "Jim Clarke", age: 29, gender: Gender.MALE, dob: new Date() - 90),
        new Person(name: "Dean Iverson", age: 30, gender: Gender.MALE, dob: new Date() - 45),
        new Person(name: "Angelina Jolie", age: 36, gender: Gender.FEMALE, dob: new Date())
]

def dateFormat = new SimpleDateFormat("MMM dd, yyyy")

start {
    stage(title: "GroovyFX Table Demo", width: 500, height: 200, visible: true) {
        scene(fill: GROOVYBLUE) {
            tableView(selectionMode: "single", cellSelectionEnabled: true, editable: true, items: persons,
                onMouseClicked: {event ->
                    println "TARGET:${event.pickResult}"
def obj = event.source.getItems()
println "GETITEM:${obj}"
println "GETCOLUMS:${event.source.getColumns()}"
println "${event.source}"
def props = obj.properties
def methods1 = obj.metaClass.methods.name.sort().unique()
def methods2 = obj.class.methods.name.sort().unique()

// event.sourceでプロパティ、メソッドを表示するとプロパティから row が取得できる
// [anchor:TablePosition [ row: 1, column: javafx.scene.control.TableColumn@19286b89, tableView: TableView@ab9f1bd[styleClass=root table-view] ]]

// event.source.getItems()とすると、バインドした persons が取得できる
// GETITEM:[Person(Jim Clarke, 29, MALE, Wed Aug 16 06:17:06 JST 2017), Person(Dean Iverson, 30, MALE, Sat Sep 30 06:17:06 JST 2017), Person(Angelina Jolie, 36, FEMALE, Tue Nov 14 06:17:06 JST 2017)]

println """
properties:
${props}
metaClass methods:
${methods1}
class methods:
${methods2}
"""
                }
                    // onMouseClicked: {event ->
                    //     println "Event: ${event}"
                    //     def row = event.target.getParent().id
                    //     println "Row: ${row}"
                    //     def item = event.source.items.get(row)
                    //     println "Item: ${item}"
                    // }
                ) {
                tableColumn(editable: true, property: "name", text: "Name", prefWidth: 150,
                        onEditCommit: { event ->
                            Person item = event.tableView.items.get(event.tablePosition.row)
                            item.name = event.newValue
                        }
                )
                tableColumn(editable: true, property: "age", text: "Age", prefWidth: 50, type: Integer,
                        onEditCommit: { event ->
                            Person item = event.tableView.items.get(event.tablePosition.row)
                            item.age = Integer.valueOf(event.newValue)
                        }
                )
                tableColumn(editable: true, property: "gender", text: "Gender", prefWidth: 150, type: Gender,
                        onEditCommit: { event ->
                            Person item = event.tableView.items.get(event.tablePosition.row)
                            item.gender = event.newValue;
                        }
                )
                tableColumn(editable: true, property: "dob", text: "Birth", prefWidth: 150, type: Date,
                        converter: { from ->
                            // convert date object to String
                            return dateFormat.format(from)
                        },
                        onEditCommit: { event ->
                            Person item = event.tableView.items.get(event.tablePosition.row)
                            // convert TextField string to a date object.
                            Date date = dateFormat.parse(event.newValue)
                            item.dob = date
                        }
                )
            }
        }
    }
}

