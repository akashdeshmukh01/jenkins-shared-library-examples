def call() {
    pipeline {
        agent any
        stages {
            stage('Load Parameters') {
                steps {
                    script {
                        CONFIG = loadParameters() // Read the YAML file
                    }
                }
            }
            stage('Checkout') {
                steps {
                    script {
                        checkoutCode()
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
                        buildDockerImage('my-image')
                    }
                }
            }
            stage('Trivy Scan') {
                steps {
                    script {
                     trivyScan([imageName: CONFIG.trivy, severity: 'HIGH,CRITICAL', ignoreUnfixed: true]) // Pass a map with the config
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
