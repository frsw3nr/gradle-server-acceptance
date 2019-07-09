ruleset {

    ruleset('rulesets/basic.xml')
    ruleset('rulesets/braces.xml')
    ruleset('rulesets/concurrency.xml')
    ruleset('rulesets/convention.xml')
    ruleset('rulesets/design.xml')
    ruleset('rulesets/dry.xml')
    ruleset('rulesets/enhanced.xml')
    ruleset('rulesets/exceptions.xml')
    ruleset('rulesets/formatting.xml')
    ruleset('rulesets/generic.xml')
    ruleset('rulesets/grails.xml')
    ruleset('rulesets/groovyism.xml')
    ruleset('rulesets/imports.xml')
    ruleset('rulesets/jdbc.xml')
    ruleset('rulesets/junit.xml')
    ruleset('rulesets/logging.xml')
    ruleset('rulesets/naming.xml') {
        // TODO: スネークケースの正規表現でチェックをする様、パラメータ名チェックを有効にする
        exclude '*Name'
        // ClassName {
        //     regex = '^[A-Z][\$a-zA-Z0-9]*$'
        // }
        // FieldName {
        //     finalRegex = '^[a-z][_a-zA-Z0-9]*$'
        //     staticFinalRegex = '^logger$|^[A-Z][_A-Z0-9]*$|^serialVersionUID$'
        // }
        // MethodName {
        //     regex = '^[a-z][\$_a-zA-Z0-9]*$|^.*\\s.*$'
        // }
        // VariableName {
        //     finalRegex = '^[a-z][_a-zA-Z0-9]*$'
        // }
    }
    ruleset('rulesets/security.xml')
    ruleset('rulesets/serialization.xml')
    ruleset('rulesets/size.xml')
    ruleset('rulesets/unnecessary.xml')
    ruleset('rulesets/unused.xml')

}
