def call() {
    script {
        def params = loadParameters()
        def sonarConfig = params.sonarqube

        withSonarQubeEnv('SonarQube') {
            sh """
                sonar-scanner \
                -Dsonar.projectKey=${sonarConfig.projectKey} \
                -Dsonar.sources=${sonarConfig.sources} \
                -Dsonar.host.url=${sonarConfig.hostUrl} \
                -Dsonar.login=${sonarConfig.authToken}
            """
        }
    }
}
