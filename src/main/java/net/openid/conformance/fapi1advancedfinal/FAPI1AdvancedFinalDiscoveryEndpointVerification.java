package net.openid.conformance.fapi1advancedfinal;

import com.google.gson.JsonObject;
import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.CheckDiscEndpointAcrClaimSupported;
import net.openid.conformance.condition.client.CheckDiscEndpointAuthorizationEndpoint;
import net.openid.conformance.condition.client.CheckDiscEndpointClaimsParameterSupported;
import net.openid.conformance.condition.client.CheckDiscEndpointPARSupported;
import net.openid.conformance.condition.client.CheckDiscEndpointRequestParameterSupported;
import net.openid.conformance.condition.client.CheckDiscEndpointUserinfoEndpoint;
import net.openid.conformance.condition.client.CheckDiscRequirePushedAuthorizationRequestsIsABoolean;
import net.openid.conformance.condition.client.CheckJwksUriIsHostedOnOpenBankingDirectory;
import net.openid.conformance.condition.client.FAPIAuCdrCheckDiscEndpointClaimsSupported;
import net.openid.conformance.condition.client.FAPIBrazilCheckDiscEndpointAcrValuesSupported;
import net.openid.conformance.condition.client.FAPIBrazilCheckDiscEndpointAcrValuesSupportedShould;
import net.openid.conformance.condition.client.FAPIBrazilCheckDiscEndpointCpfOrCnpjClaimSupported;
import net.openid.conformance.condition.client.FAPIBrazilCheckDiscEndpointGrantTypesSupported;
import net.openid.conformance.condition.client.FAPIBrazilCheckDiscEndpointScopesSupported;
import net.openid.conformance.condition.client.FAPICheckDiscEndpointRequestObjectEncryptionAlgValuesSupportedContainsRsaOaep;
import net.openid.conformance.condition.client.FAPICheckDiscEndpointRequestObjectEncryptionEncValuesSupportedContainsA256gcm;
import net.openid.conformance.condition.client.FAPICheckDiscEndpointRequestObjectSigningAlgValuesSupported;
import net.openid.conformance.condition.client.FAPIOBCheckDiscEndpointClaimsSupported;
import net.openid.conformance.condition.client.FAPIOBCheckDiscEndpointGrantTypesSupported;
import net.openid.conformance.condition.client.FAPIOBCheckDiscEndpointScopesSupported;
import net.openid.conformance.condition.client.FAPIRWCheckDiscEndpointGrantTypesSupported;
import net.openid.conformance.condition.client.FAPIRWCheckDiscEndpointJARMResponseModesSupported;
import net.openid.conformance.condition.client.CheckDiscEndpointResponseTypeCodeSupported;
import net.openid.conformance.condition.client.FAPIRWCheckDiscEndpointResponseTypesSupported;
import net.openid.conformance.condition.client.FAPIRWCheckDiscEndpointScopesSupported;
import net.openid.conformance.sequence.AbstractConditionSequence;
import net.openid.conformance.sequence.ConditionSequence;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.variant.FAPI1FinalOPProfile;
import net.openid.conformance.variant.FAPIAuthRequestMethod;
import net.openid.conformance.variant.FAPIResponseMode;
import net.openid.conformance.variant.VariantParameters;
import net.openid.conformance.variant.VariantSetup;

@PublishTestModule(
	testName = "fapi1-advanced-final-discovery-end-point-verification",
	displayName = "FAPI1-Advanced-Final: Discovery Endpoint Verification",
	summary = "This test ensures that the server's configuration (including scopes, response_types, grant_types etc) contains values required by the specification",
	profile = "FAPI1-Advanced-Final",
	configurationFields = {
		"server.discoveryUrl",
	}
)
@VariantParameters({
	FAPI1FinalOPProfile.class,
	FAPIResponseMode.class,
	FAPIAuthRequestMethod.class
})
public class FAPI1AdvancedFinalDiscoveryEndpointVerification extends AbstractFAPI1AdvancedFinalDiscoveryEndpointVerification {

	private Class<? extends ConditionSequence> profileSpecificChecks;

	protected Boolean jarm;

	protected Boolean par;

	protected boolean brazil = false;

	@VariantSetup(parameter = FAPI1FinalOPProfile.class, value = "plain_fapi")
	public void setupPlainFapi() {
		profileSpecificChecks = PlainFAPIDiscoveryEndpointChecks.class;
	}

	@VariantSetup(parameter = FAPI1FinalOPProfile.class, value = "openbanking_uk")
	public void setupOpenBankingUk() {
		profileSpecificChecks = OpenBankingUkDiscoveryEndpointChecks.class;
	}

	@VariantSetup(parameter = FAPI1FinalOPProfile.class, value = "consumerdataright_au")
	public void setupConsumerDataRightAu() {
		profileSpecificChecks = AuCdrDiscoveryEndpointChecks.class;
	}

	@VariantSetup(parameter = FAPI1FinalOPProfile.class, value = "openbanking_brazil")
	public void setupOpenBankingBrazil() {
		profileSpecificChecks = OpenBankingBrazilDiscoveryEndpointChecks.class;
		brazil = true;
	}

	@Override
	public void configure(JsonObject config, String baseUrl, String externalUrlOverride) {
		jarm = getVariant(FAPIResponseMode.class) == FAPIResponseMode.JARM;
		par = getVariant(FAPIAuthRequestMethod.class) == FAPIAuthRequestMethod.PUSHED;
		super.configure(config, baseUrl, externalUrlOverride);
	}

	@Override
	protected void performEndpointVerification() {

		if (jarm) {
			callAndContinueOnFailure(CheckDiscEndpointResponseTypeCodeSupported.class, Condition.ConditionResult.FAILURE, "JARM-4.1.1");
			callAndContinueOnFailure(FAPIRWCheckDiscEndpointJARMResponseModesSupported.class, Condition.ConditionResult.FAILURE, "JARM-4.3.4");
		} else {
			callAndContinueOnFailure(FAPIRWCheckDiscEndpointResponseTypesSupported.class, Condition.ConditionResult.FAILURE, "FAPI1-ADV-5.2.2-2");
		}

		if (par) {
			callAndContinueOnFailure(CheckDiscEndpointPARSupported.class, Condition.ConditionResult.FAILURE, "PAR-5");
		}

		super.performEndpointVerification();

		if (par) {
			callAndContinueOnFailure(CheckDiscRequirePushedAuthorizationRequestsIsABoolean.class, Condition.ConditionResult.FAILURE, "PAR-5");
		} else {
			callAndContinueOnFailure(CheckDiscEndpointRequestParameterSupported.class, Condition.ConditionResult.FAILURE, "FAPI1-ADV-5.2.2-1", "OIDCD-3");
		}

		callAndContinueOnFailure(FAPICheckDiscEndpointRequestObjectSigningAlgValuesSupported.class, Condition.ConditionResult.FAILURE);

		callAndContinueOnFailure(CheckDiscEndpointAuthorizationEndpoint.class, Condition.ConditionResult.FAILURE);

		call(sequence(profileSpecificChecks));

		if (brazil && !par) {
			// encrypted request object support is only required for redirect based flows
			callAndContinueOnFailure(FAPICheckDiscEndpointRequestObjectEncryptionAlgValuesSupportedContainsRsaOaep.class, Condition.ConditionResult.FAILURE, "BrazilOB-6.1.1-1");
			callAndContinueOnFailure(FAPICheckDiscEndpointRequestObjectEncryptionEncValuesSupportedContainsA256gcm.class, Condition.ConditionResult.FAILURE, "BrazilOB-6.1.1-1");
		}
	}

	public static class PlainFAPIDiscoveryEndpointChecks extends AbstractConditionSequence {

		@Override
		public void evaluate() {
			callAndContinueOnFailure(FAPIRWCheckDiscEndpointGrantTypesSupported.class, Condition.ConditionResult.FAILURE);
			callAndContinueOnFailure(FAPIRWCheckDiscEndpointScopesSupported.class, Condition.ConditionResult.FAILURE);
		}
	}

	public static class AuCdrDiscoveryEndpointChecks extends AbstractConditionSequence {

		@Override
		public void evaluate() {
			// claims parameter support is required in Australia
			callAndContinueOnFailure(CheckDiscEndpointClaimsParameterSupported.class, Condition.ConditionResult.FAILURE, "OIDCD-3", "FAPI1-ADV-5.2.3-3");

			callAndContinueOnFailure(FAPIAuCdrCheckDiscEndpointClaimsSupported.class, Condition.ConditionResult.FAILURE);

			callAndContinueOnFailure(FAPIRWCheckDiscEndpointGrantTypesSupported.class, Condition.ConditionResult.FAILURE);
			callAndContinueOnFailure(FAPIRWCheckDiscEndpointScopesSupported.class, Condition.ConditionResult.FAILURE);
		}
	}

	public static class OpenBankingUkDiscoveryEndpointChecks extends AbstractConditionSequence {

		@Override
		public void evaluate() {
			// OBUK servers are required to return acrs, which means they must support requesting the acr claim (as well
			// as the intent id claim), and hence must support the claims parameter
			callAndContinueOnFailure(CheckDiscEndpointClaimsParameterSupported.class, Condition.ConditionResult.FAILURE, "OIDCD-3", "FAPI1-ADV-5.2.3-3");

			callAndContinueOnFailure(CheckJwksUriIsHostedOnOpenBankingDirectory.class, Condition.ConditionResult.WARNING, "OBSP-3.4");

			callAndContinueOnFailure(FAPIOBCheckDiscEndpointClaimsSupported.class, Condition.ConditionResult.FAILURE, "OBSP-3.4");
			callAndContinueOnFailure(FAPIOBCheckDiscEndpointGrantTypesSupported.class, Condition.ConditionResult.FAILURE);
			callAndContinueOnFailure(FAPIOBCheckDiscEndpointScopesSupported.class, Condition.ConditionResult.FAILURE);
		}
	}

	public static class OpenBankingBrazilDiscoveryEndpointChecks extends AbstractConditionSequence {

		@Override
		public void evaluate() {
			callAndContinueOnFailure(CheckDiscEndpointClaimsParameterSupported.class, Condition.ConditionResult.FAILURE,
				"OIDCD-3", "BrazilOB-5.2.2-3");

			callAndContinueOnFailure(CheckDiscEndpointAcrClaimSupported.class, Condition.ConditionResult.FAILURE,
				"BrazilOB-5.2.2-3", "BrazilOB-5.2.2-6");
			callAndContinueOnFailure(FAPIBrazilCheckDiscEndpointCpfOrCnpjClaimSupported.class, Condition.ConditionResult.FAILURE,
				"BrazilOB-5.2.2-3", "BrazilOB-5.2.2-4", "BrazilOB-5.2.2-5");
			callAndContinueOnFailure(FAPIBrazilCheckDiscEndpointGrantTypesSupported.class, Condition.ConditionResult.FAILURE);
			callAndContinueOnFailure(FAPIBrazilCheckDiscEndpointAcrValuesSupported.class, Condition.ConditionResult.FAILURE, "BrazilOB-5.2.2-6");
			callAndContinueOnFailure(FAPIBrazilCheckDiscEndpointAcrValuesSupportedShould.class, Condition.ConditionResult.WARNING, "BrazilOB-5.2.2-7");
			callAndContinueOnFailure(CheckDiscEndpointUserinfoEndpoint.class, Condition.ConditionResult.FAILURE, "BrazilOB-5.2.2-8");
			callAndContinueOnFailure(FAPIBrazilCheckDiscEndpointScopesSupported.class, Condition.ConditionResult.FAILURE);
		}
	}
}
