import grails.util.GrailsNameUtils
import groovy.text.SimpleTemplateEngine

includeTargets << grailsScript("Init")
includeTargets << grailsScript('_GrailsBootstrap')

overwriteAll = false
templateAttributes = [:]
templateDir = "$springSecurityTwitterPluginDir/src/templates"
appDir = "$basedir/grails-app"
templateEngine = new SimpleTemplateEngine()

target(s2InitTwitter: 'Initializes Twitter artifacts for the Spring Security Twitter plugin') {
	depends(checkVersion, configureProxy, packageApp, classpath)

	configure()
	copyData()
}

private void configure() {

	def SpringSecurityUtils = classLoader.loadClass('org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils')
	def conf = SpringSecurityUtils.securityConfig

	String userClassFullName = conf.userLookup.userDomainClassName
	checkValue userClassFullName, 'domain.userClassName'

	String userPackageName
	String userClassName
	(userPackageName, userClassName) = splitClassName(userClassFullName)

	String userConnectionProperty = conf.userLookup.usernamePropertyName
	checkValue userConnectionProperty, 'domain.userConnectionProperty'

    String userImport = "import $userClassFullName"

	templateAttributes = [userClassFullName: userClassFullName,
	                      userClassName: userClassName,
                          className: 'TwitterUser',
                          userConnectionProperty: userConnectionProperty,
                          userImport: userImport]
}

private void copyData() {
	copyFile "$templateDir/spring-security-twitter.messages.properties.template",
		"$appDir/i18n/spring-security-twitter.messages.properties"

	generateFile "$templateDir/TwitterUser.groovy.connected.template",
	             "$appDir/domain/TwitterUser.groovy"
}

packageToDir = { String packageName ->
	String dir = ''
	if (packageName) {
		dir = packageName.replaceAll('\\.', '/') + '/'
	}

	return dir
}

okToWrite = { String dest ->

	def file = new File(dest)
	if (overwriteAll || !file.exists()) {
		return true
	}

	String propertyName = "file.overwrite.$file.name"
	ant.input(addProperty: propertyName, message: "$dest exists, ok to overwrite?",
	          validargs: 'y,n,a', defaultvalue: 'y')

	if (ant.antProject.properties."$propertyName" == 'n') {
		return false
	}

	if (ant.antProject.properties."$propertyName" == 'a') {
		overwriteAll = true
	}

	true
}

generateFile = { String templatePath, String outputPath ->
	if (!okToWrite(outputPath)) {
		return
	}

	File templateFile = new File(templatePath)
	if (!templateFile.exists()) {
		ant.echo message: "\nERROR: $templatePath doesn't exist"
		return
	}

	File outFile = new File(outputPath)

	// in case it's in a package, create dirs
	ant.mkdir dir: outFile.parentFile

	outFile.withWriter { writer ->
		templateEngine.createTemplate(templateFile.text).make(templateAttributes).writeTo(writer)
	}

	ant.echo message: "generated $outFile.absolutePath"
}

splitClassName = { String fullName ->

	int index = fullName.lastIndexOf('.')
	String packageName = ''
	String className = ''
	if (index > -1) {
		packageName = fullName[0..index-1]
		className = fullName[index+1..-1]
	}
	else {
		packageName = ''
		className = fullName
	}

	[packageName, className]
}

checkValue = { String value, String attributeName ->
	if (!value) {
		ant.echo message: "\nERROR: Cannot generate; grails.plugins.springsecurity.$attributeName isn't set"
		System.exit 1
	}
}

copyFile = { String from, String to ->
	if (!okToWrite(to)) {
		return
	}

	ant.copy file: from, tofile: to, overwrite: true
}

setDefaultTarget 's2InitTwitter'

