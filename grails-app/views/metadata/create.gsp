<!doctype html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Metadata</title>
		<style type="text/css">
			.wide{
				width: 500px;
			}
			td{
				vertical-align: top;
			}
		</style>
	</head>
	<body>
		<div style="margin-left: 20px;">
			<h1>Generate metadata</h1>
			<p>
			    Generates a new metadata for service provider. Output can be used to configure your securityContext.xml descriptor.
			</p>

			<g:form action="save">
			    <table>

			        <tr>
			            <td>Store for the current session:</td>
			            <td>
			                <select name="store">
			                    <option value="true">Yes</option>
			                    <option value="false">No</option>
			                </select>
			                <br/>
			           		<small>When set to true the generated metadata will be stored in the local metadata manager. The value
			                    will be available
			                    only until restart of the application server.
			                </small>
			            </td>
			        </tr>

			        <tr>
			            <td>Entity ID:</td>
			            <td>
			            	<g:textField name="entityId" class="wide" value="${entityId}"/>
			            	<br/>
			                <small>Entity ID is a unique identifier for an identity or service provider. Value is included in the
			                    generated metadata.
			                </small>
			            </td>
			        </tr>

			        <tr>
			            <td>Entity base URL:</td>
			            <td>
			            	<g:textField name="baseURL"  class="wide" value="${baseUrl }"/>
			            	<br/>
			                <small>Base to generate URLs for this server. For example: https://myServer:443/saml-app. The public
			                    address your server will be accessed from should be used here.
			                </small>
			            </td>
			        </tr>

			        <tr>
			            <td>Entity alias:</td>
			            <td>
			            	<g:textField name="alias"  class="wide" value="${alias}"/>
			            	<br/>
			                <small>Alias is an internal mechanism allowing collocating multiple service providers on one server.
			                    Alias must be unique.
			                </small>
			            </td>
			        </tr>

					<tr>
						<td>Include Idp Discovery</td>
						<td>
							<g:checkBox name="includeDiscovery" checked="true"/>
							<br/>
							<small>If Idp Discovery should be included in the meta data.</small>
						</td>
					</tr>

					<tr>
						<td>SSO Bindings</td>
						<td>
							<small>Which bindings to use for SSO</small><br/>
							<g:checkBox name="ssoBindingPost"  checked="true"/> <label for="ssoBindingPost">Post</label><br/>
							<g:checkBox name="ssoBindingPAOS" checked="true" /> <label for="ssoBindingPAOS">PAOS</label><br/>
							<g:checkBox name="ssoBindingArtifact" checked="true" /> <label for="ssoBindingArtifact">Artifact</label><br/>
						</td>
					</tr>
					<tr>
						<td>HoK SSO Bindings</td>
						<td>
							<small>Which bindings to use for HoK SSO</small><br/>
							<g:checkBox name="hokBindingPost"  checked="true"/> <label for="hokBindingPost">Post</label><br/>
							<g:checkBox name="hokBindingArtifact" checked="true" /> <label for="hokBindingArtifact">Artifact</label><br/>
						</td>
					</tr
			        <tr>
			            <td>Security profile:</td>
			            <td>
			                <select name="securityProfile">
			                    <option value="metaiop">MetaIOP</option>
			                    <option value="pkix">PKIX</option>
			                </select>
			                <br/>
			                <small>
			                    <p>Security profile determines how is trust of signature, encryption and SSL/TLS credentials handled. In
			                    MetaIOP mode credential is deemed valid when it's declared in the metadata document of the peer entity. No
			                    validation of the credentials is made. The value is recommended as a default.
			                    <p>PKIX profile verifies credentials against a set of trust anchors. By default certificates present in the
			                    metadata are treated as trust anchors together with the additional selected trusted keys.
			                </small>
			            </td>
			        </tr>


			        <tr>
			            <td>Signing key:</td>
			            <td>
			            	<g:select name="signingKey" from="${availableKeys}" optionKey="key" optionValue="value" />
			            	<br/>
			            	<small>Key used for digital signatures of SAML messages.</small>
			            </td>
			        </tr>

			        <tr>
			            <td>Encryption key:</td>
			            <td>
			            	<g:select name="encryptionKey" from="${availableKeys}" optionKey="key" optionValue="value" />
			            	<br/>
			                <small>Key used for digital encryption of SAML messages.</small>
			            </td>
			        </tr>

			        <tr>
			            <td>SSL/TLS key:</td>
			            <td>
			            	<g:select name="tlsKey" from="${availableKeys}" optionKey="key" optionValue="value" />
			            	<br/>
			                <small>Key used to authenticate this instance for SSL/TLS connections.</small>
			            </td>
			        </tr>

			        <tr>
			            <td>Sign metadata:</td>
			            <td>
							<g:checkBox name="signMetadata" />
			                <br/>
			                <small>If true the generated metadata will be digitally signed using the specified signature key.
			                </small>
			            </td>
			        </tr>


			        <tr>
			            <td>Sign sent AuthNRequests:</td>
			            <td>
							<g:checkBox name="requestSigned" />
			            </td>
			        </tr>
			        <tr>
			            <td>Require signed authentication Assertion:</td>
			            <td>
							<g:checkBox name="wantAssertionSigned" />
			            </td>
			        </tr>
			        <tr>
			            <td>Require signed LogoutRequest:</td>
			            <td>
							<g:checkBox name="requireLogoutRequestSigned" />
			            </td>
			        </tr>
			        <tr>
			            <td>Require signed LogoutResponse:</td>
			            <td>
							<g:checkBox name="requireLogoutResponseSigned" />
			            </td>
			        </tr>
			        <tr>
			            <td>Require signed ArtifactResolve:</td>
			            <td>
							<g:checkBox name="requireArtifactResolveSigned" />
			            </td>
			        </tr>

			        <tr>
			            <td colspan="2">
			                <br/>
			                <input type="submit" value="Generate metadata"/>
			            </td>
			        </tr>

			    </table>
			</g:form>

		</div>
	</body>
</html>		