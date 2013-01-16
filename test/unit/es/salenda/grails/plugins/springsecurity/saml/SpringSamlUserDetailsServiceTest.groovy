package es.salenda.grails.plugins.springsecurity.saml

import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.junit.Before
import org.junit.Test
import org.opensaml.saml2.core.impl.AssertionImpl
import org.opensaml.saml2.core.impl.NameIDImpl
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.saml.SAMLCredential
import test.TestRole
import test.TestSamlUser
import test.TestUserRole

import static es.salenda.grails.plugins.springsecurity.saml.UnitTestUtils.*

@TestFor(SpringSamlUserDetailsService)
@Mock([TestSamlUser, TestRole, TestUserRole])
class SpringSamlUserDetailsServiceTest {
	def credential, nameID, assertion, mockGrailsAplication, testRole, testRole2
	def service

	String username = "jackSparrow"
	Map detailsServiceSettings = [:]
	DefaultGrailsApplication grailsApplication

	@Before
	public void init() {
		service = new SpringSamlUserDetailsService()
		mockOutDefaultGrailsApplication()
		grailsApplication = new DefaultGrailsApplication()

		mockOutSpringSecurityUtilsConfig()
		mockWithTransaction()

		service.authorityClassName = ROLE_CLASS_NAME
		service.authorityJoinClassName = JOIN_CLASS_NAME
		service.authorityNameField = "authority"
		service.samlAutoCreateActive = false
		service.samlAutoCreateKey = null
		service.samlUserAttributeMappings = [username: USERNAME_ATTR_NAME]
		service.samlUserGroupAttribute = GROUP_ATTR_NAME
		service.samlUserGroupToRoleMapping = ['myGroup': ROLE]
		service.userDomainClassName = USER_CLASS_NAME
		service.authoritiesPropertyName = AUTHORITIES_PROPERTY_NAME
		service.grailsApplication = grailsApplication

		nameID = new NameIDImpl("", "", "")
		assertion = new AssertionImpl("", "", "")

		// This is what a SamlResponse will eventually be marshalled to
		credential = new SAMLCredential(nameID, assertion, null, null)
		credential.metaClass.getNameID = { [value: "$username"] }

		testRole = new TestRole(authority: ROLE)
		testRole2 = new TestRole(authority: "FAKEROLE2")

		// set default username to be returned in the saml response
		setMockSamlAttributes(credential, ["$USERNAME_ATTR_NAME": username])
	}

	@Test
	void "loadUserBySAML should return a GrailsUser"() {
		def user = service.loadUserBySAML(credential)
		assert user instanceof GrailsUser
	}

	@Test
	void "loadUserBySAML should return NameID as the username when no mapping specified"() {

		service.samlUserAttributeMappings = [:]

		setMockSamlAttributes(credential, ["$USERNAME_ATTR_NAME": "someotherValue"])

		def user = service.loadUserBySAML(credential)
		assert user.username == username
	}

	@Test
	void "loadUserBySAML should set username from the mapped saml attribute"() {

		def user = service.loadUserBySAML(credential)

		assert user.username == username
	}

	@Test
	void "loadUserBySAML should raise an exception if username not supplied in saml response"() {

		setMockSamlAttributes(credential, ["$USERNAME_ATTR_NAME": null])

		try {
			def user = service.loadUserBySAML(credential)
			fail("Null username in saml response not handled correctly!")

		} catch (UsernameNotFoundException unfException) {
			assert unfException.message == "No username supplied in saml response."

		} catch (Exception ex) {
			fail("Unexpected exception raised.")
		}
	}

	@Test
	void "loadUserBySAML should return a user with the mapped role"() {
		assert testRole.save()

		setMockSamlAttributes(credential, ["$GROUP_ATTR_NAME": "something=something,CN=myGroup", "$USERNAME_ATTR_NAME": 'myUsername'])

		def user = service.loadUserBySAML(credential)

		assert user.authorities.size() == 1
		assert user.authorities.toArray()[0].authority == ROLE
	}

	@Test
	void "loadUserBySAML should not persist the user if autocreate is not active"() {

		assert TestSamlUser.count() == 0
		def userDetails = service.loadUserBySAML(credential)
		assert TestSamlUser.count() == 0
	}

	@Test
	void "loadUserBySAML should persist the user when autocreate is active"() {

		service.samlAutoCreateActive = true
		service.samlAutoCreateKey = 'username'

		assert TestSamlUser.count() == 0
		def userDetails = service.loadUserBySAML(credential)

		assert TestSamlUser.count() == 1
		assert TestSamlUser.findByUsername(userDetails.username)
	}

	@Test
	void "loadUserBySAML should set additional mapped attributes on the user"() {
		def emailAddress = "test@mailinator.com"
		def firstname = "Jack"
		service.samlAutoCreateActive = true
		service.samlAutoCreateKey = 'username'

		service.samlUserAttributeMappings = [email: "$MAIL_ATTR_NAME", firstName: "$FIRSTNAME_ATTR_NAME"]
		setMockSamlAttributes(credential, ["$USERNAME_ATTR_NAME": username, "$MAIL_ATTR_NAME": emailAddress, "$FIRSTNAME_ATTR_NAME": firstname])

		def user = service.loadUserBySAML(credential)
		def samlUser = TestSamlUser.findByUsername(username)
		assert samlUser.email == emailAddress
		assert samlUser.firstName == firstname
	}


	@Test
	void "loadUserBySAML should not persist a user that already exists"() {

		service.samlAutoCreateActive = true
		service.samlAutoCreateKey = 'username'

		def user = new TestSamlUser(username: username, password: 'test')
		assert user.save()

		TestUserRole.metaClass.'static'.removeAll = { TestSamlUser userWithRoles -> }

		assert TestSamlUser.count() == 1
		def userDetail = service.loadUserBySAML(credential)

		assert TestSamlUser.count() == 1
	}

	@Test
	void "loadUserBySAML should persist the role for a new user"() {
		assert testRole.save()

		service.samlAutoCreateActive = true
		service.samlAutoCreateKey = 'username'

		setMockSamlAttributes(credential, ["$GROUP_ATTR_NAME": "something=something,CN=myGroup", "$USERNAME_ATTR_NAME": username])

		TestUserRole.metaClass.'static'.removeAll = { TestSamlUser userWithRoles ->
			// no roles to remove
			assert false
		}
		TestUserRole.metaClass.'static'.create = { TestSamlUser userWithNoRoles, TestRole role ->
			assert userWithNoRoles.username == username
			assert role.authority == ROLE
		}

		def userDetail = service.loadUserBySAML(credential)
	}

	@Test
	void "loadUserBySAML should update the roles for an existing user"() {
		assert testRole.save()

		service.samlAutoCreateActive = true
		service.samlAutoCreateKey = 'username'

		setMockSamlAttributes(credential, ["$GROUP_ATTR_NAME": "something=something,CN=myGroup", "$USERNAME_ATTR_NAME": username])

		def user = new TestSamlUser(username: username, password: 'test')
		assert user.save()

		def removedExistingRoles = false
		TestUserRole.metaClass.'static'.removeAll = { TestSamlUser userWithRoles ->
			assert userWithRoles.username == user.username
			removedExistingRoles = true
		}

		def savedNewRoles = false
		TestUserRole.metaClass.'static'.create = { TestSamlUser userWithNoRoles, TestRole role ->
			assert userWithNoRoles.username == user.username
			assert role.authority == ROLE
			savedNewRoles = true
		}

		def userDetail = service.loadUserBySAML(credential)
		assert removedExistingRoles
		assert savedNewRoles
	}

	@Test
	void "loadUserBySAML should  not update the roles for an existing user"() {
		assert testRole.save()

		service.samlAutoCreateActive = true
		service.samlAutoAssignAuthorities = false
		service.samlAutoCreateKey = 'username'

		setMockSamlAttributes(credential, ["$GROUP_ATTR_NAME": "something=something,CN=myGroup", "$USERNAME_ATTR_NAME": username])

		def user = new TestSamlUser(username: username, password: 'test')
		assert user.save()

		def removedExistingRoles = false
		TestUserRole.metaClass.'static'.removeAll = { TestSamlUser userWithRoles ->
			assert userWithRoles.username == user.username
			removedExistingRoles = true
		}

		def savedNewRoles = false
		TestUserRole.metaClass.'static'.create = { TestSamlUser userWithNoRoles, TestRole role ->
			assert userWithNoRoles.username == user.username
			assert role.authority == ROLE
			savedNewRoles = true
		}

		def userDetail = service.loadUserBySAML(credential)
		assert !removedExistingRoles
		assert !savedNewRoles
	}

	@Test
	void "loadUserBySAML should still pull details from DB"() {
		assert testRole.save()
		assert testRole2.save()



		service.samlAutoCreateActive = true
		service.samlAutoAssignAuthorities = false
		service.samlAutoCreateKey = 'username'

		setMockSamlAttributes(credential, ["$GROUP_ATTR_NAME": "something=something,CN=myGroup", "$USERNAME_ATTR_NAME": username])

		def user = new TestSamlUser(username: username, password: 'test')
		assert user.save()

		TestUserRole.create(user,testRole2)


		def removedExistingRoles = false
		TestUserRole.metaClass.'static'.removeAll = { TestSamlUser userWithRoles ->
			assert userWithRoles.username == user.username
			removedExistingRoles = true
		}

		def savedNewRoles = false
		TestUserRole.metaClass.'static'.create = { TestSamlUser userWithNoRoles, TestRole role ->
			assert userWithNoRoles.username == user.username
			assert role.authority == ROLE
			savedNewRoles = true
		}

		def userDetail = service.loadUserBySAML(credential)
		assert !removedExistingRoles
		assert !savedNewRoles

		Set authorities = userDetail.getAuthorities()

		assert authorities.size() == 1
		assert authorities.iterator().next().authority == testRole2.authority

	}



	@Test
	void "loadUserBySAML should set any mapped fields for a user"() {
		def emailAddress = "test@mailinator.com"
		def firstname = "Jack"

		service.samlAutoCreateActive = true
		service.samlAutoCreateKey = 'username'
		service.samlUserAttributeMappings = [email: "$MAIL_ATTR_NAME", firstName: "$FIRSTNAME_ATTR_NAME"]
		setMockSamlAttributes(credential, ["$USERNAME_ATTR_NAME": username, "$MAIL_ATTR_NAME": emailAddress, "$FIRSTNAME_ATTR_NAME": firstname])

		def user = new TestSamlUser(username: username, password: 'test')
		assert user.save()

		TestUserRole.metaClass.'static'.removeAll = {TestSamlUser samlUser -> }

		service.loadUserBySAML(credential)

		def updatedUser = TestSamlUser.findByUsername(username)
		assert updatedUser.email == emailAddress
		assert updatedUser.firstName == firstname
	}

	@Test
	void "loadUserBySAML should update mapped fields for a user"() {
		def intialEmail = 'myfirstmail@mailinator.com'
		def emailAddress = "test@mailinator.com"

		service.samlAutoCreateActive = true
		service.samlAutoCreateKey = 'username'
		service.samlUserAttributeMappings = [email: "$MAIL_ATTR_NAME"]
		setMockSamlAttributes(credential, ["$USERNAME_ATTR_NAME": username, "$MAIL_ATTR_NAME": emailAddress])
		TestUserRole.metaClass.'static'.removeAll = {TestSamlUser samlUser -> }

		def user = new TestSamlUser(username: username, password: 'test', email: intialEmail)
		assert user.save()

		service.loadUserBySAML(credential)

		def updatedUser = TestSamlUser.findByUsername(username)
		assert updatedUser.email == emailAddress
	}
}
