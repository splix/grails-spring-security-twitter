import com.the6hours.grails.springsecurity.twitter.TwitterAuthProvider
import com.the6hours.grails.springsecurity.twitter.TwitterAuthFilter

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
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.3 > *"

    Map dependsOn = ['springSecurityCore': '1.1.2 > *']

    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    String author = 'Igor Artamonov'
    String authorEmail = 'igor@artamonov.ru'
    String title = 'Twitter authentication for Spring Security plugin'
    String description = 'Twitter authentication support for the Spring Security plugin.'

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/spring-security-twitter"

    def doWithSpring = {
        def SpringSecurityUtils = classLoader.loadClass('org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils')

        def conf = SpringSecurityUtils.securityConfig
        if (!conf) {
            return
        }

        println 'Configuring Spring Security Twitter ...'

        SpringSecurityUtils.loadSecondaryConfig 'DefaultTwitterSecurityConfig'

        // have to get again after overlaying DefaultTwitterSecurityConfig
        conf = SpringSecurityUtils.securityConfig

        SpringSecurityUtils.registerProvider conf.beans.provider
        SpringSecurityUtils.registerFilter conf.beans.filter, SecurityFilterPosition.OPENID_FILTER


        twitterAuthProvider(TwitterAuthProvider) {
            userDetailsService = ref('userDetailsService')
        }

        twitterAuthFilter(TwitterAuthFilter) {
            rememberMeServices = ref('rememberMeServices')
            authenticationManager = ref('authenticationManager')
            authenticationSuccessHandler = ref('authenticationSuccessHandler')
            authenticationFailureHandler = ref('authenticationFailureHandler')
            authenticationDetailsSource = ref('authenticationDetailsSource')
            sessionAuthenticationStrategy = ref('sessionAuthenticationStrategy')
            filterProcessesUrl = '/j_spring_twitter_security_check' // not configurable
        }

    }

    def doWithApplicationContext = { applicationContext ->
    }

}
