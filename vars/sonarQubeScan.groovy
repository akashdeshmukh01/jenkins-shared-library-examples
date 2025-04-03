def call() {
    def params = readYaml file: 'parameters.yaml'
    def sonarConfig = params.sonarqube

    stage('SonarQube Scan') {
        withSonarQubeEnv('sonarqube-token') {  // Reference the SonarQube server config
            withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {  // Fetch the token from Jenkins credentials
                sh """
                sonar-scanner \
                    -Dsonar.projectKey=${sonarConfig.projectKey} \
                    -Dsonar.sources=${sonarConfig.sources} \
                    -Dsonar.host.url=${sonarConfig.hostUrl} \
                    -Dsonar.login=$SONAR_TOKEN
                """
            }
        }
    }
}
