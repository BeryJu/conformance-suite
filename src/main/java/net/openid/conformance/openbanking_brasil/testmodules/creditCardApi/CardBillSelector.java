package net.openid.conformance.openbanking_brasil.testmodules.creditCardApi;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PostEnvironment;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.testmodule.OIDFJSON;

public class CardBillSelector extends AbstractCondition {

	@Override
	public Environment evaluate(Environment env) {
		String entityString = env.getString("resource_endpoint_response");
		JsonObject accountList = new JsonParser().parse(entityString).getAsJsonObject();

		JsonArray data = accountList.getAsJsonArray("data");
		if(data.size() <= 0) {
			throw error("Data field is empty, no further processing required.");
		}

		JsonObject firstAccount = data.get(0).getAsJsonObject();
		String billId = OIDFJSON.getString(firstAccount.get("billId"));
		env.putString("billId", billId);
		return env;
	}

}
