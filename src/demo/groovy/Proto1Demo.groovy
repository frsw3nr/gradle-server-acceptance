import groovyx.javafx.*

def test = 2

GroovyFX.start {
    switch (test) {
        case 1:
        stage(title: "GroovyFX @ JavaONE", show: true ) {
            scene(fill: groovyblue, width: 420, height: 420 ) {
                text("Hello World!", layoutY: 50, font: "bold 48pt serif")
            }
        }
        break;

        case 2:
        stage(title: "GroovyFX BorderPane Demo", show: true) {
            scene(fill: groovyblue, width: 650, height:450) {
                fill linearGradient(
                    start: [0, 0.4], end: [0.9, 0.9],
                    stops: [groovyblue, rgb(153,255,255)])
                borderPane {
                    top(align: CENTER, margin: [10,0,10,0]) {
                        button("Top Button")
                    }
                    right(align: CENTER, margin: [0,10,0,1]) {
                        toggleButton("Right Toggle")
                    }
                    left(align: CENTER, margin: [0,10]) {
                        checkBox("Left Check")
                    }
                    bottom(align: CENTER, margin: 10) {
                        textField("Bottom TextField")
                    }
                    label("Center Label")
                }
            }
        }
        break;
    }

}
