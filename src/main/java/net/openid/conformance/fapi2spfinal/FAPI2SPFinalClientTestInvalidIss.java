package net.openid.conformance.fapi2spfinal;

import net.openid.conformance.condition.as.AddInvalidIssValueToIdToken;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "fapi2-security-profile-final-client-test-invalid-iss",
	displayName = "FAPI2-Security-Profile-Final: client test - invalid iss in id_token from token_endpoint, should be rejected",
	summary = "This test should end with the client displaying an error message that the iss value in the id_token does not match the authorization server's issuer",
	profile = "FAPI2-Security-Profile-Final",
	configurationFields = {
		"server.jwks",
		"client.client_id",
		"client.scope",
		"client.redirect_uri",
		"client.certificate",
		"client.jwks",
		"waitTimeoutSeconds"
	}
)

public class FAPI2SPFinalClientTestInvalidIss extends AbstractFAPI2SPFinalClientExpectNothingAfterIdTokenIssued {

	@Override
	protected void addCustomValuesToIdToken() {

		callAndStopOnFailure(AddInvalidIssValueToIdToken.class, "OIDCC-3.1.3.7-2");
	}

	@Override
	protected String getIdTokenFaultErrorMessage() {
		return "invalid iss value";
	}
}
