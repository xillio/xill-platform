if ('master' == env.BRANCH_NAME || env.BRANCH_NAME ==~ /d+(\.(d+|x))+/) {
    println 'This commit is on the master or a release branch. A full test and deployment will be executed...'

    def nativeProfile = '-P build-native'

    parallel(
            "Windows": {
                buildOn(
                        platform: 'windows',
                        mavenArgs: nativeProfile,
                        buildPhase: 'deploy'
                )
            },

            "Linux": {
                buildOn(
                        platform: 'linux',
                        mavenArgs: nativeProfile,
                        // We only run sonar on a single node
                        runSonar: true,
                        buildPhase: 'deploy'
                )
            },
            
            "Mac OSX": {
                buildOn(
                        platform: 'mac',
                        mavenArgs: nativeProfile,
                        buildPhase: 'deploy'
                )
            }
    )

} else {
    println 'This commit is not on a release branch. Skipping deployment.'

    buildOn(
            platform: 'slave',
            runSonar: true,
            buildPhase: 'verify'
    )

}

/**
 * This function will configure a node to run a build.
 * @param platform the os to run on (node label)
 * @param runSonar set to true to run a sonar analysis
 * @param buildPhase the main phase that should run for this job
 * @param mavenArgs additional arguments to pass to maven
 * @return void
 */
def buildOn(Map args) {
    def platform = args.platform ?: 'linux'
    def runSonar = args.runSonar ?: false
    def mavenArgs = args.mavenArgs ?: ''
    def buildPhase = args.buildPhase ?: 'verify'

    if(runSonar) {
        buildPhase = "$buildPhase sonar:sonar"
    }

    node("xill-platform && ${platform}") {

        // Gather all required tools
        // Note the escaped quotes to make this work with spaces
        def m2Tool = tool 'mvn-3'
        def javaTool = tool 'java-1.8'

        if('mac' == platform) {
            // On mac we have to create a symlink because it is a hard requirement to have /Contents/Home in the
            // JAVA_HOME path.
            stage('Setup Build Environment on mac') {
                sh "rm -rf target && mkdir target && mkdir target/Contents && cp -R $javaTool target/Contents/Home"
                javaTool = "${pwd()}/target/Contents/Home"
            }
        }

        withEnv(["M2_HOME=$m2Tool", "JAVA_HOME=$javaTool"]) {

            // Inject maven settings file
            configFileProvider([configFile(fileId: 'xill-platform/settings.xml', variable: 'MAVEN_SETTINGS')]) {
                def mvnOptions = [
                        // Use the provided settings.xml
                        "-s \"$MAVEN_SETTINGS\"",
                        // Run in batch mode (headless)
                        "-B",
                        // Uncomment this to enable verbose builds
                        //"-X"
                ]

                def mvn = "\"$m2Tool/bin/mvn\" ${mvnOptions.join(' ')} $mavenArgs"

                // Check out scm
                stage("Checkout on $platform") {
                    checkout scm
                }

                // Run all tests
                stage("Run $buildPhase on $platform") {
                    cli "$mvn $buildPhase sonar:sonar"
                }

                // Clean the repository
                stage("Clean on $platform") {
                    cli "$mvn clean"
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
