def call(Map config = [:]) {
    def terraformFile = config.terraformOutputFile ?: 'tf_outputs.json'

    def CONFIG
    def COMMIT_HASH
    def TAG
    def FULL_IMAGE_NAME

    stage('Parse Terraform Output') {
        script {
            echo "Reading Terraform output from: ${terraformFile}"
            def outputs = readJSON file: terraformFile
            env.IMAGE_REPO_URL = outputs.gcr_repo_url?.value ?: outputs.ecr_repo_url?.value
            echo "Parsed Image Repository URL: ${env.IMAGE_REPO_URL}"
        }
    }

    stage('Load Parameters') {
        script {
            CONFIG = loadParameters() // load from yaml/json etc.
            COMMIT_HASH = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
            TAG = "build-${COMMIT_HASH}"
            FULL_IMAGE_NAME = "${env.IMAGE_REPO_URL}:${TAG}"
            echo "Docker image to be built and pushed: ${FULL_IMAGE_NAME}"
        }
    }

    stage('Checkout Code') {
        script {
            checkout([$class: 'GitSCM',
                      branches: [[name: '*/master']],
                      userRemoteConfigs: [[url: 'https://github.com/akashdeshmukh01/NodeJS-web-app.git']]
            ])
        }
    }

    stage('SonarQube Analysis') {
        script {
            if (params.RUN_SONARQUBE) {
                sonarQubeScan(CONFIG.sonarqube)
            } else {
                echo "Skipping SonarQube scan"
            }
        }
    }

    stage('Build Docker Image') {
        script {
            buildDockerImage(FULL_IMAGE_NAME)
        }
    }

    stage('Trivy Scan') {
        script {
            if (params.RUN_TRIVY) {
                trivyScan(FULL_IMAGE_NAME)
            } else {
                echo "Skipping Trivy scan"
            }
        }
    }

    stage('Push to Registry') {
        script {
            pushDockerImage(FULL_IMAGE_NAME) // Replaces pushDockerToGCR / pushDockerToECR
        }
    }

    stage('Export Image Info for CD') {
        script {
            writeFile file: 'image_info.json', text: "{\"image\":\"${FULL_IMAGE_NAME}\"}"
            archiveArtifacts artifacts: 'image_info.json', fingerprint: true
            echo "Exported image info: ${FULL_IMAGE_NAME}"
        }
    }
}
