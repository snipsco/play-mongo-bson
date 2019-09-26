node('jenkins-slave-generic') {

    def jenkinsHome = "/home/shared/play-mongo-bson"
    def sbtExtras = "$jenkinsHome/.sbt-extras/sbt"
    def sbtParams = "-Dsbt.log.noformat=true -Dsbt.ivy.home=$jenkinsHome/.sbt_ivy2 -Divy-home=$jenkinsHome/.ivy2"

    stage('Checkout') {
        checkout scm
    }

    stage('Sbt extras') {
        if (!fileExists(sbtExtras)) {
            sh "curl --create-dirs -s -o $sbtExtras https://raw.githubusercontent.com/paulp/sbt-extras/master/sbt && chmod 0755 $sbtExtras"
        }
    }

    stage('Lib: test') {
        sh "$sbtExtras $sbtParams 'project lib' clean +test"

        echo 'Archiving Tests Report'
        junit '**/test-reports/*.xml'
    }

    stage('Sample: compile') {
        sh "$sbtExtras $sbtParams 'project sample' clean +compile"
    }

    stage('Lib: deploy snapshot') {
        if (env.BRANCH_NAME == "master") {
            sh "$sbtExtras $sbtParams +publish"
        }
    }
}
