def call(String ecrUrl, String imageName, String tag = "latest") {
    script {
        def fullImageName = "${ecrUrl}/${imageName}:${tag}"
        echo "Pushing Docker image to ECR: ${fullImageName}"
        sh "docker push ${fullImageName}"
    }
}

