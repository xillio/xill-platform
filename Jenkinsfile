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

def mvn(args) {
    configFileProvider([configFile(fileId: 'xill-platform/settings.xml', variable: 'MAVEN_SETTINGS')]) {
        def command = "mvn -s \"${env.MAVEN_SETTINGS}\" -B ${args}";
        if (isUnix()) {
            sh command
        } else {
            cmd command
        }
    }
}

pipeline {
    agent none
    stages {
        stage('Build') {
            parallel {
                stage('Build on Linux') {
                    agent {
                        dockerfile {
                            dir 'buildagent'
                        }
                    }
                    steps {
                        mvn 'verify'
                    }
                }
            }
        }
    }
}
