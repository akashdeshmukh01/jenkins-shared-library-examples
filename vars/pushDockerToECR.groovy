def call(String fullImageName) {
    script {
        echo "Pushing Docker image to ECR: ${fullImageName}"

        def ecrUrl = fullImageName.split(":")[0]

        withCredentials([usernamePassword(credentialsId: 'aws-creds', usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
            sh '''
                aws configure set aws_access_key_id $AWS_ACCESS_KEY_ID
                aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY
                aws configure set default.region us-east-1

                aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ''' + "${ecrUrl}" + '''

                docker push ''' + "${fullImageName}" + '''
            '''
        }
    }
}
