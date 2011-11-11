package com.the6hours.grails.springsecurity.twitter

import org.springframework.security.core.GrantedAuthority

/**
 * TODO
 *
 * @since 02.05.11
 * @author Igor Artamonov (http://igorartamonov.com)
 */
public interface TwitterAuthDao<T extends TwitterUserDomain> {

    T findUser(String username)

    T create(TwitterAuthToken token)

    void update(T user)

    Object getPrincipal(T user)

    Collection<GrantedAuthority> getRoles(T user)
}