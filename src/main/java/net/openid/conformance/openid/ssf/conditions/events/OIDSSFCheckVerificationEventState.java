package net.openid.conformance.openid.ssf.conditions.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.openid.ssf.SsfEvents;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.testmodule.OIDFJSON;

public class OIDSSFCheckVerificationEventState extends AbstractCondition {

	@Override
	public Environment evaluate(Environment env) {
		JsonElement setClaimsEl = env.getElementFromObject("ssf", "verification.token.claims");
		if (setClaimsEl == null || !setClaimsEl.isJsonObject()) {
			throw error("Missing verification token claims", args("verification_token", env.getElementFromObject("ssf", "verification.token")));
		}
		JsonObject setClaimsJsonObject = setClaimsEl.getAsJsonObject();

		JsonObject eventsObject = setClaimsJsonObject.getAsJsonObject("events");
		if (eventsObject == null) {
			throw error("Expected to find events object in verification token claims", args("claims", setClaimsJsonObject));
		}
		String verificationEventKey = SsfEvents.SSF_STREAM_VERIFICATION_EVENT_TYPE;
		JsonElement eventsEL = eventsObject.get(verificationEventKey);
		if (eventsEL == null) {
			throw error("Expected to find verification events object", args("missing_key", verificationEventKey, "events_object", eventsObject));
		}

		JsonObject verificationEventObject = eventsEL.getAsJsonObject();

		String expectedVerificationState = env.getString("ssf", "verification.state");
		JsonElement actualVerificationStateEl = verificationEventObject.get("state");
		if (actualVerificationStateEl == null || actualVerificationStateEl.isJsonNull()) {
			throw error("Verification event is missing state", args("event", verificationEventObject, "claims", setClaimsJsonObject));
		}
		String actualVerificationState = OIDFJSON.getString(actualVerificationStateEl);
		if (!actualVerificationState.equals(expectedVerificationState)) {
			throw error("Verification state check failed due to state mismatch", args("expected_state", expectedVerificationState, "actual_state", actualVerificationState, "claims", setClaimsJsonObject));
		}

		logSuccess("Verified verification event state", args("expected_state", expectedVerificationState, "actual_state", actualVerificationState));

		return env;
	}

}
