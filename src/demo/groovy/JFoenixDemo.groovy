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
import com.jfoenix.controls.JFXButton
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty

import static groovyx.javafx.GroovyFX.start

def selectedProperty = new SimpleStringProperty("");
start {

    JFXButton button1 = new JFXButton("RAISED BUTTON");
    button1.getStyleClass().add("button-raised");
    // main.getChildren().add(button);

    stage(title: "GroovyFX Chooser Demo", width: 800, height: 500, visible: true, resizable: true) {
        scene(root: group(), stylesheets: resource("/css/jfoenix-components.css")) {
            fxml resource("/fxml/ui/Button.fxml"), {
                onMouseEntered { println "Entered"}
            }
            // vbox(spacing: 10, padding: 10) {
            //     test1 = hbox(spacing: 10, padding: 10) {
            //         button("Open file", onAction: { selectedProperty.set("Open") })
            //         button("Save file", onAction: { selectedProperty.set("Save") })
            //     }
            //     label(id: 'selected')

            // }
        }
    }
    // test1.getChildren().add(button1);

        // scene.getStylesheets().add(ButtonDemo.class.getResource("/css/jfoenix-components.css").toExternalForm());

    // selected.textProperty().bind(selectedProperty)
}
