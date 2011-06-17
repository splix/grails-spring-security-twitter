Twitter Auth plugin for Grails
==============================

Grails plugin for Twitter Authentication, as extension to [Grails Spring Security Core plugin](http://www.grails.org/plugin/spring-security-core)

Currently it's **alfa version**, and it isn't deployed anywhere yet. If you want to use this plugin, you have to download
and install it manually (see later),

Requirements
------------

 * grails 1.3+
 * spring-security-core plugin 1.1+
 * twitter4j

How to install
--------------

Install spring-security-plugin:

```
grails install-plugin spring-security-core
```

Build this plugin:

```
git clone git://github.com/splix/grails-spring-security-twitter.git
cd grails-spring-security-twitter
grails release-plugin
```

Press Ctrl+C when it asks for your SVN password. And please remember path to built **zip** file.

And install it:

```
cd %YOUR PROJECT DRI%
grails install-plugin %PATH TO PLUGIN ZIP%
grails s2-init-twitter
```

Done