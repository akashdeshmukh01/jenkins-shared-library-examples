def call(String fullImageName) {
    script {
        def registryUrl = fullImageName.split(":")[0]
        echo "Pushing Docker image: ${fullImageName}"
        
        if (registryUrl.contains("amazonaws.com")) {
            echo "Detected AWS ECR"
            withCredentials([usernamePassword(credentialsId: 'aws-creds', usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                sh '''
                    aws configure set aws_access_key_id $AWS_ACCESS_KEY_ID
                    aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY
                    aws configure set default.region us-east-1

                    aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ''' + "${registryUrl}" + '''

                    docker push ''' + "${fullImageName}" + '''
                '''
            }
        } else if (registryUrl.contains("pkg.dev")) {
            echo "Detected Google GCR / Artifact Registry"
            withCredentials([file(credentialsId: 'gcp-service-account-key', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                def registryHost = registryUrl.split("/")[0]
                echo "Docker registry host: ${registryHost}"
                sh '''
                    echo "Activating GCP service account..."
                    gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS
                    
                    echo "Configuring Docker for GCR/Artifact Registry..."
                    gcloud auth configure-docker ''' + "${registryHost}" + ''' --quiet

                    echo "Pushing Docker image to Artifact Registry..."
                    docker push ''' + "${fullImageName}" + '''
                '''
            }
        } else {
            error("Unsupported registry URL: ${registryUrl}")
        }
    }
}
