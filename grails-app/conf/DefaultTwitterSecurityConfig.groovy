security {

	twitter {

        appId = "APP_ID"
        apiKey = "API_KEY"

        language = "en_US"
        button.text = "Login with Twitter"

        filter {
            processUrl = '/j_spring_twitter_security_check'
        }

        registration {
            autocreate = true
            createAccountUri = '/login/twitterCreateAccount'
            roleNames = ['ROLE_USER']
        }

        beans {
            filter = "twitterAuthFilter"
            provider = "twitterAuthProvider"
        }


        domain {
            classname = 'TwitterUser'
            connectionPropertyName = 'user'
        }
    }
}