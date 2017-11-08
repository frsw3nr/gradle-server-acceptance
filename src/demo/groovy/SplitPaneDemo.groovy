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
import groovyx.javafx.SceneGraphBuilder
import groovyx.javafx.beans.FXBindable
import javafx.event.EventHandler
import javafx.collections.FXCollections

import static groovyx.javafx.GroovyFX.start

def layoutFrame(SceneGraphBuilder builder) {
    builder.anchorPane {
        splitPane(orientation: HORIZONTAL, anchor: [0, 0, 0, 0]) {
            // dividerPosition(index: 0, position: 0.25)
            // dividerPosition(index: 1, position: 0.50)
            // dividerPosition(index: 2, position: 1.0)
            anchorPane {
                button("ONE", leftAnchor: 10)
                button("TWO", rightAnchor: 10, bottomAnchor: 10)
            }
            anchorPane {
                label("Label 1") // left or top
                gridPane(prefHeight : 400.0, prefWidth : 600.0, anchor: [0, 0, 0, 0]) {
                    def index = 0

                    label 'NodeName', row: index
                    textField id: 'nodeName', row: index, column: 1

                    index += 1
                    label 'AliasName', row: index
                    textField id: 'aliasName', row: index, column: 1

                    index += 1
                    label 'IP', row: index
                    textField id: 'ip', row: index, column: 1

                    index += 1
                    label 'SpecificPassword', row: index, column: 0
                    passwordField id: 'specificPassword', row: index, column: 1
                }
            }
        }
    }
}

start {
    stage(title: "GroovyfX, SplitPane Demo", width: 800, height: 400, visible: true) {
        scene(fill: GROOVYBLUE) {
            def left_pane = delegate
            layoutFrame(left_pane)
        }
    }
}


