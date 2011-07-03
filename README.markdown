Twitter Auth plugin for Grails
==============================

Grails plugin for Twitter Authentication, as extension to [Grails Spring Security Core plugin](http://www.grails.org/plugin/spring-security-core)

Currently it's **alfa version**, and it isn't deployed anywhere yet. If you want to use this plugin, you have to install it from GitHub (see later),

Requirements
------------

 * grails 1.3+
 * spring-security-core plugin 1.1+
 * twitter4j (will be downloaded automatically)

How to install
--------------

Install spring-security-plugin:

```
grails install-plugin spring-security-core
```

Download package from github: [grails-spring-security-twitter-0.3.zip](http://github.com/downloads/splix/grails-spring-security-twitter/grails-spring-security-twitter-0.3.zip).
And install it:

```
grails install-plugin %PATH_TO_DOWNLOADED_ZIP%
grails s2-init-twitter
```

Done

How to use
----------

For documentation see: <a href="http://splix.github.com/grails-spring-security-twitter/">http://splix.github.com/grails-spring-security-twitter/</a>


Questions?
----------

Have any questions? Contact me: igor@artamonov.ru
