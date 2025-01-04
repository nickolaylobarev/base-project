/**
 * Jenkins CI/CD configuration
 * To run tests in the MAIN repository pipeline, copy this file to the root and set the environment variables in Jenkins:
 * `TEST_BRANCH_NAME` - branch name in the MAIN repository
 * `TEST_REPO_URL` - URL of THIS repository
 */

task_branch = "${TEST_BRANCH_NAME}"
def cutted_branch = task_branch.contains("origin") ? task_branch.split('/')[1] : task_branch.trim()
currentBuild.displayName = "$cutted_branch"
base_git_url = "${TEST_REPO_URL}"

node {
    withEnv(["branch=${cutted_branch}", "base_url=${base_git_url}"]) {
        stage("Checkout Branch") {
            try {
                getProject("$base_git_url", "$cutted_branch")
            } catch (e) {
                echo "Failed to get branch $cutted_branch"
                throw ("${e}")
            }
        }

        try {
            stage("Smoke Tests") {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    sh """
                        chmod +x gradlew
                        ./gradlew clean :api:test -PincludeTags="Smoke"
                    """
                }
            }

            stage("Integration and E2E Tests") {
                parallel(
                        'Integration Tests': {
                            catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                                sh """
                            chmod +x gradlew
                            ./gradlew clean :api:test -PincludeTags="Integration" -PexcludeTags="Smoke"
                        """
                            }
                        },
                        'E2E Tests': {
                            catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                                sh """
                            chmod +x gradlew
                            ./gradlew clean :api:test -PincludeTags="E2E"
                        """
                            }
                        }
                )
            }
        } finally {
            stage("Allure Report") {
                allure([
                        includeProperties: true,
                        jdk              : '',
                        properties       : [],
                        reportBuildPolicy: 'ALWAYS',
                        results          : [[path: '.allure-results']]
                ])
            }
        }
    }
}

def getProject(String repo, String branch) {
    cleanWs()
    checkout scm: [
            $class           : 'GitSCM', branches: [[name: branch]],
            userRemoteConfigs: [[
                                        url: repo
                                ]]
    ]
}
