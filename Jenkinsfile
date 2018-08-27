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

def uploadFile(String file, String fileName) {
    return "curl -f -u '${env.BINTRAY_USR}:${env.BINTRAY_PSW}' " +
         "-X PUT https://api.bintray.com/content/xillio/Xill-Platform/DeployTest/${env.MAVEN_VERSION}/${fileName} " +
         "-H 'Content-Type: application/json' " +
         "-H 'X-Bintray-Package:DeployTest' " +
         "-H 'X-Bintray-Version:${env.MAVEN_VERSION}' " +
         "-T '${file}'"
}

pipeline {
    agent none
    parameters {
        booleanParam(name: 'NO_SONAR', defaultValue: false, description: 'Skip sonar analysis')
    }
    environment {
        MAVEN_VERSION = readMavenPom().getVersion()
        BINTRAY = credentials("BINTRAY_LOGIN")
    }
    stages {
        stage('Build on Linux') {
            agent {
                dockerfile {
                    dir 'buildagent'
                    label 'docker && linux'
                    args '-u 0:0'
                }
            }
            steps {
                sh "curl -f -u '${env.BINTRAY_USR}:${env.BINTRAY_PSW}' " +
                   "-X POST https://api.bintray.com/packages/xillio/Xill-Platform/DeployTest/versions " +
                   "-H 'Content-Type: application/json' " +
                   "-d '{\"name\": \"${env.MAVEN_VERSION}\"}'"
                   configFileProvider([configFile(fileId: 'xill-platform/settings.xml', variable: 'MAVEN_SETTINGS')]) {
                        sh "mvn " +
                              "-s ${MAVEN_SETTINGS} " +
                              "-B  " +
                              "verify " +
                              "--fail-at-end"

                   }
                   sh uploadFile("xill-ide/target/xill-ide-${env.MAVEN_VERSION}-multiplatform.zip", "xill-ide-${env.MAVEN_VERSION}-multiplatform.zip")
                   sh uploadFile("xill-cli/target/xill-cli-${env.MAVEN_VERSION}.zip", "xill-cli-${env.MAVEN_VERSION}.zip")
                   sh uploadFile("xill-cli/target/xill-cli-${env.MAVEN_VERSION}.tar.gz", "xill-cli-${env.MAVEN_VERSION}.tar.gz")
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/*-reports/*.xml'
                }
            }
        }
        stage('Windows') {
            agent {
                label 'windows && xill-platform'
            }
            steps {
                configFileProvider([configFile(fileId: 'xill-platform/settings.xml', variable: 'MAVEN_SETTINGS')]) {
                    bat "mvn " +
                        "-P build-native " +
                        "-s ${MAVEN_SETTINGS} " +
                        "-B  " +
                        "verify " +
                        "--fail-at-end"
                }
                bat uploadFile("xill-ide-native/target/xill-ide-${env.MAVEN_VERSION}-win.zip", "xill-ide-${env.MAVEN_VERSION}-win.zip")
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/*-reports/*.xml'
                }
            }
        }
        stage('Sonar Analysis') {
            agent {
                dockerfile {
                    dir 'buildagent'
                    label 'docker && linux'
                    args '-u 0:0'
                }
            }
            when {
                expression {
                    !params.NO_SONAR
                }
            }
            environment {
                SONARCLOUD_LOGIN = credentials('SONARCLOUD_LOGIN')
            }
            steps {
                configFileProvider([configFile(fileId: 'xill-platform/settings.xml', variable: 'MAVEN_SETTINGS')]) {
                    sh 'mvn -s "$MAVEN_SETTINGS" -B ' +
                            "-Dsonar.login='${env.SONARCLOUD_LOGIN}' " +
                            '-Dsonar.host.url=https://sonarcloud.io ' +
                            '-Dsonar.organization=xillio ' +
                            "-Dsonar.branch.name='${env.GIT_BRANCH}' " +
                            'sonar:sonar'
                }
            }
        }
    }
}
