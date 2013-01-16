package es.salenda.grails.plugins.springsecurity.saml

import grails.plugin.spock.IntegrationSpec
import test.TestUserRole
import test.TestSamlUser
import spock.lang.Ignore
import grails.test.mixin.TestFor
import test.TestRole
import org.springframework.security.saml.SAMLCredential
import org.opensaml.saml2.core.impl.NameIDImpl
import org.opensaml.saml2.core.impl.AssertionImpl
import org.codehaus.groovy.grails.commons.GrailsApplication


class SpringSamlUserDetailsServiceSpec extends IntegrationSpec {

	String username = "jackSparrow"

	GrailsApplication grailsApplication

	def "Test getting user details from db"() {
		given:
			TestSamlUser user = TestSamlUser.build([username:username,email:'bob@fake.com'])
			TestRole role = TestRole.build(authority:"testauth")
			TestUserRole userRole = TestUserRole.build(user:user,role:role)
			SpringSamlUserDetailsService service = new SpringSamlUserDetailsService(samlAutoAssignAuthorities: false,samlAutoCreateActive: true,userDomainClassName: "test.TestSamlUser",samlAutoCreateKey: 'username',authorityNameField: 'authority',authorityJoinClassName: 'test.TestUserRole', authoritiesPropertyName:'authorities')
			service.grailsApplication = grailsApplication
		  def loadedUser
		when:
			SAMLCredential cred
			cred = new SAMLCredential(new NameIDImpl("", "", ""), new AssertionImpl("", "", ""), null, null)
			cred.metaClass.getNameID = { [value: "$username"] }
			loadedUser = service.loadUserBySAML(cred)




		then:
			user.username == username && user.email == 'bob@fake.com' && loadedUser && loadedUser.username == username

	}



}
