package net.openid.conformance.openid.ssf.conditions.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.testmodule.Environment;

public class OIDSSFGetOrWaitForPushRequest extends AbstractCondition {

	@Override
	public Environment evaluate(Environment env) {
		JsonObject pushRequestObject = getPushRequestObject(env);
		if (pushRequestObject == null) {
			throw error("Did not receive push request");
		}

		logSuccess("Detected push request object", args("push_request", pushRequestObject));

		return env;
	}

	protected JsonObject getPushRequestObject(Environment env) {
		JsonElement elementFromObject = env.getElementFromObject("ssf", "push_request");
		if (elementFromObject == null || !elementFromObject.isJsonObject()) {
			return null;
		}

		return elementFromObject.getAsJsonObject();
	}
}
