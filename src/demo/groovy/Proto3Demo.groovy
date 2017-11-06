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

class Email3 {
    @FXBindable String node_name, alias_name, ip

    String toString() { "<$node_name> $alias_name : $ip" }
}

start { app ->
    SceneGraphBuilder builder = delegate
    layoutFrame builder
    DemoStyle.style builder

    def model = new Email3()
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

                label 'IP', row: 3, column: 0, valignment: BASELINE
                textField id: 'ip', row: 3, column: 1

                button id: 'submit', row: 5, column: 1, halignment: RIGHT,
                        "Send ip"
            }
        }
    }
}


void bindModelToViews(Email3 email, SceneGraphBuilder sgb) {
    sgb.with {
        email.node_nameProperty().bind node_name.textProperty()
        email.alias_nameProperty().bind alias_name.textProperty()
        email.ipProperty().bind ip.textProperty()
    }
}

void attachHandlers(Email3 email, SceneGraphBuilder sgb) {
    sgb.submit.onAction = { println "preparing and sending the mail: $email" } as EventHandler
}
