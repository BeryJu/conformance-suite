package net.openid.conformance.openbanking_brasil.testmodules.support;

import com.google.gson.JsonObject;
import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.testmodule.Environment;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class AddExpirationInThreeMinute extends AbstractCondition {

	@Override
	@PreEnvironment(required = "consent_endpoint_request")
	public Environment evaluate(Environment env) {
		int minutes = 3;
		Instant expiryTime = Instant.now().plus(minutes, ChronoUnit.MINUTES);
		Instant expiryTimeNoFractionalSeconds = expiryTime.truncatedTo(ChronoUnit.SECONDS);
		String rfc3339ExpiryTime = DateTimeFormatter.ISO_INSTANT.format(expiryTimeNoFractionalSeconds);

		JsonObject consentRequest = env.getObject("consent_endpoint_request");
		JsonObject data = consentRequest.getAsJsonObject("data");

		data.addProperty("expirationDateTime", rfc3339ExpiryTime);
		logSuccess(String.format("Set expiry date to be in %s minutes", minutes), args("expiry", rfc3339ExpiryTime));
		return env;
	}
}
