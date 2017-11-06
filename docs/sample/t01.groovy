@Grab('org.groovyfx:groovyfx:9.0.0-SNAPSHOT')

import groovy.transform.Canonical
import groovy.transform.Canonical
import groovyx.javafx.SceneGraphBuilder
import groovyx.javafx.beans.FXBindable
import javafx.event.EventHandler
import javafx.collections.FXCollections

import static groovyx.javafx.GroovyFX.start
import static javafx.geometry.HPos.RIGHT
import static javafx.geometry.VPos.BASELINE

@Canonical
class Person {
    @FXBindable String name
    int age

    String toString() { "Nmae:$name,Age:$age" }
}

def a = [new Person("AA", 30), new Person("BB", 40)]

println "${a}"
