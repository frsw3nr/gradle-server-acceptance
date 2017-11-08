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

/*
   This example is an improvement over "AllInOne" in that it put the various aspects of UI generation
   in different places.
   Style information has mainly gone into DemoStyle and demo.css. I wish the alignment tweaks could also go there.
   The approach may be further extended into a proper lifecycle management, the use of presentation models,
   controllers, and full-blown meta-design for reusable layouts.
   @author Dierk Koenig
*/

import groovy.transform.Canonical
import groovyx.javafx.SceneGraphBuilder
import groovyx.javafx.beans.FXBindable
import javafx.event.EventHandler
import javafx.collections.FXCollections

import static groovyx.javafx.GroovyFX.start
import static javafx.geometry.HPos.RIGHT
import static javafx.geometry.VPos.BASELINE

class Node {
    @FXBindable String nodeName, aliasName, ip, platforms, group, specificPassword

    String toString() {
        "name: $nodeName, alias: $aliasName,  group: $group, ip:$ip, pass:$specificPassword"
    }
}

def nodes = [
    new Node(nodeName: "ostrich", ip: '192.168.10.1', platforms: 'RedHat6',    group: 'System01'),
    new Node(nodeName: "win2012", ip: '192.168.10.2', platforms: 'Windows',    group: 'System01'),
    new Node(nodeName: "centos6", ip: '192.168.10.3', platforms: 'RedHat6,vCenter', group: 'System01')
]

@Canonical
class GroupTest {
    String id, group_name

    @Override
    String toString() { "$id" }
}

start { app ->
    SceneGraphBuilder builder = delegate
    stage(title: "GroovyfX, SplitPane Demo", width: 800, height: 400, visible: true) {

        scene(fill: GROOVYBLUE) {
            borderPane {
                top {
                    menuBar {
                        menu("File")
                    }
                }
                center {
                    mainFrame delegate, nodes
                }
            }
        }
    }
    // DemoStyle.style builder

    def model = new Node()
    bindModelToViews model, builder
    attachHandlers model, builder

    primaryStage.show()
}

def mainFrame(SceneGraphBuilder builder, nodes) {
    // builder.group(scaleX: 0.25, scaleY: 0.25, translateX: 6, translateY: 4) {
    builder.group() {
        splitPane(orientation: HORIZONTAL) {
        // splitPane(orientation: HORIZONTAL) {
            anchorPane {
                tableView(selectionMode: "single", cellSelectionEnabled: true, editable: true, items: nodes) {
                    tableColumn(editable: true, property: "nodeName", text: "Name", prefWidth: 150,
                            onEditCommit: { event ->
                                Node item = event.tableView.items.get(event.tablePosition.row)
                                item.nodeName = event.newValue
                            }
                    )
                    tableColumn(editable: true, property: "ip", text: "IP", prefWidth: 150,
                            onEditCommit: { event ->
                                Node item = event.tableView.items.get(event.tablePosition.row)
                                item.ip = event.newValue;
                            }
                    )
                    tableColumn(editable: true, property: "platforms", text: "platforms", prefWidth: 150,
                            onEditCommit: { event ->
                                Node item = event.tableView.items.get(event.tablePosition.row)
                                item.platforms = event.newValue;
                            }
                    )
                }
            }
            anchorPane {
                vbox(spacing: 10, padding: 10) {
                    hbox(spacing: 10, padding: 10) {
                        button("New")
                        button("Edit")
                        button("Copy")
                    }
                    label id: 'header', 'Please Send Us Your ip'
                    gridPane {
                        def index = 0

                        label 'NodeName', row: index, column: 0
                        textField id: 'nodeName', row: index, column: 1

                        index += 1
                        label 'AliasName', row: index, column: 0
                        textField id: 'aliasName', row: index, column: 1

                        index += 1
                        label 'IP', row: index, column: 0
                        textField id: 'ip', row: index, column: 1

                        index += 1
                        label 'SpecificPassword', row: index, column: 0
                        passwordField id: 'specificPassword', row: index, column: 1

                        index += 1
                        label 'Group', row: index, column: 0
                        choiceBox id: 'group', row: index, column: 1,
                                items: ["System01", "System02", "System03"]

                        index += 1
                        label 'Platform', row: index, column: 0
                        hyperlink 'Linux', id: 'platform', row: index, column: 1,
                                  onAction: { println "Link 'Linux'" }

                        index += 1
                        button id: 'submit', row: index, column: 1, halignment: RIGHT, "Save"
                    }
                }
            }
        }
    }
}


void bindModelToViews(Node node, SceneGraphBuilder sgb) {
    sgb.with {
        node.nodeNameProperty().bind         nodeName.textProperty()
        node.aliasNameProperty().bind        aliasName.textProperty()
        node.ipProperty().bind               ip.textProperty()
        node.specificPasswordProperty().bind specificPassword.textProperty()
        node.groupProperty().bind            group.getSelectionModel().selectedItemProperty()
    }
}

void attachHandlers(Node node, SceneGraphBuilder sgb) {
    sgb.submit.onAction = { println "update node: $node" } as EventHandler
}
