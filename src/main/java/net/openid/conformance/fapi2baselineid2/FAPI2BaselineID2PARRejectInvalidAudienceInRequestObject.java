package net.openid.conformance.fapi2baselineid2;

import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.AddAudToRequestObject;
import net.openid.conformance.condition.client.AddPAREndpointAsAudToRequestObject;
import net.openid.conformance.condition.client.EnsureInvalidRequestUriError;
import net.openid.conformance.condition.client.EnsurePARInvalidRequestObjectError;
import net.openid.conformance.condition.client.ExpectInvalidAudienceErrorPage;
import net.openid.conformance.sequence.ConditionSequence;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.variant.VariantNotApplicable;

//PAR-2.0.1/JAR-4.0: The value of "aud" should be the value of the Authorization Server (AS) issuer
@PublishTestModule(
	testName = "fapi2-baseline-id2-par-pushed-authorization-url-as-audience-in-request-object",
	displayName = "PAR : try to use pushed authorization request endpoint url as audience in request object",
	summary = "This test tries to use the pushed authorization request endpoint url as audience in request object, the authorization server is expected to reject the request - depending when the server chooses to verify the request object, the refusal may be an error from the pushed authorization request endpoint, or may be returned from the authorization endpoint.",
	profile = "FAPI2-Baseline-ID2",
	configurationFields = {
		"server.discoveryUrl",
		"client.client_id",
		"client.scope",
		"client.jwks",
		"mtls.key",
		"mtls.cert",
		"mtls.ca",
		"client2.client_id",
		"client2.scope",
		"client2.jwks",
		"mtls2.key",
		"mtls2.cert",
		"mtls2.ca",
		"resource.resourceUrl"
	}
)
public class FAPI2BaselineID2PARRejectInvalidAudienceInRequestObject extends AbstractFAPI2BaselineID2ExpectingAuthorizationEndpointPlaceholderOrCallback {

	@Override
	protected void createPlaceholder() {
		callAndStopOnFailure(ExpectInvalidAudienceErrorPage.class, "FAPI1-BASE-5.2.2-9");

		env.putString("error_callback_placeholder", env.getString("invalid_aud_error"));
	}

	@Override
	protected ConditionSequence makeCreateAuthorizationRequestObjectSteps() {
		return super.makeCreateAuthorizationRequestObjectSteps().
			replace(AddAudToRequestObject.class, condition(AddPAREndpointAsAudToRequestObject.class).requirement("PAR-2"));
	}

	@Override
	protected void processParResponse() {
		// the server could reject this at the par endpoint, or at the authorization endpoint
		String key = "pushed_authorization_endpoint_response_http_status";
		Integer http_status = env.getInteger(key);
		if (http_status >= 200 && http_status < 300) {
			super.processParResponse();
			return;
		}

		callAndContinueOnFailure(EnsurePARInvalidRequestObjectError.class, Condition.ConditionResult.FAILURE, "JAR-6.2","PAR-2.3");

		fireTestFinished();
	}

	@Override
	protected void onAuthorizationCallbackResponse() {
		// This may be overly strict; https://tools.ietf.org/html/draft-ietf-oauth-jwsreq-26#section-4 only says 'should':
		// > The value of "aud" should be the value of the Authorization Server (AS) "issuer" as defined in RFC8414
		// It might be better to try setting 'aud' to a completely different server, which we would expect to be reject.
		callAndContinueOnFailure(EnsureInvalidRequestUriError.class, Condition.ConditionResult.FAILURE);
		fireTestFinished();
	}
}
