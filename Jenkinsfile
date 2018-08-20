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

pipeline {
    agent any
    parameters {
        booleanParam(name: 'NO_SONAR', defaultValue: false, description: 'Skip sonar analysis')
        booleanParam(name: 'BUILD_NATIVE', defaultValue: false, description: 'Build a native distribution')
    }
    stages {
        stage('Build') {
            parallel {
                stage('Linux') {
                    agent {
                        dockerfile {
                            dir 'buildagent'
                            label 'docker && linux'
                        }
                    }
                    steps {
                        configFileProvider([configFile(fileId: 'xill-platform/settings.xml', variable: 'MAVEN_SETTINGS')]) {
                            sh "mvn " +
                                    "${params.BUILD_NATIVE ? '-P build-native' : ''} " +
                                    "-s ${env.MAVEN_SETTINGS} " +
                                    "-B  " +
                                    "verify " +
                                    "--fail-at-end"
                        }
                    }
                    post {
                        success {
                            archiveArtifacts allowEmptyArchive: true, artifacts: 'xill-ide/target/xill-ide-*-multiplatform.zip'
                            archiveArtifacts allowEmptyArchive: true, artifacts: 'xill-cli/target/xill-cli-*.zip'
                            archiveArtifacts allowEmptyArchive: true, artifacts: 'xill-cli/target/xill-cli-*.tar.gz'
                        }
                        always {
                            junit allowEmptyResults: true, testResults: '**/target/*-reports/*.xml'
                        }
                    }
                }
                stage('Windows') {
                    agent {
                        node {
                            label 'windows && xill-platform'
                        }
                    }
                    steps {
                       configFileProvider([configFile(fileId: 'xill-platform/settings.xml', variable: 'MAVEN_SETTINGS')]) {
                           bat "mvn " +
                                   "${params.BUILD_NATIVE ? '-P build-native' : ''} " +
                                   "-s ${env.MAVEN_SETTINGS} " +
                                   "-B  " +
                                   "verify " +
                                   "--fail-at-end"
                           bat "dir xill-ide\\target"
                           bat "dir xill-cli\\target"
                       }
                    }
                    post {
                        success {
                            archiveArtifacts allowEmptyArchive: true, artifacts: 'xill-ide-native\target\xill-ide-*-win.zip'
                        }
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
