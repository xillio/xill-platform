/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
if ('master' == env.BRANCH_NAME || env.BRANCH_NAME ==~ /d+(\.(d+|x))+/) {
    println 'This commit is on the master or a release branch. A full test and deployment will be executed...'

    String nativeProfile = '-P build-native'

    currentBuild.displayName = "${env.BRANCH_NAME}: ${currentBuild.number}"

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
                        buildPhase: 'deploy'
                )
            },

            "Mac OSX": {
                buildOn(
                        platform: 'mac',
                        mavenArgs: nativeProfile,
                        // We only run sonar on a single node
                        runSonar: true,
                        buildPhase: 'deploy'
                )
            }
    )

} else {
    println 'This commit is not on a release branch. Skipping deployment.'

    String issueNumber = getIssueNumberFromBranchName()

    if(issueNumber != null) {
        currentBuild.displayName = "${issueNumber}: ${currentBuild.number}"
    }

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
void buildOn(Map args) {
    String platform = args.platform ?: 'linux'
    boolean runSonar = args.runSonar ?: false
    String mavenArgs = args.mavenArgs ?: ''
    String buildPhase = args.buildPhase ?: 'verify'

    if (runSonar) {
        buildPhase = "$buildPhase sonar:sonar"
    }

    node("xill-platform && ${platform}") {

        // Gather all required tools
        // Note the escaped quotes to make this work with spaces
        String m2Tool = tool 'mvn-3'
        String javaTool = tool 'java-1.8'

        if ('mac' == platform) {
            // On mac we have to create a symlink because it is a hard requirement to have Contents/Home in the
            // JAVA_HOME path.
            sh "rm -rf target && mkdir target && mkdir target/Contents && cp -R $javaTool target/Contents/Home"
            javaTool = "${pwd()}/target/Contents/Home"
        }

        withEnv(["M2_HOME=$m2Tool", "JAVA_HOME=$javaTool"]) {

            // Inject maven settings file
            configFileProvider([configFile(fileId: 'xill-platform/settings.xml', variable: 'MAVEN_SETTINGS')]) {
                String[] mvnOptions = [
                        // Use the provided settings.xml
                        "-s \"$MAVEN_SETTINGS\"",
                        // Run in batch mode (headless)
                        "-B",
                        // Pass the sonar url
                        "-Dsonar.host.url=https://sonaross.xillio.com",
                        // Uncomment this to enable verbose builds
                        "-X"
                ]

                String mvn = "\"$m2Tool/bin/mvn\" ${mvnOptions.join(' ')} $mavenArgs"

                // Run the build and clean
                stage("Run 'mvn $buildPhase' on $platform") {
                    checkout scm
                    cli "$mvn $buildPhase -Dsonar.branch=${env.BRANCH_NAME}"
                    cli "$mvn clean"
                }
            }
        }
    }
}

/**
 * This function will extract the issue number from the current branch name. If it cannot be extracted it will
 * return null.
 * @return the issue id or null
 */
String getIssueNumberFromBranchName() {
    String branchName = env.BRANCH_NAME
    String[] parts = branchName.split('-')

    if(parts.length < 3) {
        // This does not have the format: XXXX-1234-name
        return null;
    }

    if(!parts[1].isInteger()) {
        // The second part is not an issue number
        return null;
    }

    return "${parts[0]}-${parts[1]}"
}

/**
 * This function will delegate arguments to the platform specific command line interface.
 * @param args
 * @return void
 */
void cli(String args) {
    if (isUnix()) {
        sh args
    } else {
        bat args
    }
}
