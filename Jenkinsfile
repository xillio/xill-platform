
node('linux') {
    stage "Setup"
    def mvnHome = tool 'mvn-3.3.9'
    def mvn = "${mvnHome}/bin/mvn"

    stage "Clean"
    sh "${mvn} clean"

    stage "Tests"
    sh "${mvn} verify"

    stage "Build"

    stage "Sonar"

    stage "Deploy"
}