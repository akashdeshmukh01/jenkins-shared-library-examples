def call(String imageNameWithTag) {
    script {
        try {
            echo "Running Trivy scan on image: ${imageNameWithTag}"

            def trivyCmd = "trivy image --exit-code 1 --severity HIGH,CRITICAL ${imageNameWithTag}"
            def scanStatus = sh(script: trivyCmd, returnStatus: true)

            if (scanStatus != 0) {
                error("Trivy scan failed! Vulnerabilities found in ${imageNameWithTag}")
            } else {
                echo "Trivy scan passed. No HIGH or CRITICAL vulnerabilities found in ${imageNameWithTag}"
            }
        } catch (Exception e) {
            error "Trivy scan failed due to an error: ${e.message}"
        }
    }
}

