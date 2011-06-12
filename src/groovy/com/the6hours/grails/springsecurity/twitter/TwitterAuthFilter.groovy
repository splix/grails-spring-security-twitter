package com.the6hours.grails.springsecurity.twitter

import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.core.Authentication
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import twitter4j.Twitter
import twitter4j.auth.RequestToken
import org.apache.commons.lang.StringUtils
import twitter4j.auth.AccessToken
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import org.apache.log4j.Logger

/**
 * TODO
 *
 * @since 09.05.11
 * @author Igor Artamonov (http://igorartamonov.com)
 */
class TwitterAuthFilter extends AbstractAuthenticationProcessingFilter {

    private static def log = Logger.getLogger(this)

    public static final String PREFIX = "twitterAuth."
    public static final String REQUEST_TOKEN = PREFIX + "requestToken"

    TwitterFactory factory = new TwitterFactory()
    String consumerKey
    String consumerSecret
//    String filterPopupUrl
//    boolean popup

    TwitterAuthFilter(String url) {
        super(url)
    }

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        log.debug "TwitterAuthFilter auth"

        RequestToken requestToken = (RequestToken) request.getSession().getAttribute(REQUEST_TOKEN)
        if (requestToken == null) {
            log.warn "No requestToken for twitter callback at " + REQUEST_TOKEN
            return null
        }
        String verifier = request.getParameter("oauth_verifier")
        if (StringUtils.isEmpty(verifier)) {
            log.warn "Empty oauth_verifier"
            return null
        }

        Twitter twitter = factory.getInstance()
        twitter.setOAuthConsumer(consumerKey, consumerSecret)

        try {
            AccessToken token = twitter.getOAuthAccessToken(requestToken, verifier)
            request.getSession().removeAttribute(REQUEST_TOKEN)
            TwitterAuthToken securityToken = new TwitterAuthToken(
                    userId: token.userId,
                    screenName: token.screenName,
                    tokenSecret: token.tokenSecret,
                    token: token.token
            )
            securityToken.authenticated = true
            Authentication auth = getAuthenticationManager().authenticate(securityToken)
            if (auth.authenticated) {
                rememberMeServices.loginSuccess(request, response, auth)
            }
            log.info "Successful authentication"
            return auth
        } catch (TwitterException e) {
            log.error "Failed processing twitter callback", e
        }
        log.info "Authentication failed"
        TwitterAuthToken auth = new TwitterAuthToken()
        auth.authenticated = false
        return auth
    }

    protected boolean _requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        if (super.requiresAuthentication(request, response)) {
            return true
        }
        if (!popup) {
            return false
        }
        String uri = request.getRequestURI();
        int pathParamIndex = uri.indexOf(';');

        if (pathParamIndex > 0) {
            // strip everything after the first semi-colon
            uri = uri.substring(0, pathParamIndex);
        }

        if ("".equals(request.getContextPath())) {
            return uri.endsWith(filterProcessesUrl);
        }

        return uri.endsWith(request.getContextPath() + filterPopupUrl);
    }


}
