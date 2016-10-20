def final MVN_TOOL = 'mvn-3'
def final JAVA_TOOL = 'java-1.8'


for (platform in ['linux', 'windows', 'osx']) {
    buildOn(platform, false, false)
}

/**
 * This function will configure a node to run a build.
 * @param platform the os to run on (node label)
 * @param runSonar set to true to run a sonar analysis
 * @param deploy set to true to deploy to maven repository
 * @return void
 */
def buildOn(platform, runSonar, deploy) {
    // Select the command line runner
    def cli = bat

    if (isUnix()) {
        cli = sh
    }

    node(platform) {

        // Gather all required tools
        // Note the escaped quotes to make this work with spaces
        env.M2_HOME = tool MVN_TOOL
        env.JAVA_HOME = tool JAVA_TOOL

        // Inject maven settings file
        configFileProvider([configFile(fileId: 'xill-platform/settings.xml', variable: 'MAVEN_SETTINGS')]) {
            def mvnOptions = [
                    // Use the provided settings.xml
                    "-s \"$MAVEN_SETTINGS\"",
                    // Run in batch mode (headless)
                    "-B"
            ]

            def mvn = "\"${env.M2_HOME}/bin/mvn\" ${mvnOptions.join(' ')}"

            // Check out scm
            stage('Checkout') {
                checkout scm
            }

            // Clean the repository
            stage('Clean') {
                cli "${mvn} clean"
            }

            // Run all tests
            stage('Tests') {
                cli "${mvn} verify"
            }

            if (runSonar) {
                // Run the sonar analysis
                stage('Sonar') {
                    cli "${mvn} sonar:sonar"
                }
            }

            if (deploy) {
                // Deploy to repository
                // No need for tests as we already passed verify
                stage('Deploy') {
                    cli "${mvn} deploy -DskipTests"
                }
            }
        }
    }
}
