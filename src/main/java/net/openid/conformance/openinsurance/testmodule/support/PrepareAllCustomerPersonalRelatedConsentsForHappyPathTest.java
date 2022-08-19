package net.openid.conformance.openinsurance.testmodule.support;

import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.testmodule.Environment;

public class PrepareAllCustomerPersonalRelatedConsentsForHappyPathTest extends AbstractCondition {

	@Override
	public Environment evaluate(Environment env) {
		String[] permissions = {"CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ",
			"CUSTOMERS_PERSONAL_ADITTIONALINFO_READ", "RESOURCES_READ"};
		env.putString("consent_permissions", String.join(" ", permissions));
		return env;
	}
}
