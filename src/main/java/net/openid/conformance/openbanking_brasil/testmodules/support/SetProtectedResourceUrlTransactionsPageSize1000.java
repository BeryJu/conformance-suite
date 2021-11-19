package net.openid.conformance.openbanking_brasil.testmodules.support;

import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.condition.PostEnvironment;
import net.openid.conformance.testmodule.Environment;

public class SetProtectedResourceUrlTransactionsPageSize1000 extends AbstractCondition {

	@Override
	@PreEnvironment(strings = "accountId") 
	@PostEnvironment(strings = "protected_resource_url")
	public Environment evaluate(Environment env) {

		String resourceUrl = env.getString("protected_resource_url");
		String accountId = env.getString("accountId");
		resourceUrl = String.format("%s/%s/transactions?page-size=1000", resourceUrl, accountId);
		env.putString("protected_resource_url", resourceUrl);
		logSuccess("URL for account transactions set up");
		return env;
	}
}

