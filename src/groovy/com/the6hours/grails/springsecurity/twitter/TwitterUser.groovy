package com.the6hours.grails.springsecurity.twitter

/**
 * TODO
 *
 * @since 02.05.11
 * @author Igor Artamonov (http://igorartamonov.com)
 */
interface TwitterUser {

    public int getUserId()
    public void setUserId(int userId)

    public String getScreenName()
    public void setScreenName(String screenName)

    public String getTokenSecret()
    public void setTokenSecret(String secret)

    public String getToken()
    public void setToken(String token)
}
