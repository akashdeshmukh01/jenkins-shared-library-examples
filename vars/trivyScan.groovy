def call(String imageName) {
    script {
        try {
            echo "Running Trivy scan on ${imageName}"

            def trivyCmd = """
                trivy --config \$HOME/trivy.yaml --scanners vuln --ignorefile "" image ${imageName}
            """
            def scanResult = sh(script: trivyCmd, returnStatus: true)

            if (scanResult != 0) {
                error("Trivy scan failed! Vulnerabilities found in ${imageName}")
            } else {
                echo "Trivy scan passed! No critical vulnerabilities found in ${imageName}"
            }
        } catch (Exception e) {
            error "Trivy scan failed due to an error: ${e.message}"
        }
    }
}
