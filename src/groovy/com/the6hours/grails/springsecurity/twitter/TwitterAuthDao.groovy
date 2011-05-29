package com.the6hours.grails.springsecurity.twitter

/**
 * TODO
 *
 * @since 02.05.11
 * @author Igor Artamonov (http://igorartamonov.com)
 */
public interface TwitterAuthDao {

    TwitterUserDomain findUser(String username)

    TwitterUserDomain create(TwitterAuthToken token)

    void update(TwitterUserDomain user)

    Object getPrincipal(TwitterUserDomain user)

    def getRoles(TwitterUserDomain user)
}