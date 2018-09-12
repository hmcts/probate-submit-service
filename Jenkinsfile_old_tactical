#!groovy
properties(
        [[$class: 'GithubProjectProperty', projectUrlStr: 'https://github.com/hmcts/probate-submit-service.git'],
        parameters([ 
            string(description: 'Store RPM variable for branches than master or develop (other than "no" stores rpm)', defaultValue: 'no', name: 'store_rpm'),
            string(description: 'Store docker from Branches other than master (other than "no" create docker)', defaultValue: 'no', name: 'create_docker')
        ]),
         pipelineTriggers([[$class: 'GitHubPushTrigger']])]
)

//@Library(['Reform', 'PRO'])
@Library('Reform')
import uk.gov.hmcts.Ansible
import uk.gov.hmcts.Packager
import uk.gov.hmcts.Versioner
import uk.gov.hmcts.RPMTagger

def ansible = new Ansible(this, 'probate')
def packager = new Packager(this, 'probate')
def versioner = new Versioner(this)

def rpmTagger
def app = "submit-service"
def artifactorySourceRepo = "probate-local"

node {

    try {        
        def storeRPMToArtifactory = false
        def newAppVersion
        def version
        def probateBackendSubmitServiceRPMVersion

        if(store_rpm != 'no' || "master"  == "${env.BRANCH_NAME}" || "develop"  == "${env.BRANCH_NAME}") {
            storeRPMToArtifactory = true
        }

        stage('Checkout') {
            deleteDir()
            checkout scm
            dir('ansible-management') {
                git url: "https://github.com/hmcts/ansible-management", branch: "master", credentialsId: "jenkins-public-github-api-token"
            }
        }

        if ("master"  != "${env.BRANCH_NAME}") {
            newAppVersion = "-SNAPSHOT"
            if("develop"  != "${env.BRANCH_NAME}") {
                newAppVersion = "-${env.BRANCH_NAME}-SNAPSHOT"
            }
            echo "${newAppVersion}"
            stage('Add SNAPSHOT using SED') {
                sh """ 
                    sed -i '/version/ s/"/${newAppVersion}"/2' build.gradle
                """
            }
        }

        stage('Test (Unit)') {
            sh "./gradlew test"
        }

        stage('Gradle Build') {
            versioner.addJavaVersionInfo()
            sh "./gradlew clean build  -x dependencyCheckAnalyze"
        }

        try {
            stage('Gradle dependency Check Analyze') {
                sh "./gradlew build"
                archiveArtifacts  'build/reports/dependency-check-report.html'
            }
        } catch (err) {
            sh 'echo "Ignore Dependency Check Analyzer"' 
        }

        stage('publish Test & findbugs & OWASP Report') {
            archive "build/reports/tests/*/*"
            archiveArtifacts  'build/reports/jacoco/index.html'        
            archiveArtifacts  'build/reports/findbugs/main.html'
            archiveArtifacts  'build/reports/findbugs/test.html'
        }

        try {
            stage('sonar') {
                sh './gradlew sonarqube'
            }
        }  catch (err) {
            sh 'echo "Ignore Sonar Cube Error"' 
        }

        if(create_docker != 'no' || "master"  == "${env.BRANCH_NAME}") {
            stage('Package (Docker)') {
                //submitServiceVersion = dockerImage imageName: 'probate/submit-service'
                //println(submitServiceVersion)
            }
        }

        if(storeRPMToArtifactory) {
            stage('Package (RPM)') {
                probateBackendSubmitServiceRPMVersion = packager.javaRPM( app, 'build/libs/submit-service.jar',
                        'springboot', 'src/main/resources/application.yml')
                sh "echo $probateBackendSubmitServiceRPMVersion"
                version = "{probate_submit_buildnumber: ${probateBackendSubmitServiceRPMVersion} }"
                sh "echo $version"
                def rpmName = packager.rpmName(app, probateBackendSubmitServiceRPMVersion)
                sh "echo $rpmName"
                rpmTagger = new RPMTagger(this, app, rpmName, artifactorySourceRepo)
                packager.publishJavaRPM(app)
            }
        }

        if ("master"  == "${env.BRANCH_NAME}") {

            stage('Install (Test)') {
                ansible.runInstallPlaybook(version, 'test')
            }

            stage('Deploy (Test)') {
                ansible.runDeployPlaybook(version, 'test')
            }

            stage('Tag Deploy success (Test)') {
                rpmTagger.tagDeploymentSuccessfulOn('test')
            }

            stage('Smoke Test') {
                ws('workspace/probate-frontend/build') {
                    env.PROBATE_FRONTEND_URL = "https://www-" + 'test' + ".probate.reform.hmcts.net/"
                    git url: 'git@git.reform.hmcts.net:probate/smoke-tests.git'
                    sh '''
                        npm install
                        npm run test-service -- --ENV test --SERVICE submit-service
                    '''
                    deleteDir()
                }
            }

            stage('Tag Smoke Test success (Test)') {
                rpmTagger.tagTestingPassedOn('test')
            }
        }
        deleteDir()
    } catch (err) {
        deleteDir()
        slackSend(
                channel: '#probate-jenkins',
                color: 'danger',
                message: "${env.JOB_NAME}:  <${env.BUILD_URL}console|Build ${env.BUILD_DISPLAY_NAME}> has FAILED probate submit service")
        throw err

    }
}
