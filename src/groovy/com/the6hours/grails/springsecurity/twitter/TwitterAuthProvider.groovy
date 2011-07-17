package com.the6hours.grails.springsecurity.twitter

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.userdetails.User

/**
 * TODO
 *
 * @since 02.05.11
 * @author Igor Artamonov (http://igorartamonov.com)
 */
class TwitterAuthProvider implements AuthenticationProvider {

    TwitterAuthDao authDao
    TwitterAuthListener listener

    Authentication authenticate(Authentication authentication) {
        TwitterAuthToken token = authentication

        TwitterUserDomain user = authDao.findUser(token.screenName)

        if (user == null) {
            //log.debug "Create new twitter user"
            user = authDao.create(token)
            if (listener) {
                listener.userCreated(user)
            }
        }  else {
            if (user.token != token.token || user.tokenSecret != token.tokenSecret) {
                //log.info "update twitter user $user.screenName"
                user.token = token.token
                user.tokenSecret = token.tokenSecret
                authDao.update(user)
                if (listener) {
                    listener.tokenUpdated(user)
                }
            }
        }

        Collection<GrantedAuthority> roles = authDao.getRoles(user)
        UserDetails userDetails = new User(user.screenName, token.tokenSecret, true, true, true, true, roles)
        token.details = userDetails
        token.authorities = userDetails.getAuthorities()
        token.principal = authDao.getPrincipal(user)
        return token
    }

    boolean supports(Class<? extends Object> authentication) {
        return TwitterAuthToken.isAssignableFrom(authentication)
    }

}
