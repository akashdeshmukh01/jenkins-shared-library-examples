def call(Map config) {
    script {
        try {
            echo "Running Trivy scan on ${config.imageName}"

            def trivyCmd = """
                trivy --config \$HOME/trivy.yaml --scanners vuln --ignorefile "" image ${config.imageName}
            """
            def scanResult = sh(script: trivyCmd, returnStatus: true)

            if (scanResult != 0) {
                error("Trivy scan failed! Vulnerabilities found in ${config.imageName}")
            } else {
                echo "Trivy scan passed! No critical vulnerabilities found in ${config.imageName}"
            }
        } catch (Exception e) {
            error "Trivy scan failed due to an error: ${e.message}"
        }
    }
}
