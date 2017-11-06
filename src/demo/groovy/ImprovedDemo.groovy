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

import groovyx.javafx.SceneGraphBuilder
import groovyx.javafx.beans.FXBindable
import javafx.event.EventHandler

import static groovyx.javafx.GroovyFX.start
import static javafx.geometry.HPos.RIGHT
import static javafx.geometry.VPos.BASELINE

class NodeTest {
    @FXBindable String node_name, alias_name, ip, group, specific_password

    String toString() { "<$node_name> $alias_name : $group, $ip, $specific_password" }
}

start { app ->
    SceneGraphBuilder builder = delegate
    layoutFrame builder
    DemoStyle.style builder

    def model = new NodeTest()
    bindModelToViews model, builder
    attachHandlers model, builder

    primaryStage.show()
}

def layoutFrame(SceneGraphBuilder sgb) {
    sgb.stage {
        scene {
            gridPane {
                label id: 'header', row: 0, column: 1,
                        'Please Send Us Your ip'

                label 'NodeName', row: 1, column: 0
                textField id: 'node_name', row: 1, column: 1

                label 'AliasName', row: 2, column: 0
                textField id: 'alias_name', row: 2, column: 1

                label 'IP', row: 3, column: 0
                textField id: 'ip', row: 3, column: 1

                label 'SpecificPassword', row: 4, column: 0
                passwordField id: 'specific_password', row: 4, column: 1

                label 'Group', row: 5, column: 0
                choiceBox id: 'group', row: 5, column: 1,
                        items: ["one", "two", "three"]

                button id: 'submit', row: 6, column: 1, halignment: RIGHT,
                        "Send ip"
            }
        }
    }
}

void bindModelToViews(NodeTest node, SceneGraphBuilder sgb) {
    sgb.with {
        node.node_nameProperty().bind node_name.textProperty()
        node.alias_nameProperty().bind alias_name.textProperty()
        node.ipProperty().bind ip.textProperty()
        node.specific_passwordProperty().bind specific_password.textProperty()
        node.groupProperty().bind group.getSelectionModel().selectedItemProperty()
    }
}

void attachHandlers(NodeTest node, SceneGraphBuilder sgb) {
    sgb.submit.onAction = { println "preparing and sending the mail: $node" } as EventHandler
}
