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

def createBintrayVersion() {
    return "curl -fsS " +
            "-u '${env.BINTRAY_USR}:${env.BINTRAY_PSW}' " +
            "-X POST https://api.bintray.com/packages/xillio/Xill-Platform/Xill-CLI/versions " +
            "-H 'Content-Type: application/json' " +
            "-d '{\"name\": \"${env.MAVEN_VERSION}\"}' && " +
            "curl -fsS " +
            "-u '${env.BINTRAY_USR}:${env.BINTRAY_PSW}' " +
            "-X POST https://api.bintray.com/packages/xillio/Xill-Platform/Xill-IDE/versions " +
            "-H 'Content-Type: application/json' " +
            "-d '{\"name\": \"${env.MAVEN_VERSION}\"}'"
}

def uploadFileToBintray(String packageName, String file, String fileName) {
    return "curl -fsS -u \"${env.BINTRAY_USR}:${env.BINTRAY_PSW}\" " +
            "-X PUT " +
            "https://api.bintray.com/content/xillio/Xill-Platform/${packageName}/${env.MAVEN_VERSION}/${fileName}?publish=1 " +
            "-H \"Content-Type: application/json\" " +
            "-T \"${file}\" && " +
            "curl -fsS -u \"${env.BINTRAY_USR}:${env.BINTRAY_PSW}\" " +
            "-X PUT " +
            "https://api.bintray.com/file_metadata/xillio/Xill-Platform/${packageName}/${fileName} " +
            "-H \"Content-Type: application/json\" " +
            '-d "{\\\"list_in_downloads\\\":true}"'
}

def isRelease() {
    return env.MAVEN_VERSION.contains('SNAPSHOT');
}

pipeline {
    agent none
    parameters {
        booleanParam(name: 'NO_SONAR', defaultValue: false, description: 'Skip sonar analysis')
    }
    environment {
        BINTRAY = credentials("BINTRAY_LOGIN")
    }
    stages {
        stage('Build') {
            parallel {
                stage('Linux') {
                    agent {
                        dockerfile {
                            dir 'buildagent'
                            label 'docker && linux'
                            args '-u 0:0'
                        }
                    }
                    environment {
                        MAVEN_VERSION = readMavenPom().getVersion()
                    }
                    steps {
                        script {
                            configFileProvider([configFile(fileId: 'xill-platform/settings.xml', variable: 'MAVEN_SETTINGS')]) {
                                if (isRelease()) {
                                    sh createBintrayVersion()
                                    sh "mvn " +
                                            "-s ${MAVEN_SETTINGS} " +
                                            "-B  " +
                                            "deploy " +
                                            "--fail-at-end"
                                    sh uploadFileToBintray("Xill-IDE", "xill-ide/target/xill-ide-${env.MAVEN_VERSION}-multiplatform.zip", "xill-ide-${env.MAVEN_VERSION}-multiplatform.zip")
                                    sh uploadFileToBintray("Xill-CLI", "xill-cli/target/xill-cli-${env.MAVEN_VERSION}.zip", "xill-cli-${env.MAVEN_VERSION}.zip")
                                    sh uploadFileToBintray("Xill-CLI", "xill-cli/target/xill-cli-${env.MAVEN_VERSION}.tar.gz", "xill-cli-${env.MAVEN_VERSION}.tar.gz")
                                } else {
                                    sh "mvn " +
                                            "-s ${MAVEN_SETTINGS} " +
                                            "-B  " +
                                            "verify " +
                                            "--fail-at-end"
                                }
                            }
                        }
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
                    environment {
                        MAVEN_VERSION = readMavenPom().getVersion()
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
                        script {
                            if (isRelease()) {
                                bat uploadFileToBintray("Xill-IDE", "xill-ide-native/target/xill-ide-${env.MAVEN_VERSION}-win.zip", "xill-ide-${env.MAVEN_VERSION}-win.zip")
                            }
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true, testResults: '**/target/*-reports/*.xml'
                        }
                    }
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
