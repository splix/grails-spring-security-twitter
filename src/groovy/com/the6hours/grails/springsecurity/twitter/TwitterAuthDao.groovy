package com.the6hours.grails.springsecurity.twitter

/**
 * TODO
 *
 * @since 02.05.11
 * @author Igor Artamonov (http://igorartamonov.com)
 */
public interface TwitterAuthDao {

    TwitterUser findUser(String username)

    TwitterUser create(TwitterAuthToken token)

    void update(TwitterUser user)

    Object getPrincipal(TwitterUser user)

    String[] getRoles(TwitterUser user)
}