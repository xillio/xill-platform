def branchName = env.BRANCH_NAME
def nativeProfile = '-P build-native'

if ('master'.equals(branchName) || branchName ==~ /d+\.d+\.d+/) {
    println 'This commit is on the master or a release branch. A full test and deployment will be executed...'

    parallel(
            "Windows": {
                buildOn(
                        platform: "windows",
                        mavenArgs: nativeProfile,
                        deploy: true
                )
            },

            "Linux": {
                buildOn(
                        platform: "linux",
                        mavenArgs: nativeProfile,
                        deploy: true
                )
            },

            "Mac OSX": {
                buildOn(
                        platform: "mac",
                        mavenArgs: nativeProfile,
                        runSonar: true,
                        deploy: true
                )
            }
    )

} else {
    println 'This commit is not on a release branch. Skipping deployment.'

    buildOn(
            platform: 'slave',
            runSonar: true
    )

}

/**
 * This function will configure a node to run a build.
 * @param platform the os to run on (node label)
 * @param runSonar set to true to run a sonar analysis
 * @param deploy set to true to deploy to maven repository
 * @param mavenArgs additional arguments to pass to maven
 * @return void
 */
def buildOn(Map args) {
    def platform = args.platform ?: 'linux'
    def runSonar = args.runSonar ?: false
    def deploy = args.deploy ?: false
    def mavenArgs = args.mavenArgs ?: ''

    node("xill-platform && ${platform}") {

        // Gather all required tools
        // Note the escaped quotes to make this work with spaces
        def m2Tool = tool 'mvn-3'
        env.M2_HOME = m2Tool
        def javaTool = tool 'java-1.8'

        withEnv(["JAVA_HOME=$javaTool"]) {

            // Inject maven settings file
            configFileProvider([configFile(fileId: 'xill-platform/settings.xml', variable: 'MAVEN_SETTINGS')]) {
                def mvnOptions = [
                        // Use the provided settings.xml
                        "-s \"$MAVEN_SETTINGS\"",
                        // Run in batch mode (headless)
                        "-B"
                ]

                def mvn = "\"${m2Tool}/bin/mvn\" ${mvnOptions.join(' ')} ${mavenArgs}"

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
                        cli "${mvn} sonar:sonar -Dsonar.branch=${branchName}"
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
