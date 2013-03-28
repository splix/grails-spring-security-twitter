package com.the6hours.grails.springsecurity.twitter

import org.springframework.security.core.GrantedAuthority

/**
 * TODO
 *
 * @since 02.05.11
 * @author Igor Artamonov (http://igorartamonov.com)
 */
public interface TwitterAuthDao<T> {

    T findUser(TwitterAuthToken username)

    T create(TwitterAuthToken token)

    void updateTokenIfNeeded(T user, TwitterAuthToken token)

    Object getAppUser(T user)

    Object getPrincipal(Object user)

    Collection<GrantedAuthority> getRoles(T user)
}