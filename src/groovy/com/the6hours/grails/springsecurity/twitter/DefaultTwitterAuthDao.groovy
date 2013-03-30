package com.the6hours.grails.springsecurity.twitter

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.springsecurity.GormUserDetailsService
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.userdetails.UserDetails

/**
 *
 * @author Igor Artamonov (http://igorartamonov.com)
 * @since 21.12.12
 */
class DefaultTwitterAuthDao implements TwitterAuthDao, InitializingBean {

    private static def log = Logger.getLogger(this)

    GrailsApplication grailsApplication
    ApplicationContext applicationContext
    def coreUserDetailsService

    Class TwitterUserDomain
    Class AppUserDomain
    String twitterUserClassName
    String appUserClassName

    String idProperty = "twitterId"
    String usernameProperty = "username"
    String rolesPropertyName

    List<String> defaultRoleNames = ['ROLE_USER', 'ROLE_TWITTER']

    String appUserConnectionPropertyName = "user"

    def twitterAuthService

    Object findUser(TwitterAuthToken token) {
        if (twitterAuthService && twitterAuthService.respondsTo('findUser', token)) {
            return twitterAuthService.findUser(token)
        }
        Object user = null
        TwitterUserDomain.withTransaction {
            user = TwitterUserDomain.findWhere((idProperty): token.userId)
        }
        return user
    }

    Object create(TwitterAuthToken token) {
        if (twitterAuthService && twitterAuthService.respondsTo('create', token.class)) {
            return twitterAuthService.create(token)
        }

        def securityConf = SpringSecurityUtils.securityConfig

        def user = grailsApplication.getDomainClass(TwitterUserDomain.name).newInstance()

        user.properties[idProperty] = token.userId
        if (usernameProperty && user.properties.containsKey(usernameProperty)) {
            user.properties[usernameProperty] = token.screenName
        }
        if (user.properties.containsKey('token')) {
            user.token = token.token
        }
        if (user.properties.containsKey('tokenSecret')) {
            user.tokenSecret = token.tokenSecret
        }

        def appUser = null
        if (TwitterUserDomain != AppUserDomain) {
            appUser = grailsApplication.getDomainClass(AppUserDomain.name).newInstance()
            appUser[securityConf.userLookup.usernamePropertyName] = token.screenName
            appUser[securityConf.userLookup.passwordPropertyName] = token.tokenSecret
            appUser[securityConf.userLookup.enabledPropertyName] = true
            appUser[securityConf.userLookup.accountExpiredPropertyName] = false
            appUser[securityConf.userLookup.accountLockedPropertyName] = false
            appUser[securityConf.userLookup.passwordExpiredPropertyName] = false
            AppUserDomain.withTransaction {
                appUser.save(flush: true, failOnError: true)
            }
            user[appUserConnectionPropertyName] = appUser
        } else {
            appUser = user
        }
        TwitterUserDomain.withTransaction {
            user.save()
        }
        Class<?> PersonRole = grailsApplication.getDomainClass(securityConf.userLookup.authorityJoinClassName).clazz
        Class<?> Authority = grailsApplication.getDomainClass(securityConf.authority.className).clazz
        PersonRole.withTransaction { status ->
            defaultRoleNames.each { String roleName ->
                String findByField = securityConf.authority.nameField[0].toUpperCase() + securityConf.authority.nameField.substring(1)
                def auth = Authority."findBy${findByField}"(roleName)
                if (auth) {
                    PersonRole.create(appUser, auth)
                } else {
                    log.error("Can't find authority for name '$roleName'")
                }
            }
        }

        return user
    }

    void updateIfNeeded(Object user, TwitterAuthToken token) {
        if (twitterAuthService && twitterAuthService.respondsTo('updateTokenIfNeeded', user.class, token.class)) {
            twitterAuthService.updateIfNeeded(user, token)
            return
        }
        TwitterUserDomain.withTransaction {
            if (!user.isAttached()) {
                user.attach()
            }
            boolean update = false
            if (user.properties.containsKey('token')) {
                if (user.token != token.token) {
                    update = true
                    user.token = token.token
                }
            }
            if (user.properties.containsKey('tokenSecret')) {
                if (user.tokenSecret != token.tokenSecret) {
                    update = true
                    user.tokenSecret = token.tokenSecret
                }
            }
            if (user.properties.containsKey(usernameProperty)) {
                if (user.properties[usernameProperty] != token.screenName) {
                    update = true
                    user.properties[usernameProperty] = token.screenName
                }
            }
            if (update) {
                user.save()
            }
        }
    }

    Object getAppUser(Object user) {
        if (twitterAuthService && twitterAuthService.respondsTo('getAppUser', user.class)) {
            return twitterAuthService.getAppUser(user)
        }
        if (TwitterUserDomain == AppUserDomain) {
            return user
        }
        return user[appUserConnectionPropertyName]
    }

    Object getPrincipal(Object user) {
        if (twitterAuthService && twitterAuthService.respondsTo('getPrincipal', user.class)) {
            return twitterAuthService.getPrincipal(user)
        }
        if (coreUserDetailsService) {
            return coreUserDetailsService.createUserDetails(user, getRoles(user))
        }
        return user
    }

    Collection<GrantedAuthority> getRoles(Object user) {
        if (twitterAuthService && twitterAuthService.respondsTo('getRoles', user.class)) {
            return twitterAuthService.getRoles(user)
        }

        if (UserDetails.isAssignableFrom(user.class)) {
            return ((UserDetails)user).getAuthorities()
        }

        def conf = SpringSecurityUtils.securityConfig
        Class<?> PersonRole = grailsApplication.getDomainClass(conf.userLookup.authorityJoinClassName)?.clazz
        if (!PersonRole) {
            log.error("Can't load roles for user $user. Reason: can't find ${conf.userLookup.authorityJoinClassName} class")
            return []
        }
        Collection roles = []
        PersonRole.withTransaction { status ->
            roles = user?.getAt(rolesPropertyName)
        }
        if (!roles) {
            roles = []
        }
        if (roles.empty) {
            return roles
        }
        return roles.collect {
            if (it instanceof String) {
                return new GrantedAuthorityImpl(it.toString())
            } else {
                new GrantedAuthorityImpl(it[conf.authority.nameField])
            }
        }
    }

    void afterPropertiesSet() throws Exception {
        if (coreUserDetailsService != null) {
            if (!(coreUserDetailsService instanceof GormUserDetailsService && coreUserDetailsService.respondsTo('createUserDetails'))) {
                log.warn("UserDetailsService from spring-security-core don't have method 'createUserDetails()'")
                coreUserDetailsService = null
            }
        } else {
            log.warn("No UserDetailsService bean from spring-security-core")
        }

        if (TwitterUserDomain == null) {
            TwitterUserDomain = grailsApplication.getDomainClass(twitterUserClassName)?.clazz
            if (!TwitterUserDomain) {
                log.error("Can't find domain: $twitterUserClassName")
            }
        }
        if (AppUserDomain == null) {
            AppUserDomain = grailsApplication.getDomainClass(appUserClassName)?.clazz
            if (!AppUserDomain) {
                log.error("Can't find domain: $appUserClassName")
            }
        }
        if (TwitterUserDomain == null && AppUserDomain != null) {
            log.info("Use $AppUserDomain to store Twitter Authentication")
            TwitterUserDomain = AppUserDomain
        } else if (TwitterUserDomain != null && AppUserDomain == null) {
            AppUserDomain = TwitterUserDomain
        }

    }
}
