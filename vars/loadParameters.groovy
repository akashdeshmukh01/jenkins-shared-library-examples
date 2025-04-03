import groovy.yaml.YamlSlurper

def call() {
    script {
        def yamlFile = readYaml file: 'parameter.yaml'
        return yamlFile
    }
}
