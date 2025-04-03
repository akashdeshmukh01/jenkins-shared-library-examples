def call() {
    def params = readYaml file: 'parameters.yaml'
    
    stage('Checkout Code') {
        checkout scm
    }

    stage('SonarQube Scan') {
        sonarQubeScan(params.sonarqube)
    }

    stage('Trivy Scan') {
        trivyScan(params.trivy)
    }

    stage('OWASP ZAP Scan') {
        zapScan(params.zap)
    }
    
    stage('Build & Push Docker Image') {
        dockerBuildPush()
    }
}
