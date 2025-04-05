def call(String imageName, String tag = 'latest') {
    script {
        sh "docker build -t ${fullImageName} ."
    }
}
