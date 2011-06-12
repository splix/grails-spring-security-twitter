package com.the6hours.grails.springsecurity.twitter

import com.the6hours.grails.springsecurity.twitter.TwitterAuthFilter
import twitter4j.auth.RequestToken
import org.apache.log4j.Logger

class TwitterAuthController {

    private static def log = Logger.getLogger(this)

    /**
     * Dependency injection for the springSecurityService.
     */
    def springSecurityService

    def popup = {
        log.debug "Show popup"
        RequestToken requestToken = session[TwitterAuthFilter.REQUEST_TOKEN]
        if (!requestToken) {
            log.warn('No requestToken')
            //TODO
        }
        if (springSecurityService.isLoggedIn()) {
            log.debug "Is loggedIn"
            render controller: 'twitterAuth', view: 'popupOk', model: []
        } else {
            log.debug "Not loggedIn"
            String authUrl = requestToken.authenticationURL
            redirect url: authUrl
        }
    }

    def closePopup = {
        if (springSecurityService.isLoggedIn()) {
            render view: 'popupOk',
                    model: []
        } else {
            render view: 'popupFail',
                    model: []
        }
    }
}
