package net.openid.conformance.openid.federation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.CallTokenEndpointAndReturnFullResponse;
import net.openid.conformance.condition.client.CheckIfAuthorizationEndpointError;
import net.openid.conformance.condition.client.CheckIfTokenEndpointResponseError;
import net.openid.conformance.condition.client.CheckStateInAuthorizationResponse;
import net.openid.conformance.condition.client.CreateTokenEndpointRequestForAuthorizationCodeGrant;
import net.openid.conformance.condition.client.EnsureContentTypeJson;
import net.openid.conformance.condition.client.EnsureNotFoundError;
import net.openid.conformance.condition.client.ExtractAuthorizationCodeFromAuthorizationResponse;
import net.openid.conformance.condition.client.ExtractIdTokenFromTokenResponse;
import net.openid.conformance.condition.client.ValidateIssIfPresentInAuthorizationResponse;
import net.openid.conformance.openid.federation.client.ClientRegistration;
import net.openid.conformance.testmodule.AbstractRedirectServerTestModule;
import net.openid.conformance.testmodule.OIDFJSON;
import net.openid.conformance.variant.ServerMetadata;
import net.openid.conformance.variant.VariantConfigurationFields;
import net.openid.conformance.variant.VariantParameters;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import static net.openid.conformance.openid.federation.EntityUtils.appendWellKnown;
import static net.openid.conformance.openid.federation.EntityUtils.stripWellKnown;

@VariantParameters({
	ServerMetadata.class,
	ClientRegistration.class
})
@VariantConfigurationFields(parameter = ServerMetadata.class, value = "static", configurationFields = {
	"federation.entity_configuration"
})
public abstract class AbstractOpenIDFederationTest extends AbstractRedirectServerTestModule {

	public abstract void additionalConfiguration();

	protected boolean opToRpMode() {
		return "true".equals(env.getString("config", "internal.op_to_rp_mode"));
	}

	@Override
	public void configure(JsonObject config, String baseUrl, String externalUrlOverride, String baseMtlsUrl) {
		env.putString("base_url", baseUrl);
		env.putString("base_mtls_url", baseMtlsUrl);
		env.putObject("config", config);

		callAndStopOnFailure(ValidateEntityIdentifier.class, Condition.ConditionResult.FAILURE, "OIDFED-1.2");
		skipIfElementMissing("config", "federation.trust_anchor", Condition.ConditionResult.INFO,
			ValidateTrustAnchor.class, Condition.ConditionResult.FAILURE, "OIDFED-1.2");

		String entityIdentifier = env.getString("config", "federation.entity_identifier");
		eventLog.startBlock("Retrieve Entity Configuration for %s".formatted(entityIdentifier));

		callAndStopOnFailure(ExtractEntityIdentiferFromConfig.class, Condition.ConditionResult.FAILURE);

		if (ServerMetadata.STATIC.equals(getVariant(ServerMetadata.class))) {
			// This case is perhaps not applicable in the general case,
			// but f ex the leaf entities in the Swedish sandbox federation
			// do not publish their own entity configurations.
			callAndStopOnFailure(GetStaticEntityStatement.class, Condition.ConditionResult.FAILURE);
		} else {
			callAndStopOnFailure(ValidateFederationUrl.class, Condition.ConditionResult.FAILURE, "OIDFED-1.2");
			callAndStopOnFailure(CallEntityStatementEndpointAndReturnFullResponse.class, Condition.ConditionResult.FAILURE, "OIDFED-9");
			validateEntityStatementResponse();
		}
		eventLog.endBlock();

		callAndStopOnFailure(ExtractJWTFromFederationEndpointResponse.class,  "OIDFED-9");
		if (ServerMetadata.DISCOVERY.equals(getVariant(ServerMetadata.class))) {
			validateEntityStatement();
		}
		callAndStopOnFailure(SetPrimaryEntityStatement.class, Condition.ConditionResult.FAILURE);

		additionalConfiguration();

		setStatus(Status.CONFIGURED);
		fireSetupDone();
	}

	protected Object entityConfigurationResponse(String mapKey, Class<? extends Condition> signCondition) {
		setStatus(Status.RUNNING);

		env.mapKey("entity_configuration_claims", mapKey);
		callAndStopOnFailure(signCondition);
		env.unmapKey("entity_configuration_claims");
		String entityConfiguration = env.getString("signed_entity_statement");

		env.removeNativeValue("signed_entity_statement");
		setStatus(Status.WAITING);

		return ResponseEntity
			.status(HttpStatus.OK)
			.contentType(EntityUtils.ENTITY_STATEMENT_JWT)
			.body(entityConfiguration);
	}

	@Override
	protected void processCallback() {
		eventLog.startBlock("Verify authorization endpoint response");
		env.mapKey("authorization_endpoint_response", "callback_query_params");

		onAuthorizationCallbackResponse();

		eventLog.endBlock();
		fireTestFinished();
	}

	protected void onAuthorizationCallbackResponse() {
		callAndContinueOnFailure(ValidateIssIfPresentInAuthorizationResponse.class, Condition.ConditionResult.FAILURE, "OAuth2-iss-2");
		callAndStopOnFailure(CheckIfAuthorizationEndpointError.class);
		callAndContinueOnFailure(CheckStateInAuthorizationResponse.class, Condition.ConditionResult.FAILURE);
		callAndStopOnFailure(ExtractAuthorizationCodeFromAuthorizationResponse.class);
		handleSuccessfulAuthorizationEndpointResponse();
	}

	protected void handleSuccessfulAuthorizationEndpointResponse() {
		performPostAuthorizationFlow();
	}

	protected void performPostAuthorizationFlow() {
		// call the token endpoint and complete the flow
		createAuthorizationCodeRequest();
		redeemAuthorizationCode();
		onPostAuthorizationFlowComplete();
	}

	protected void createAuthorizationCodeRequest() {
		callAndStopOnFailure(CreateTokenEndpointRequestForAuthorizationCodeGrant.class);
	}

	//Originally called requestAuthorizationCode()
	protected void redeemAuthorizationCode() {
		String tokenEndpoint = env.getString("primary_entity_statement_jwt", "claims.metadata.openid_provider.token_endpoint");
		env.putString("token_endpoint", tokenEndpoint);
		callAndStopOnFailure(CallTokenEndpointAndReturnFullResponse.class);
		callAndStopOnFailure(CheckIfTokenEndpointResponseError.class);
		callAndStopOnFailure(ExtractIdTokenFromTokenResponse.class, "OIDCC-3.1.3.3", "OIDCC-3.3.3.3");
		env.putObject("token_endpoint_id_token", env.getObject("id_token"));
	}

	protected void onPostAuthorizationFlowComplete() {
		fireTestFinished();
	}

	protected void validateEntityStatement() {
		String entityStatementUrl = env.getString("federation_endpoint_url");

		eventLog.startBlock("Validate basic claims in Entity Statement for %s".formatted(entityStatementUrl));
		callAndContinueOnFailure(ExtractRegisteredClaimsFromFederationResponse.class, Condition.ConditionResult.FAILURE, "OIDFED-3");
		env.putString("expected_iss", stripWellKnown(entityStatementUrl));
		env.putString("expected_sub", stripWellKnown(entityStatementUrl));
		call(sequence(ValidateFederationResponseBasicClaimsSequence.class));
		eventLog.endBlock();

		eventLog.startBlock("Validate JWKs and signature in Entity Statement for %s".formatted(entityStatementUrl));
		callAndContinueOnFailure(ExtractJWKsFromEntityStatement.class, Condition.ConditionResult.FAILURE, "OIDFED-3");
		call(sequence(ValidateFederationResponseSignatureSequence.class));
		eventLog.endBlock();

		eventLog.startBlock("Validate metadata in Entity Statement for %s".formatted(entityStatementUrl));
		callAndContinueOnFailure(ValidateEntityStatementMetadata.class, Condition.ConditionResult.INFO, "OIDFED-5");
		eventLog.endBlock();

		eventLog.startBlock("Validate Federation Entity metadata for %s".formatted(entityStatementUrl));
		callAndContinueOnFailure(ValidateFederationEntityMetadata.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.1");
		eventLog.endBlock();

		eventLog.startBlock("Validate OpenID Connect Relying Party metadata for %s".formatted(entityStatementUrl));
		callAndContinueOnFailure(ExtractOpenIDConnectRelyingPartyMetadata.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.2");
		validateOpenIDRelyingPartyMetadata();
		eventLog.endBlock();

		eventLog.startBlock("Validate OpenID Connect OpenID Provider metadata for %s".formatted(entityStatementUrl));
		callAndContinueOnFailure(ExtractOpenIDProviderMetadata.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.3");
		validateOpenIdProviderMetadata();
		eventLog.endBlock();

		eventLog.startBlock("Validate OAuth Authorization Server metadata for %s".formatted(entityStatementUrl));
		callAndContinueOnFailure(ValidateOAuthAuthorizationServerMetadata.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.4");
		eventLog.endBlock();

		eventLog.startBlock("Validate OAuth Client metadata for %s".formatted(entityStatementUrl));
		callAndContinueOnFailure(ValidateOAuthClientMetadata.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.5");
		eventLog.endBlock();

		eventLog.startBlock("Validate OAuth Protected Resource metadata for %s".formatted(entityStatementUrl));
		callAndContinueOnFailure(ValidateOAuthProtectedResourceMetadata.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.6");
		eventLog.endBlock();
	}

	protected void validateEntityStatementResponse() {
		env.mapKey("endpoint_response", "federation_endpoint_response");
		call(sequence(ValidateEntityStatementResponseSequence.class));
		env.unmapKey("endpoint_response");
	}

	protected void validateListResponse() {
		env.mapKey("endpoint_response", "federation_endpoint_response");
		call(sequence(ValidateListResponseSequence.class));
		env.unmapKey("endpoint_response");
	}

	protected void validateFetchResponse() {
		env.mapKey("endpoint_response", "federation_endpoint_response");
		call(sequence(ValidateFetchResponseSequence.class));
		env.unmapKey("endpoint_response");
	}

	protected void validateFetchErrorResponse() {
		env.mapKey("endpoint_response", "federation_endpoint_response");
		callAndContinueOnFailure(EnsureContentTypeJson.class, Condition.ConditionResult.FAILURE, "OIDFED-8.1.2");
		callAndContinueOnFailure(EnsureResponseIsJsonObject.class, Condition.ConditionResult.FAILURE, "OIDFED-8.1.2");
		env.unmapKey("endpoint_response");

		env.mapKey("authorization_endpoint_response", "endpoint_response_body");
		skipIfMissing(new String[]{"authorization_endpoint_response"}, null, Condition.ConditionResult.FAILURE, EnsureNotFoundError.class, Condition.ConditionResult.FAILURE, "OIDFED-8.1.2");
		env.unmapKey("authorization_endpoint_response");
	}

	protected void validateResolveResponse() {
		env.mapKey("endpoint_response", "federation_endpoint_response");
		call(sequence(ValidateResolveResponseSequence.class));
		env.unmapKey("endpoint_response");
	}

	protected void validateOpenIDRelyingPartyMetadata() {
		if (env.containsObject("openid_relying_party_metadata")) {
			env.mapKey("client", "openid_relying_party_metadata");
			call(sequence(ValidateOpenIDRelyingPartyMetadataSequence.class));
			callAndContinueOnFailure(ValidateClientRegistrationTypes.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.2");
			callAndContinueOnFailure(ValidateClientRegistrationTypesValues.class, Condition.ConditionResult.WARNING, "OIDFED-5.1.2");
			env.unmapKey("client");
			env.removeObject("openid_relying_party_metadata");
		}
	}

	protected void validateOpenIdProviderMetadata() {
		if (env.containsObject("openid_provider_metadata")) {
			env.mapKey("server", "openid_provider_metadata");
			String registrationEndpoint = env.getString("openid_provider_metadata", "registration_endpoint");
			net.openid.conformance.variant.ClientRegistration clientRegistration = registrationEndpoint != null
				? net.openid.conformance.variant.ClientRegistration.DYNAMIC_CLIENT
				: net.openid.conformance.variant.ClientRegistration.STATIC_CLIENT;
			call(new ValidateDiscoveryMetadataSequence(clientRegistration));
			callAndContinueOnFailure(ValidateClientRegistrationTypesSupported.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.3");
			skipIfElementMissing("openid_provider_metadata", "client_registration_types_supported", Condition.ConditionResult.INFO,
				ValidateClientRegistrationTypesSupportedValues.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.3");
			skipIfElementMissing("openid_provider_metadata", "client_registration_types_supported", Condition.ConditionResult.INFO,
				ValidateFederationRegistrationEndpoint.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.3");
			skipIfElementMissing("openid_provider_metadata", "client_registration_types_supported", Condition.ConditionResult.INFO,
				ValidateRequestAuthenticationMethodsSupported.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.3");
			skipIfElementMissing("openid_provider_metadata", "request_authentication_methods_supported", Condition.ConditionResult.INFO,
				ValidateRequestAuthenticationSigningAlgValuesSupported.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.3");
			env.unmapKey("server");
			env.removeObject("openid_provider_metadata");
		}
	}

	protected void validateAbsenceOfMetadataPolicy() {
		String entity = env.getString("federation_endpoint_url");
		eventLog.startBlock("Validate that Entity Statement for %s does not have a metadata_policy".formatted(entity));
		callAndContinueOnFailure(ValidateAbsenceOfMetadataPolicy.class, Condition.ConditionResult.FAILURE, "OIDFED-3");
		eventLog.endBlock();
	}

	protected void validateImmediateSuperiors() {
		String entity = env.getString("federation_endpoint_url");
		String anchor = env.getString("config", "federation.trust_anchor");
		// authority_hints is REQUIRED in Entity Configurations of the Entities that have at least one Superior above them,
		// such as Leaf and Intermediate Entities. This claim MUST NOT be present in Entity Configurations of Trust Anchors with no Superiors.
		if (!entity.startsWith(anchor)) {
			eventLog.startBlock("Validate authority hints in Entity Statement for %s".formatted(entity));
			callAndContinueOnFailure(ValidateAuthorityHints.class, Condition.ConditionResult.FAILURE, "OIDFED-3");
		} else {
			eventLog.startBlock("Validate absence of authority hints in Entity Statement for configured trust anchor %s".formatted(entity));
			callAndContinueOnFailure(ValidateAbsenceOfAuthorityHints.class, Condition.ConditionResult.FAILURE, "OIDFED-3");
		}
		validateSubordinateStatements();
		eventLog.endBlock();
	}

	protected void validateSubordinateStatements() {
		JsonElement authorityHintsElement = env.getElementFromObject("federation_response_jwt", "claims.authority_hints");
		if (authorityHintsElement != null) {
			JsonArray authorityHints = authorityHintsElement.getAsJsonArray();
			for (JsonElement authorityHintElement : authorityHints) {
				String authorityHint = OIDFJSON.getString(authorityHintElement);
				String authorityHintUrl = appendWellKnown(authorityHint);

				// Get the entity statement for the superior
				env.putString("federation_endpoint_url", authorityHintUrl);
				callAndStopOnFailure(ValidateFederationUrl.class, Condition.ConditionResult.FAILURE, "OIDFED-1.2");
				callAndStopOnFailure(CallEntityStatementEndpointAndReturnFullResponse.class, Condition.ConditionResult.FAILURE, "OIDFED-9");
				validateEntityStatementResponse();
				callAndStopOnFailure(ExtractJWTFromFederationEndpointResponse.class,  "OIDFED-9");
				validateEntityStatement();

				eventLog.startBlock("Validating subordinate statement by immediate superior %s".formatted(authorityHint));

				// Verify that the primary entity is present in the list endpoint result
				callAndStopOnFailure(ExtractFederationListEndpoint.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.1");
				callAndStopOnFailure(ValidateFederationUrl.class, Condition.ConditionResult.FAILURE, "OIDFED-1.2");
				callAndStopOnFailure(CallListEndpointAndReturnFullResponse.class, Condition.ConditionResult.FAILURE, "OIDFED-8.2.1");
				validateListResponse();
				callAndContinueOnFailure(VerifyPrimaryEntityPresenceInSubordinateListing.class, Condition.ConditionResult.FAILURE, "OIDFED-8.2");

				// Get the entity statement from the Superior's fetch endpoint
				env.putString("expected_sub", env.getString("primary_entity_statement_iss"));
				callAndStopOnFailure(ExtractFederationFetchEndpoint.class, Condition.ConditionResult.FAILURE, "OIDFED-8.1.1");
				callAndStopOnFailure(ValidateFederationUrl.class, Condition.ConditionResult.FAILURE, "OIDFED-1.2");

				callAndContinueOnFailure(AppendSubToFederationEndpointUrl.class, Condition.ConditionResult.FAILURE, "OIDFED-8.1.1");
				callAndStopOnFailure(CallFetchEndpointAndReturnFullResponse.class, Condition.ConditionResult.FAILURE, "OIDFED-8.1.1");
				validateFetchResponse();
				callAndStopOnFailure(ExtractJWTFromFederationEndpointResponse.class,  "OIDFED-8.1.2");

				call(sequence(ValidateFederationResponseSignatureSequence.class));

				callAndContinueOnFailure(ExtractRegisteredClaimsFromFederationResponse.class, Condition.ConditionResult.FAILURE, "OIDFED-3");
				call(sequence(ValidateFederationResponseBasicClaimsSequence.class));

				callAndContinueOnFailure(ValidateEntityStatementMetadata.class, Condition.ConditionResult.INFO, "OIDFED-5.1.1");
				// No authority hints in subordinate statements
				callAndContinueOnFailure(ValidateAbsenceOfAuthorityHints.class, Condition.ConditionResult.FAILURE, "OIDFED-3");
				// No federation_entity metadata in subordinate statements
				callAndContinueOnFailure(ValidateAbsenceOfFederationEntityMetadata.class, Condition.ConditionResult.FAILURE, "OIDFED-8.1");
				// Only Subordinate Statements may include this claim.
				callAndContinueOnFailure(ValidateEntityStatementMetadataPolicy.class, Condition.ConditionResult.FAILURE, "OIDFED-6.1.2");

				eventLog.endBlock();
			}
		}
	}

	protected List<String> findPath(String fromEntity, String toTrustAnchor) throws CyclicPathException {
		 List<String> path = findPath(fromEntity, toTrustAnchor, new ArrayList<>());
		 if (path != null) {
			 eventLog.log(getName(), "Path to trust anchor: %s".formatted(String.join(" → ", path)));
			 return path;
		 } else {
			 eventLog.log(getName(), "Unable to find path from %s to trust anchor %s".formatted(fromEntity, toTrustAnchor));
			 return List.of();
		 }
	 }

	private List<String> findPath(String fromEntity, String toTrustAnchor, List<String> path) throws CyclicPathException {

		if (path.isEmpty()) {
			env.mapKey("federation_response_jwt", "primary_entity_statement_jwt");
		} else {
			env.unmapKey("federation_response_jwt");

			String currentWellKnownUrl = appendWellKnown(fromEntity);
			env.putString("federation_endpoint_url", currentWellKnownUrl);

			callAndStopOnFailure(ValidateFederationUrl.class, Condition.ConditionResult.FAILURE, "OIDFED-1.2");
			callAndStopOnFailure(CallEntityStatementEndpointAndReturnFullResponse.class, Condition.ConditionResult.FAILURE, "OIDFED-9");
			validateEntityStatementResponse();
			callAndStopOnFailure(ExtractJWTFromFederationEndpointResponse.class,  "OIDFED-9");
		}

		if (path.contains(fromEntity)) {
			throw new CyclicPathException("Cyclic path detected. Entity %s already exists in the path: %s".formatted(fromEntity, String.join(" → ", path)));
		}

		path.add(fromEntity);

		if (EntityUtils.equals(fromEntity, toTrustAnchor)) {
			return path;
		}

		JsonElement authorityHintsElement = env.getElementFromObject("federation_response_jwt", "claims.authority_hints");
		if (authorityHintsElement == null) {
			return null;
		}
		JsonArray authorityHints = authorityHintsElement.getAsJsonArray();
		if (authorityHints.isJsonNull() || authorityHints.isEmpty()) {
			return null;
		}

		for (JsonElement authorityHintElement : authorityHints) {
			String authorityHint = OIDFJSON.getString(authorityHintElement);
			List<String> result = findPath(authorityHint, toTrustAnchor, new ArrayList<>(path));
			if (result != null) {
				return result;
			}
		}

		return null;
	}

	protected JsonArray buildTrustChain(List<String> path) {
		eventLog.startBlock("Building trust chain from %s to %s".formatted(path.get(0), path.get(path.size() - 1)));
		JsonArray trustChain = new JsonArray();
		trustChain.add(env.getString("primary_entity_statement_jwt", "value"));

		if (path.size() == 1) {
			return trustChain;
		}

		for (int i = 1; i < path.size(); i++) {
			String entityIdentifier = path.get(i);
			env.putString("federation_endpoint_url", appendWellKnown(entityIdentifier));
			callAndStopOnFailure(ValidateFederationUrl.class, Condition.ConditionResult.FAILURE, "OIDFED-1.2");
			callAndStopOnFailure(CallEntityStatementEndpointAndReturnFullResponse.class, Condition.ConditionResult.FAILURE, "OIDFED-9");
			validateEntityStatementResponse();
			callAndStopOnFailure(ExtractJWTFromFederationEndpointResponse.class,  "OIDFED-9");
			callAndContinueOnFailure(ExtractFederationEntityMetadataUrls.class, Condition.ConditionResult.FAILURE, "OIDFED-3");

			String fetchEndpoint = env.getString("federation_fetch_endpoint");
			env.putString("federation_endpoint_url", fetchEndpoint);
			String sub = path.get(i - 1);
			env.putString("expected_sub", sub);
			callAndStopOnFailure(ValidateFederationUrl.class, Condition.ConditionResult.FAILURE, "OIDFED-1.2");
			callAndContinueOnFailure(AppendSubToFederationEndpointUrl.class, Condition.ConditionResult.FAILURE, "OIDFED-8.1.1");
			callAndStopOnFailure(CallFetchEndpointAndReturnFullResponse.class, Condition.ConditionResult.FAILURE, "OIDFED-8.1.1");
			validateFetchResponse();
			callAndStopOnFailure(ExtractJWTFromFederationEndpointResponse.class,  "OIDFED-8.1.2");
			trustChain.add(OIDFJSON.getString(env.getElementFromObject("federation_response_jwt", "value")));
		}

		String trustAnchorEntityIdentifier = path.get(path.size() - 1);
		env.putString("federation_endpoint_url", appendWellKnown(trustAnchorEntityIdentifier));
		callAndStopOnFailure(ValidateFederationUrl.class, Condition.ConditionResult.FAILURE, "OIDFED-1.2");
		callAndStopOnFailure(CallEntityStatementEndpointAndReturnFullResponse.class, Condition.ConditionResult.FAILURE, "OIDFED-9");
		validateEntityStatementResponse();
		callAndStopOnFailure(ExtractJWTFromFederationEndpointResponse.class,  "OIDFED-9");
		trustChain.add(OIDFJSON.getString(env.getElementFromObject("federation_response_jwt", "value")));
		eventLog.endBlock();

		return trustChain;
	}

	public static class CyclicPathException extends Exception {

		@Serial
		private static final long serialVersionUID = 1L;

		public CyclicPathException(String message) {
			super(message);
		}

	}

}
