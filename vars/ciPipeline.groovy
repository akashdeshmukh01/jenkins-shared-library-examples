def call() {
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
            stage('Fetch Terraform Output') {
                steps {
                    copyArtifacts(projectName: 'terraform-infra-pipeline', selector: lastSuccessful())
                }
            }

            stage('Parse Terraform Output') {
                steps {
                    script {
                        def outputs = readJSON file: 'tf_outputs.json'
                        env.ECR_URL = outputs.ecr_repo_url.value
                        echo "Parsed ECR URL from Terraform output: ${env.ECR_URL}"
                    }
                }
            }

            stage('Load Parameters') {
                steps {
                    script {
                        CONFIG = loadParameters() // Load from config.yaml (e.g., SonarQube details)
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
                        checkoutCode()
                    }
                }
            }

            stage('SonarQube Analysis') {
                when {
                    expression { return params.RUN_SONARQUBE }
                }
                steps {
                    script {
                        sonarQubeScan(CONFIG.sonarqube)
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
