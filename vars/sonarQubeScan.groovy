def call() {
    script {
        def params = loadParameters()
        echo "Loaded Parameters: ${params}"  // Debugging output

        if (!params?.sonarqube) {
            error "SonarQube configuration is missing in parameter.yaml!"
        }

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
