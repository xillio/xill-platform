
node('linux') {
    stage('Setup') {
        def mvnHome = tool 'mvn-3.3.9'
        def mvn = "${mvnHome}/bin/mvn"
    }

    stage('Checkout') {
        checkout scm
    }

    stage('Clean') {
        sh "${mvn} clean"
    }

    stage('Tests') {
        sh "${mvn} verify"
    }

    stage('Sonar') {
        sh "${mvn} sonar:sonar"
    }

    stage('Deploy') {
        sh "${mvn} deploy"
    }
}