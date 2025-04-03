def call() {
    pipeline {
        agent any
        environment {
            CONFIG = readYaml(file: 'parameter.yaml') // Read the YAML file once
        }
        stages {
            stage('Checkout') {
                steps {
                    script {
                        checkoutCode() // Calling shared library function for code checkout
                    }
                }
            }
            stage('SonarQube Analysis') {
                steps {
                    script {
                        sonarQubeScan(CONFIG.sonarqube) // Pass SonarQube config
                    }
                }
            }
            stage('Build Docker Image') {
                steps {
                    script {
                        buildDockerImage()
                    }
                }
            }
            stage('Trivy Scan') {
                steps {
                    script {
                        trivyScan(CONFIG.trivy) // Pass Trivy config
                    }
                }
            }
            stage('Push to ECR') {
                steps {
                    script {
                        pushDockerToECR()
                    }
                }
            }
        }
    }
}
