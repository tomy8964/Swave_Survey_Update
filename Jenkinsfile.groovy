pipeline {
    agent any
    
    stages() {
        stage('git clone') {
            steps() {
                slackSend (
                        channel: '#jenkins',
                        color: '#FFFF00',
                        message: "STARTED: Job ${env.JOB_NAME}"
                )
                git branch: 'master', credentialsId: 'git-hamgeonwook', url: 'https://github.com/tomy8964/Swave_Survey_Update/'
            }
        }
        
        stage('Build User') {
            steps {
                dir('user') {
                    sh "chmod +x gradlew"
                    sh "./gradlew clean bootJar"
                    script{
                        image = docker.build("hamgeonwook/user-back")
                        docker.withRegistry('https://registry.hub.docker.com/repository/docker/hamgeonwook/user-back/', 'docker-hub-credentials') {
                            image.push("${env.BUILD_NUMBER}")
                        }
                    }
                }
            }
            post {
                success {
                    slackSend (
                            channel: '#jenkins',
                            color: '#00FF00',
                            message: "SUCCESS: Job ${env.JOB_NAME} Build User"
                    )
                }
                failure {
                    slackSend (
                            channel: '#jenkins',
                            color: '#FF0000',
                            message: "FAIL: Job ${env.JOB_NAME} Build User"
                    )
                }
            }
        }

        stage('Build Surveydocument') {
            steps {
                dir('surveydocument') {
                    sh "chmod +x gradlew"
                    sh "./gradlew clean bootJar"
                    script{
                        image = docker.build("hamgeonwook/survey-back")
                        docker.withRegistry('https://registry.hub.docker.com/repository/docker/hamgeonwook/survey-back/', 'docker-hub-credentials') {
                            image.push("${env.BUILD_NUMBER}")
                        }
                    }
                }
            }
            post {
                success {
                    slackSend (
                            channel: '#jenkins',
                            color: '#00FF00',
                            message: "SUCCESS: Job ${env.JOB_NAME} Build Surveydocument"
                    )
                }
                failure {
                    slackSend (
                            channel: '#jenkins',
                            color: '#FF0000',
                            message: "FAIL: Job ${env.JOB_NAME} Build Surveydocument"
                    )
                }
            }
        }

        stage('Build Surveyanswer') {
            steps {
                dir('surveyanswer') {
                    sh "chmod +x gradlew"
                    sh "./gradlew clean bootJar"
                    script{
                        image = docker.build("hamgeonwook/response-back")
                        docker.withRegistry('https://registry.hub.docker.com/repository/docker/hamgeonwook/response-back/', 'docker-hub-credentials') {
                            image.push("${env.BUILD_NUMBER}")
                        }
                    }
                }
            }
            post {
                success {
                    slackSend (
                            channel: '#jenkins',
                            color: '#00FF00',
                            message: "SUCCESS: Job ${env.JOB_NAME} Build Surveyanswer"
                    )
                }
                failure {
                    slackSend (
                            channel: '#jenkins',
                            color: '#FF0000',
                            message: "FAIL: Job ${env.JOB_NAME} Build Surveyanswer"
                    )
                }
            }
        }

        stage('Build Surveyanalyze') {
            steps {
                dir('surveyanalyze') {
                    sh "chmod +x gradlew"
                    sh "./gradlew clean bootJar"
                    script{
                        image = docker.build("hamgeonwook/analysis-back")
                        docker.withRegistry('https://registry.hub.docker.com/repository/docker/hamgeonwook/analysis-back/', 'docker-hub-credentials') {
                            image.push("${env.BUILD_NUMBER}")
                        }
                    }
                }
            }
            post {
                success {
                    slackSend (
                            channel: '#jenkins',
                            color: '#00FF00',
                            message: "SUCCESS: Job ${env.JOB_NAME} Build Surveyanalyze"
                    )
                }
                failure {
                    slackSend (
                            channel: '#jenkins',
                            color: '#FF0000',
                            message: "FAIL: Job ${env.JOB_NAME} Build Surveyanalyze"
                    )
                }
            }
        }

        stage('Build SpringGateWay') {
            steps {
                dir('apigateway-service-master') {
                    sh "chmod +x gradlew"
                    sh "./gradlew clean bootJar"
                    script{
                        image = docker.build("hamgeonwook/gateway")
                        docker.withRegistry('https://registry.hub.docker.com/repository/docker/hamgeonwook/gateway/', 'docker-hub-credentials') {
                            image.push("${env.BUILD_NUMBER}")
                        }
                    }
                }
            }
            post {
                success {
                    slackSend (
                            channel: '#jenkins',
                            color: '#00FF00',
                            message: "SUCCESS: Job ${env.JOB_NAME} Build SpringGateWay"
                    )
                }
                failure {
                    slackSend (
                            channel: '#jenkins',
                            color: '#FF0000',
                            message: "FAIL: Job ${env.JOB_NAME} Build SpringGateWay"
                    )
                }
            }
        }

        stage('AgroCD Manifest Update') {
            steps {
                git credentialsId: 'git-hamgeonwook',
                        url: 'https://github.com/tomy8964/argocd-back',
                        branch: 'main'
                dir('apps') {
                    sh "sed -i 's/user-back:.*\$/user-back:${currentBuild.number}/g' user.yaml"
                    sh "git add user.yaml"
                    sh "sed -i 's/survey-back:.*\$/survey-back:${currentBuild.number}/g' survey.yaml"
                    sh "git add survey.yaml"
                    sh "sed -i 's/response-back:.*\$/response-back:${currentBuild.number}/g' response.yaml"
                    sh "git add response.yaml"
                    sh "sed -i 's/analysis-back:.*\$/analysis-back:${currentBuild.number}/g' analysis.yaml"
                    sh "git add analysis.yaml"
                    sh "sed -i 's/gateway:.*\$/gateway:${currentBuild.number}/g' gateway.yaml"
                    sh "git add gateway.yaml"
                    
                    sshagent(credentials: ['git-ssh']) {
                        sh "git commit -m '[UPDATE] v${currentBuild.number} image versioning'"
                        sh "git remote set-url origin git@github.com:tomy8964/argocd-back.git"
                        sh "git push -u origin main"
                    }
                }
            }
            post {
                success {
                    slackSend (
                            channel: '#jenkins',
                            color: '#00FF00',
                            message: "SUCCESS: Job ${env.JOB_NAME} AgroCD Manifest Update"
                    )
                }
                failure {
                    slackSend (
                            channel: '#jenkins',
                            color: '#FF0000',
                            message: "FAIL: Job ${env.JOB_NAME} AgroCD Manifest Update"
                    )
                }
            }
        }
    }
    post {
        success {
            slackSend (
                    channel: '#jenkins',
                    color: '#00FF00',
                    message: "SUCCESS: Job ${env.JOB_NAME}"
            )
        }
        failure {
            slackSend (
                    channel: '#jenkins',
                    color: '#FF0000',
                    message: "FAIL: Job ${env.JOB_NAME}"
            )
        }
    }
}