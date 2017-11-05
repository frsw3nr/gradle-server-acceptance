import groovyx.javafx.*

key = '※Google+のAPIキー'
uid = 110611905999186598367 // user ID
url = "https://www.googleapis.com/plus/v1/people/$uid?key=$key".toURL()
json = new groovy.json.JsonSlurper().parseText(url.text)

GroovyFX.start {
    def sg = new SceneGraphBuilder()

    stage = sg.stage(title: "Profile", width: 640, height:380) {
        scene(fill: black) {
            imageView(x: 20, y: 40, rotationAxis: [0, 1.0, 0]) {
                image(json.image.url.replaceAll(/sz=50/, 'sz=200'))
                effect reflection(fraction: 0.25)
                transition =
                rotateTransition(1.s, from:0, to:360, tween: ease_out)
                onMouseClicked { transition.play() }
            }
            text(x: 240, y: 60, text: json.displayName, fill: white, font: "32pt", textOrigin: "top") {
                effect bloom()
            }
            text(x: 240, y: 120, text: json.tagline, fill: white, font: "16pt", textOrigin: "top")
        }
    }
    stage.show()
    transition.play()
}
