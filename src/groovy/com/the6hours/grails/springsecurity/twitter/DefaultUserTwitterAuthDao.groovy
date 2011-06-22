package com.the6hours.grails.springsecurity.twitter

import org.springframework.security.core.GrantedAuthority
import org.codehaus.groovy.grails.plugins.springsecurity.GormUserDetailsService
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

/**
 * Default DAO for twitter auth stat are merged with User object
 *
 * @since 22.06.11
 * @author Igor Artamonov (http://igorartamonov.com)
 */
class DefaultUserTwitterAuthDao implements TwitterAuthDao {

    def grailsApplication
    GormUserDetailsService userDetailsService

    String userDomainClassName
    String rolesPropertyName

    TwitterUserDomain findUser(String username) {
        return userDetailsService.loadUserByUsername(username)
    }

    TwitterUserDomain create(TwitterAuthToken token) {
        def conf = SpringSecurityUtils.securityConfig
        Class<?> User = grailsApplication.getDomainClass(userDomainClassName).clazz
        def user = User.newInstance()
        user.password = token.token
        user.username = token.screenName
        User.withTransaction { status ->
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
        Class<?> User = grailsApplication.getDomainClass(userDomainClassName).clazz
        User.withTransaction { status ->
            user.save()
        }
    }

    Object getPrincipal(TwitterUserDomain user) {
        return user
    }

    Collection<GrantedAuthority> getRoles(TwitterUserDomain user) {
        return user?.getAt(rolesPropertyName)
    }
}
