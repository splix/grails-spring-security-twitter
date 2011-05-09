package com.the6hours.grails.springsecurity.twitter

/**
 * TODO
 *
 * @since 02.05.11
 * @author Igor Artamonov (http://igorartamonov.com)
 */
class DefaultConnectedTwitterAuthDao implements TwitterAuthDao {

    def grailsApplication

    String domainClassName

    String connectionPropertyName
    String userDomainClassName
    String rolesPropertyName

    TwitterUser findUser(String username) {
		Class<?> User = grailsApplication.getDomainClass(domainClassName).clazz
		User.withTransaction { status ->
			def user = User.findWhere(username: username)
            return user
		}
        return null
    }

    TwitterUser create(TwitterAuthToken token) {
        Class<TwitterUser> userClass = grailsApplication.getDomainClass(domainClassName).clazz
        TwitterUser user = userClass.newInstance()
        user.screenName = token.screenName
        user.token = token.token
        user.tokenSecret = token.tokenSecret
        user.userId = token.userId

        update(user)

        return user
    }

    void update(TwitterUser user) {
        Class<?> User = grailsApplication.getDomainClass(domainClassName).clazz
        User.withTransaction { status ->
            user.save()
        }
    }

    Object getPrincipal(TwitterUser user) {
        return user.properties[connectionPropertyName]
    }

    String[] getRoles(TwitterUser user) {
        return user.getAt(connectionPropertyName).getAt(rolesPropertyName)
    }
}
