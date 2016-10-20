
node('linux') {

    // Gather all required tools
    // Note the escaped quotes to make this work with spaces
    env.M2_HOME = tool 'mvn-3'
    env.JAVA_HOME = tool 'java-1.8'


    // Inject maven settings file
    configFileProvider([configFile(fileId: 'xill-platform/settings.xml', variable: 'MAVEN_SETTINGS')]) {
        def mvn = "\"${env.M2_HOME}/bin/mvn\" -s \"$MAVEN_SETTINGS\""

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

}