def call(Map config = [:]) {
    def terraformFile = config.terraformOutputFile ?: 'tf_outputs.json'

    def IMAGE_NAME = 'my-image'
    def CONFIG
    def COMMIT_HASH
    def TAG
    def FULL_IMAGE_NAME

    stage('Parse Terraform Output') {
        script {
            echo "Reading Terraform output from: ${terraformFile}"
            def outputs = readJSON file: terraformFile
            env.ECR_URL = outputs.ecr_repo_url.value
            echo "Parsed ECR URL from Terraform output: ${env.ECR_URL}"
        }
    }

    stage('Load Parameters') {
        script {
            CONFIG = loadParameters() // load from yaml/json etc.
            COMMIT_HASH = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
            TAG = "build-${COMMIT_HASH}"
            FULL_IMAGE_NAME = "${env.ECR_URL}/${IMAGE_NAME}:${TAG}"
            echo "Docker image to be built and pushed: ${FULL_IMAGE_NAME}"
        }
    }

    stage('Checkout Code') {
        script {
            checkoutCode() // shared lib helper
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
        script {
            buildDockerImage("${env.ECR_URL}/${IMAGE_NAME}", TAG)
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
        script {
            pushDockerToECR(env.ECR_URL, IMAGE_NAME, TAG)
        }
    }
}
