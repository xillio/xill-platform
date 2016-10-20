parallel(
        "Windows": {
            buildOn(
                    platform: "windows"
            )
        },

        "Linux": {
            buildOn(
                    platform: "linux"
            )
        },

        "Mac OSX": {
            buildOn(
                    platform: "mac"
            )
        }
)

/**
 * This function will configure a node to run a build.
 * @param platform the os to run on (node label)
 * @param runSonar set to true to run a sonar analysis
 * @param deploy set to true to deploy to maven repository
 * @return void
 */
def buildOn(Map args) {
    def platform = args.get('platform', 'linux')
    def runSonar = args.get('runSonar', false)
    def deploy = args.get('deploy', false)

    node(platform) {

        // Gather all required tools
        // Note the escaped quotes to make this work with spaces
        def m2Tool = tool 'mvn-3'
        env.M2_HOME = m2Tool
        def javaTool = tool 'java-1.8'
        env.JAVA_HOME = javaTool

        // Inject maven settings file
        configFileProvider([configFile(fileId: 'xill-platform/settings.xml', variable: 'MAVEN_SETTINGS')]) {
            def mvnOptions = [
                    // Use the provided settings.xml
                    "-s \"$MAVEN_SETTINGS\"",
                    // Run in batch mode (headless)
                    "-B"
            ]

            def mvn = "\"${m2Tool}/bin/mvn\" ${mvnOptions.join(' ')}"

            // Check out scm
            stage("$platform: Checkout") {
                checkout scm
            }

            // Clean the repository
            stage("$platform: Clean") {
                cli "${mvn} clean"
            }

            // Run all tests
            stage("$platform: Tests") {
                cli "${mvn} verify"
            }

            if (runSonar) {
                // Run the sonar analysis
                stage("$platform: Sonar") {
                    cli "${mvn} sonar:sonar"
                }
            }

            if (deploy) {
                // Deploy to repository
                // No need for tests as we already passed verify
                stage("$platform: Deploy") {
                    cli "${mvn} deploy -DskipTests"
                }
            }
        }
    }
}


/**
 * This function will delegate arguments to the platform specific command line interface.
 * @param args
 * @return void
 */
def cli(String args) {
    if (isUnix()) {
        sh args
    } else {
        bat args
    }
}
