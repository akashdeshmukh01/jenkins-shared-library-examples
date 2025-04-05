def call(String fullImageName) {
    script {
        sh "docker build -t ${fullImageName} ."
    }
}
