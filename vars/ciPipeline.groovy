def call(Map config = [:]) {
    def terraformFile = config.terraformOutputFile ?: 'tf_outputs.json'

    pipeline {
        agent any

        environment {
            IMAGE_NAME = 'my-image'
        }

        parameters {
            booleanParam(name: 'RUN_SONARQUBE', defaultValue: true, description: 'Run SonarQube scan')
            booleanParam(name: 'RUN_TRIVY', defaultValue: true, description: 'Run Trivy scan')
        }

        stages {
            stage('Parse Terraform Output') {
                steps {
                    script {
                        echo "Reading Terraform output from: ${terraformFile}"
                        def outputs = readJSON file: terraformFile
                        env.ECR_URL = outputs.ecr_repo_url.value
                        echo "Parsed ECR URL from Terraform output: ${env.ECR_URL}"
                    }
                }
            }

            stage('Load Parameters') {
                steps {
                    script {
                        CONFIG = loadParameters() // Custom method to load config.yaml or similar
                        COMMIT_HASH = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                        TAG = "build-${COMMIT_HASH}"
                        FULL_IMAGE_NAME = "${env.ECR_URL}/${IMAGE_NAME}:${TAG}"
                        echo "Docker image to be built and pushed: ${FULL_IMAGE_NAME}"
                    }
                }
            }

            stage('Checkout Code') {
                steps {
                    script {
                        checkoutCode() // Custom method from shared library
                    }
                }
            }

            stage('SonarQube Analysis') {
                when {
                    expression { return params.RUN_SONARQUBE }
                }
                steps {
                    script {
                        sonarQubeScan(CONFIG.sonarqube) // Expects keys like projectKey, etc.
                    }
                }
            }

            stage('Build Docker Image') {
                steps {
                    script {
                        buildDockerImage("${env.ECR_URL}/${IMAGE_NAME}", TAG)
                    }
                }
            }

            stage('Trivy Scan') {
                when {
                    expression { return params.RUN_TRIVY }
                }
                steps {
                    script {
                        trivyScan("${env.ECR_URL}/${IMAGE_NAME}:${TAG}")
                    }
                }
            }

            stage('Push to ECR') {
                steps {
                    script {
                        pushDockerToECR(env.ECR_URL, IMAGE_NAME, TAG)
                    }
                }
            }
        }
    }
}
