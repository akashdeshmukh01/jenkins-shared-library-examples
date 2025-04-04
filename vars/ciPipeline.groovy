def call() {
    pipeline {
        agent any

        parameters {
            booleanParam(name: 'RUN_SONARQUBE', defaultValue: true, description: 'Check to run SonarQube scan')
            booleanParam(name: 'RUN_TRIVY', defaultValue: true, description: 'Check to run Trivy scan')
            choice(name: 'IMAGE', choices: ['my-image:latest', 'my-image:v1'], description: 'Choose the image to scan')
        }

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
                when {
                    expression { return params.RUN_SONARQUBE } // Run if SonarQube is selected
                }
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
                when {
                    expression { return params.RUN_TRIVY } // Run if Trivy is selected
                }
                steps {
                    script {
                        def imageName = params.IMAGE // Get the image name from parameters
                        trivyScan(imageName) // Run Trivy scan on the selected image
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
