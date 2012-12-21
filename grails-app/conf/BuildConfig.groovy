grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"

String springSecurityVer = "3.0.7.RELEASE"
grails.release.scm.enabled = false
grails.project.repos.default = "grailsCentral"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }

    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenRepo "http://twitter4j.org/maven2"
        mavenRepo "http://repo1.maven.org/maven2"
    }
    dependencies {
        runtime('org.twitter4j:twitter4j-core:2.2.5'){
            excludes 'commons-logging'
        }


        runtime('org.springframework.security:spring-security-core:'+springSecurityVer) {
            excludes 'com.springsource.javax.servlet',
                     'com.springsource.org.aopalliance',
                     'com.springsource.org.apache.commons.logging',
                     'com.springsource.org.apache.xmlcommons',
                     'org.springframework.aop',
                     'org.springframework.beans',
                     'org.springframework.context',
                     'org.springframework.core',
                     'org.springframework.web'

        }
        runtime('org.springframework.security:spring-security-web:'+springSecurityVer) {
            excludes 'com.springsource.javax.servlet',
                     'com.springsource.org.aopalliance',
                     'com.springsource.org.apache.commons.logging',
                     'com.springsource.org.apache.xmlcommons',
                     'org.springframework.aop',
                     'org.springframework.beans',
                     'org.springframework.context',
                     'org.springframework.core',
                     'org.springframework.web'
        }
    }

    plugins {
        provided ':webxml:1.4.1'

        build(':release:2.2.0', ':rest-client-builder:1.0.3') {
            export = false
        }

    }
}
