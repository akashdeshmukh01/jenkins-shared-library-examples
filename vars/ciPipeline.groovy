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
            def imageName = "my-image"  // Set your image name here
            trivyScan(imageName)  // Pass the image name as a string
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
