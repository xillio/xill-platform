if ('master'.equals(env.BRANCH_NAME) || env.BRANCH_NAME ==~ /d+\.d+\.d+/ || true) {
    println 'This commit is on the master or a release branch. A full test and deployment will be executed...'

    def nativeProfile = '-P build-native'

    parallel(
//            "Windows": {
//                buildOn(
//                        platform: "windows",
//                        mavenArgs: nativeProfile,
//                        deploy: true
//                )
//            },
//
//            "Linux": {
//                buildOn(
//                        platform: "linux",
//                        mavenArgs: nativeProfile,
//                        deploy: true
//                )
//            },

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
        def javaTool = tool 'java-1.8'

        withEnv(buildEnvironmentalVariableList(m2Tool, javaTool, platform)) {

            // Inject maven settings file
            configFileProvider([configFile(fileId: 'xill-platform/settings.xml', variable: 'MAVEN_SETTINGS')]) {
                def mvnOptions = [
                        // Use the provided settings.xml
                        "-s \"$MAVEN_SETTINGS\"",
                        // Run in batch mode (headless)
                        "-B",
                        // Uncomment this to enable verbose builds
                        "-X"
                ]

                def mvn = "\"${m2Tool}/bin/mvn\" ${mvnOptions.join(' ')} ${mavenArgs}"

                if('mac'.equals(platform)) {
                    sh 'rm -rf Contents && mkdir Contents && ln -s $JAVA_HOME Contents/Home'
                    sh 'export JAVA_HOME=`pwd`/Contents/Home'
                }

                // Check out scm
                stage("Checkout on $platform") {
                    checkout scm
                }

                // Clean the repository
                stage("Clean on $platform") {
                    cli "${mvn} clean"
                }

                // Run all tests
                stage("Tests on $platform") {
                    cli "${mvn} verify"
                }

                if (runSonar) {
                    // Run the sonar analysis
                    stage("Sonar on $platform") {
                        cli "${mvn} sonar:sonar -Dsonar.branch=${env.BRANCH_NAME}"
                    }
                }

                if (deploy) {
                    // Deploy to repository
                    // No need for tests as we already passed verify
                    stage("Deploy on $platform") {
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
