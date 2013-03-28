import com.the6hours.grails.springsecurity.twitter.DefaultTwitterAuthDao
import com.the6hours.grails.springsecurity.twitter.TwitterAuthProvider
import com.the6hours.grails.springsecurity.twitter.TwitterAuthFilter
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler

/* Copyright 2006-2010 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
class SpringSecurityTwitterGrailsPlugin {

    // the plugin version
    def version = "0.4.4"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0.0 > *"

    Map dependsOn = ['springSecurityCore': '1.2.7.2 > *']
    def license = 'APACHE'

    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def issueManagement = [ system: "GitHub", url: "https://github.com/splix/grails-spring-security-twitter/issues" ]
    def scm = [ url: "https://github.com/splix/grails-spring-security-twitter.git" ]
    def documentation = "http://grails.org/plugin/spring-security-twitter"

	def observe = ["springSecurityCore"]

    String author = 'Igor Artamonov'
    String authorEmail = 'igor@artamonov.ru'
    String title = 'Twitter Authentication  for Spring Security'
    String description = 'Twitter authentication support for the Spring Security plugin.'


    def doWithSpring = {
        //def SpringSecurityUtils = classLoader.loadClass('org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils')

        def conf = SpringSecurityUtils.securityConfig
        if (!conf) {
            println 'ERROR: There is no Spring Security configuration'
            println 'ERROR: Stop configuring Spring Security Twitter'
            return
        }

        println 'Configuring Spring Security Twitter ...'

        SpringSecurityUtils.loadSecondaryConfig 'DefaultTwitterSecurityConfig'

        // have to get again after overlaying DefaultTwitterSecurityConfig
        conf = SpringSecurityUtils.securityConfig

		String twitterDaoName = conf?.twitter?.dao ?: null
        if (twitterDaoName == null) {
			twitterDaoName = 'twitterAuthDao'
            twitterAuthDao(DefaultTwitterAuthDao) {
                twitterUserClassName = conf.twitter.domain.classname
                appUserConnectionPropertyName = conf.twitter.domain.connectionPropertyName

                appUserClassName = conf.userLookup.userDomainClassName
                rolesPropertyName = conf.userLookup.authoritiesPropertyName

                coreUserDetailsService = ref('userDetailsService')
                grailsApplication = ref('grailsApplication')
            }
		} else {
			log.info("Using provided Twitter Auth DAO bean: $twitterDaoName")
		}

		String twitterAuthProviderName = conf?.twitter?.bean?.provider ?: null
        if (twitterAuthProviderName != null) {
            SpringSecurityUtils.registerProvider twitterAuthProviderName
        } else {
            SpringSecurityUtils.registerProvider 'twitterAuthProvider'
            twitterAuthProviderName = 'twitterAuthProvider'
            twitterAuthProvider(TwitterAuthProvider) {
                authDao = ref(twitterDaoName)
            }
        }

		String twitterAuthFilterName = conf?.twitter?.bean?.filter ?: null
        if (twitterAuthFilterName != null) {
            SpringSecurityUtils.registerFilter twitterAuthFilterName, SecurityFilterPosition.OPENID_FILTER
        } else {
            SpringSecurityUtils.registerFilter 'twitterAuthFilter', SecurityFilterPosition.OPENID_FILTER
            twitterAuthFilterName = 'twitterAuthFilter'
            twitterAuthFilter(TwitterAuthFilter, conf.twitter.filter.processUrl) {
                rememberMeServices = ref('rememberMeServices')
                authenticationManager = ref('authenticationManager')
                authenticationDetailsSource = ref('authenticationDetailsSource')
                filterProcessesUrl =  conf.twitter.filter.processUrl
                consumerKey = conf.twitter.consumerKey
                consumerSecret = conf.twitter.consumerSecret
                if (conf.twitter.sessionAuthenticationStrategy) {
                    sessionAuthenticationStrategy = ref(conf.twitter.sessionAuthenticationStrategy)
                } else {
                    sessionAuthenticationStrategy = ref('sessionAuthenticationStrategy')
                }
                if (conf.twitter.authenticationFailureHandler) {
                    authenticationFailureHandler = ref(conf.twitter.authenticationFailureHandler)
                } else {
                    authenticationFailureHandler = ref('authenticationFailureHandler')
                }
                if (conf.twitter.authenticationSuccessHandler) {
                    authenticationSuccessHandler = ref(conf.twitter.authenticationSuccessHandler)
                } else if (conf.twitter.popup) {
                    authenticationSuccessHandler = new SimpleUrlAuthenticationSuccessHandler(conf.twitter.filter.processPopupUrl)
                } else {
                    authenticationSuccessHandler = ref('authenticationSuccessHandler')
                }
            }
        }
        println "... finished configuring Spring Security Twitter"
    }

    def doWithApplicationContext = { applicationContext ->
    }

}
