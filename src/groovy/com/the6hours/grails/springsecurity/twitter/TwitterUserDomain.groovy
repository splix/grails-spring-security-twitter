package com.the6hours.grails.springsecurity.twitter

/**
 *
 * If you're upgrading from versions prior to 0.5 - just remote all usages of this interface, remove
 * 'implements TwitterUserDomain' from your domain. You don't need this class anymore.
 *
 * Since version 0.5 you could use standard domain for Twitter Authentication plugin, just
 * add fields: long twitterId, String username, String token, String tokenSecret
 *
 * Btw, if you want explicitly show that you domain is a domain Twitter User, you could still inherit
 * this class.
 *
 */
class TwitterUserDomain {

    long twitterId
    String username
    String token
    String tokenSecret

}
