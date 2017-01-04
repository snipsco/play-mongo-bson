node('jenkins-slave-generic') {

    def sbtExtras = "/home/shared/play-mongo-bson/.sbt-extras/sbt"

    stage('Checkout') {
        checkout scm
    }

    stage('Sbt extras') {
        if (!fileExists(sbtExtras)) {
            sh "curl --create-dirs -s -o $sbtExtras https://raw.githubusercontent.com/paulp/sbt-extras/master/sbt && chmod 0755 $sbtExtras"
        }
    }

    stage('Lib: test') {
        sh "cd lib; $sbtExtras -Dsbt.log.noformat=true -Dsbt.ivy.home=/home/shared/play-mongo-bson/.sbt_ivy2 -Divy-home=/home/shared/play-mongo-bson/.ivy2 clean test"

        echo 'Archiving Tests Report'
        junit '**/test-reports/*.xml'
    }

    stage('Lib: deploy snapshot') {
        if (env.BRANCH_NAME == "master") {
            sh "cd lib; $sbtExtras -Dsbt.log.noformat=true -Dsbt.ivy.home=/home/shared/play-mongo-bson/.sbt_ivy2 -Divy-home=/home/shared/play-mongo-bson/.ivy2 publish"
        }
    }

    stage('Sample: compile') {
        sh "cd sample; $sbtExtras -Dsbt.log.noformat=true -Dsbt.ivy.home=/home/shared/play-mongo-bson/.sbt_ivy2 -Divy-home=/home/shared/play-mongo-bson/.ivy2 clean compile"
    }
}