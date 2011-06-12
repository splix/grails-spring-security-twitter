security {

	twitter {

        app {
            id = "APP_ID"
            key = "APP_KEY"
            consumerKey = "CONSUMER_KEY"
            consumerSecret = "CONSUMER_KEY"
        }

        language = "en_US"
        button.text = "Login with Twitter"
        popup = true

        autoCreate {
            active = true
            roles = ['ROLE_USER', 'ROLE_TWITTER']
        }

        filter {
            processUrl = '/j_spring_twitter_security_check'
            processPopupUrl = '/twitterAuth/popup'
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