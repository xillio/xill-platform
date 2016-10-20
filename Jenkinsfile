
node('linux') {

    // Gather all required tools
    // Note the escaped quotes to make this work with spaces
    def mvnHome = tool 'mvn-3.3.9'
    def mvn = "\"${mvnHome}/bin/mvn\""

    // Set the java version for this build
    ENV.JAVA_HOME = tool 'java-1.8'

    // Check out scm
    stage('Checkout') {
        checkout scm
    }

    // Clean the repository
    stage('Clean') {
        sh "${mvn} clean"
    }

    // Run all tests
    stage('Tests') {
        sh "${mvn} verify"
    }

    // Run the sonar analysis
    stage('Sonar') {
        sh "${mvn} sonar:sonar"
    }

    // Deploy to repository
    stage('Deploy') {
        sh "${mvn} deploy"
    }

}