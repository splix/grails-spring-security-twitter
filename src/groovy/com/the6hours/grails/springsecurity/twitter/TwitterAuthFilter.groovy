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

/**
 * TODO
 *
 * @since 09.05.11
 * @author Igor Artamonov (http://igorartamonov.com)
 */
class TwitterAuthFilter extends AbstractAuthenticationProcessingFilter{

    TwitterAuthFilter(String url) {
        super(url)
    }

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        Twitter twitter = (Twitter) request.getSession().getAttribute("twitter")
        if (twitter == null) {
            //log.warn "Access twitter callback, but there are no twitter in session"
            return null
        }
        RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken")
        if (requestToken == null) {
            //log.warn "No requestToken for twitter callback"
            return null
        }
        String verifier = request.getParameter("oauth_verifier")
        if (StringUtils.isEmpty(verifier)) {
            //log.warn "Empty oauth_verifier"
            return null
        }
        try {
            AccessToken token = twitter.getOAuthAccessToken(requestToken, verifier)
            request.getSession().removeAttribute("requestToken")
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
            return auth
        } catch (TwitterException e) {
            //log.error "Failed processing twitter callback", e
        }
        return null
    }

}
