package com.the6hours.grails.springsecurity.twitter

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import twitter4j.Twitter
import com.the6hours.grails.springsecurity.twitter.TwitterAuthFilter
import twitter4j.TwitterFactory
import twitter4j.auth.RequestToken
import org.apache.log4j.Logger

/**
 * Twitter Auth tags
 *
 * @since 03.05.11
 * @author Igor Artamonov (http://igorartamonov.com)
 */
class TwitterAuthTagLib {

    private static def log = Logger.getLogger(this)

    static namespace = 'twitterAuth'

	/** Dependency injection for springSecurityService. */
	def springSecurityService

	def button = { attrs, body ->
        def conf = SpringSecurityUtils.securityConfig.twitter

//        String authFilter = conf.popup ? conf.filter.processPopupUrl : conf.filter.processUrl
        String authFilter = conf.filter.processUrl

        TwitterFactory factory = new TwitterFactory()
        Twitter twitter = factory.getInstance()
        twitter.setOAuthConsumer(conf.app.consumerKey, conf.app.consumerSecret)

        RequestToken requestToken = session.getAttribute(TwitterAuthFilter.REQUEST_TOKEN)
        if (requestToken == null) {
            println "Prepare new requestToken, put as " + TwitterAuthFilter.REQUEST_TOKEN
            String callbackUrl = g.resource(file: authFilter, absolute: true)
            requestToken = twitter.getOAuthRequestToken(callbackUrl)
            session.setAttribute(TwitterAuthFilter.REQUEST_TOKEN, requestToken)
        } else {
            println "Reusing existing requestToken"
        }

        println "Request Token: " + session.getAttribute(TwitterAuthFilter.REQUEST_TOKEN)

        String authUrl = requestToken.authenticationURL
        String text = "Connect with Twitter"

        out << '<span class="twitter-login">'
        out << '<a href="'
        out << authUrl
        out << '" class="twitter-button" title="'
        out << text
        out << '" onclick="twitterConnect(); return false;"><span>'
        out << text
        out << '</span></a></span>'

        if (conf.popup) {
            String successUrl = SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
            out << '<script type="text/javascript">'
            out << '   function twitterConnect() {'
            out << "     window.open('$authUrl', 'twitter_auth', 'width=640,height=500,toolbar=no,directories=no,status=no,menubar=no,copyhistory=no');"
            out << '   }'
            out << '   function loggedIn() {'
            out << "     window.href = '$successUrl';"
            out << '   }'
            out << '</script>'
        } else {
            out << '<script type="text/javascript">'
            out << '   function twitterConnect() {'
            out << "     window.href = '$authUrl'';"
            out << '   }'
            out << '</script>'
        }
    }
}
