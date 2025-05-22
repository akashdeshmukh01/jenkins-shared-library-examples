def call(Map sonarConfig) { 
    if (!sonarConfig || !sonarConfig.projectKey || !sonarConfig.sources || !sonarConfig.hostUrl) {
        error "Invalid or missing SonarQube configuration!"
    }

    withCredentials([string(credentialsId: 'sonar-auth-token', variable: 'SONAR_TOKEN')]) {
        withEnv(["PATH+SONAR=/opt/sonar-scanner/bin"]) {
            withSonarQubeEnv('sonar-scanner') {
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
