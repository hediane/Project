def call (Map config)
{
    node
        { 
            stage('msg')
            {
                echo "checking out the source scmurl "
                echo "${config.scmurl}"
            }
           stage("testCheckout") {
            checkout([$class: 'GitSCM', 
            branches: [[name: 'refs/heads/main']], 
            userRemoteConfigs: [[
                //refspec: '+refs/tags/*:refs/remotes/origin/tags/*',
                url:"${config.scmurl}"]]
                //url: 'https://github.com/hediane/teeeeeest.git']]
        ])
         }
            /*stage('location of dockerfile') 
                {  
                    sh "docker build -t teeeeeest -f ${config.dockerfileLocation} ."
                    echo "checking out the source dockerfile "
                    //echo "${config.dockerfileLocation}",
                }*/
            stage('location of docker-compose') 
                {  
                    sh "${config.dockerComposeLocation} up -d"
                    echo "Buid Image with docker-compose "
                    //echo "${config.dockerfileLocation}",
                }
            stage('GetUserJenkins') 
                {  
                    wrap([$class: 'BuildUser']) {
                    def user = env.BUILD_USER_ID
                    echo "${user}"
                    //def list = ['AmaniGHADDAB']
                    echo "${config.devValidator}"
                    if ("${config.devValidator}".contains("${user}"))
                    {
                        echo"Validate"
                    }
                    else
                    {
                        post {
                            failure {
                                slackSend color: 'danger', channel: '#devops', message: "<${currentBuild.absoluteUrl}|Server build ${env.BUILD_NUMBER}> failed to deploy build "
                            }
			}
                        echo"Don't have access"
                    }
                 }
                }
                stage('Decide deploy to test servers') 
                {  
                    steps 
                    {
                        script {
                            DEPLOY_TO_TEST_SERVERS = input message: 'User input required',
                                    submitter: 'authenticated',
                                    parameters: [choice(name: 'Deploy to the test servers', choices: 'no\nyes', description: 'Choose "yes" if you want to deploy the test servers')]
                            if (DEPLOY_TO_TEST_SERVERS == 'no') {
                                slackSend color: 'warning', channel: '#devops', message: "<${currentBuild.absoluteUrl}|Server build ${env.BUILD_NUMBER}> skips all further steps"
                            }
                        }
			        }       
                }
            
    }

}