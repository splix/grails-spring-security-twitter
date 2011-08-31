package com.the6hours.grails.springsecurity.twitter

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.GrantedAuthority

/**
 * DAO for independent twitter user domain object
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

    TwitterUserDomain findUser(String username) {
		Class<?> User = grailsApplication.getDomainClass(domainClassName).clazz
        def user = null
        User.withTransaction { status ->
            user = User.findWhere(screenName: username)
            user?.user // load the User object to memory prevent LazyInitializationException
        }
        return user
    }

    TwitterUserDomain create(TwitterAuthToken token) {
        Class<TwitterUserDomain> userClass = grailsApplication.getDomainClass(domainClassName).clazz
        TwitterUserDomain user = userClass.newInstance()
        user.screenName = token.screenName
        user.token = token.token
        user.tokenSecret = token.tokenSecret
        user.uid = token.userId
        user[connectionPropertyName] = createAppUser(token)
        update(user)

        return user
    }

    Object createAppUser(TwitterAuthToken token) {
        //TODO wtf???
        def conf = SpringSecurityUtils.securityConfig
        Class<?> MainUser = grailsApplication.getDomainClass(userDomainClassName).clazz
        def user = MainUser.newInstance()
        user.password = token.token
        user.username = token.screenName
        MainUser.withTransaction { status ->
            user.save()
        }
        conf.twitter.autoCreate.roles.collect {
            Class<?> Role = grailsApplication.getDomainClass(conf.authority.className).clazz
            def role = Role.findByAuthority(it)
            if (!role) {
                role = Role.newInstance()
                role.properties[conf.authority.nameField] = it
                Role.withTransaction { status ->
                    role.save()
                }
            }
            return role
        }.each { role ->
            Class<?> PersonRole = grailsApplication.getDomainClass(conf.userLookup.authorityJoinClassName).clazz
            PersonRole.withTransaction { status ->
                PersonRole.create(user, role, false)
            }
        }
        return user
    }

    void update(TwitterUserDomain user) {
        Class<?> User = grailsApplication.getDomainClass(domainClassName).clazz
        User.withTransaction { status ->
            user.save()
        }
    }

    Object getPrincipal(TwitterUserDomain user) {
        return user.getAt(connectionPropertyName)
    }

    Collection<GrantedAuthority> getRoles(TwitterUserDomain user) {
        def conf = SpringSecurityUtils.securityConfig
        Class<?> PersonRole = grailsApplication.getDomainClass(conf.userLookup.authorityJoinClassName).clazz
        def roles = PersonRole.withTransaction { status ->
            return getPrincipal(user)?.getAt(rolesPropertyName)
        }
        return roles.collect {
            new GrantedAuthorityImpl(it[conf.authority.nameField])
        }
    }
}
