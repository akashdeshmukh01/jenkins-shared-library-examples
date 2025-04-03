import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Paths

def call() {
    def yamlFilePath = "parameter.yaml"
    def yamlText = new String(Files.readAllBytes(Paths.get(yamlFilePath)), "UTF-8")
    def yaml = new Yaml()
    return yaml.load(yamlText)
}
