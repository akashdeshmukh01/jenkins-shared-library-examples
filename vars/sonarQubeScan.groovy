def call(Map sonarConfig) { 
    if (!sonarConfig || !sonarConfig.projectKey || !sonarConfig.sources || !sonarConfig.hostUrl || !sonarConfig.authToken) {
        error "Invalid or missing SonarQube configuration!"
    }

    withSonarQubeEnv('sonarQube') {
        sh """
            sonar-scanner \
            -Dsonar.projectKey=${sonarConfig.projectKey} \
            -Dsonar.sources=${sonarConfig.sources} \
            -Dsonar.host.url=${sonarConfig.hostUrl} \
            -Dsonar.login=${sonarConfig.authToken}
        """
    }
}
