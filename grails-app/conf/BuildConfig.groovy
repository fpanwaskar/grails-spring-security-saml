
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.release.scm.enabled = false

grails.project.dependency.resolution = {
    inherits ('global') {
		excludes "xml-apis"
	}
	log 'warn'

	repositories {
		grailsPlugins()
		grailsHome()
		grailsCentral()
		mavenRepo "https://build.shibboleth.net/nexus/content/repositories/releases"
		mavenRepo "https://build.shibboleth.net/nexus/content/groups/public/"
		mavenCentral()
	}

    dependencies {
			build ("org.apache.ivy:ivy:2.2.0") {
				transitive = false
			}

			compile ('org.opensaml:opensaml:2.5.3') {
				transitive = false
			}
			compile ('org.opensaml:xmltooling:1.3.4') {
				transitive = false
			}
			compile ('org.apache.santuario:xmlsec:1.4.4') {
				transitive = false
			}
			compile ('org.bouncycastle:bcprov-jdk15:1.45') {
				transitive = false
			}
			compile ('org.opensaml:openws:1.4.4') {
				transitive = false
			}
			compile('joda-time:joda-time:1.6.2') {
				transitive = false
			}
			compile('commons-httpclient:commons-httpclient:3.1') {
				transitive = false
			}
			compile('org.apache.velocity:velocity:1.7') {
				transitive = false
			}
			compile ('ca.juliusdavies:not-yet-commons-ssl:0.3.9') {
				transitive = false
			}
			compile ('org.owasp.esapi:esapi:2.0.1')
			{
				transitive = false
			}
    }

    plugins {
			test ":spock:0.7"
			test ":code-coverage:1.2.5"
			compile ":build-test-data:2.0.3"
			compile ":guard:1.0.7"
			build(":tomcat:$grailsVersion",
              ":hibernate:$grailsVersion") {
            export = false
        }
    }
}

//<editor-fold desc="Release Plugin External Maven Config">
def mavenConfigFile = new File("${basedir}/grails-app/conf/mavenInfo.groovy")
if (mavenConfigFile.exists()) {
	def slurpedMavenInfo = new ConfigSlurper().parse(mavenConfigFile.toURL())
	slurpedMavenInfo.grails.project.repos.each {k, v ->
		println "Adding maven info for repo $k"
		grails.project.repos."$k" = v
	}
}
else {
	println "No mavenInfo file found."
}
//</editor-fold>



codenarc.reports = {
	CodeNarcReport('xml') {
		outputFile = 'target/test-reports/CodeNarcReport.xml'
		title = 'CodeNarc Report'
	}
}
