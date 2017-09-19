package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j
import static groovyx.javafx.GroovyFX.start

@Slf4j
class GUI {

    def test1() {

        start {
            stage(title: "GridPane Demo", width: 400, height: 500, visible: true) {
                scene(fill: GROOVYBLUE) {
                    gridPane(hgap: 5, vgap: 10, padding: 25, alignment: "top_center") {
                        columnConstraints(minWidth: 50, halignment: "right")
                        columnConstraints(prefWidth: 250, hgrow: 'always')

                        label("Please Send Us Your Feedback", style: "-fx-font-size: 18px;",
                                row: 0, columnSpan: 2, halignment: "center", margin: [0, 0, 10]) {
                            onMouseEntered { e -> e.source.parent.gridLinesVisible = true }
                            onMouseExited { e -> e.source.parent.gridLinesVisible = false }
                        }

                        label("Name", hgrow: "never", row: 1, column: 0)
                        textField(promptText: "Your name", row: 1, column: 1)

                        label("Email", row: 2, column: 0)
                        textField(promptText: "Your email address", row: 2, column: 1)

                        label("Message", row: 3, column: 0, valignment: "baseline")
                        textArea(prefRowCount: 8, row: 3, column: 1, vgrow: 'always')

                        button("Send Message", row: 4, column: 1, halignment: "right")
                    }
                }
            }
        }
    }
}
