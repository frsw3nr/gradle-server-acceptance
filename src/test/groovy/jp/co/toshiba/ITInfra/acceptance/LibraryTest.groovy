/*
 * This Spock specification was auto generated by running 'gradle init --type groovy-library'
 * by 'psadmin' at '16/09/30 5:33' with Gradle 2.3
 *
 * @author psadmin, @date 16/09/30 5:33
 */

import spock.lang.Specification

class LibraryTest extends Specification{
    def "someLibraryMethod returns true"() {
        setup:
        Library lib = new Library()
        when:
        def result = lib.someLibraryMethod()
        then:
        result == true
    }
}
