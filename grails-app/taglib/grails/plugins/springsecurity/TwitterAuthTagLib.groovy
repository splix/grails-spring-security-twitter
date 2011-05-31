package grails.plugins.springsecurity

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import twitter4j.Twitter
import com.the6hours.grails.springsecurity.twitter.TwitterAuthFilter
import twitter4j.TwitterFactory
import twitter4j.auth.RequestToken

/**
 * Twitter Auth tags
 *
 * @since 03.05.11
 * @author Igor Artamonov (http://igorartamonov.com)
 */
class TwitterAuthTagLib {

    static namespace = 'twitterAuth'

	/** Dependency injection for springSecurityService. */
	def springSecurityService

	def button = { attrs, body ->
        def conf = SpringSecurityUtils.securityConfig.twitter
        String apiKey = conf.app.key
        String authFilter = conf.filter.processUrl
        String logoutUrl = SpringSecurityUtils.securityConfig.logout.filterProcessesUrl

        TwitterFactory factory = new TwitterFactory()
        Twitter twitter = factory.getInstance()
        twitter.setOAuthConsumer(conf.app.consumerKey, conf.app.consumerSecret)

        RequestToken requestToken = session[TwitterAuthFilter.REQUEST_TOKEN]
        if (requestToken == null) {
            String callbackUrl = g.resource(file: authFilter, absolute: true)
            requestToken = twitter.getOAuthRequestToken(callbackUrl)
            session[TwitterAuthFilter.REQUEST_TOKEN] = requestToken
        }

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
        out << '<script type="text/javascript">'
        out << '   function twitterConnect() {'
        out << "     document.window.href = '$authUrl'';"
        out << '   }'
        out << '</script>'
    }
}
