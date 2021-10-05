package net.openid.conformance.openbanking_brasil.account;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.condition.client.AbstractJsonAssertingCondition;
import net.openid.conformance.logging.ApiName;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.util.field.ArrayField;
import net.openid.conformance.util.field.StringField;

import java.util.Set;

/**
 *  * API: https://github.com/OpenBanking-Brasil/areadesenvolvedor/blob/gh-pages/swagger/swagger_accounts_apis.yaml
 *  * URL: /accounts
 *  * Api git hash: f14f533cf29fdcef0a3ad38e2f49e1f31c5ab7b2
 **/
@ApiName("Accounts list")
public class AccountListValidator extends AbstractJsonAssertingCondition {

	@Override
	@PreEnvironment(strings = "resource_endpoint_response")
	public Environment evaluate(Environment environment) {
		JsonObject body = bodyFrom(environment);

		assertHasField(body, ROOT_PATH);
		assertField(body,
			new ArrayField.Builder("data")
				.setMinItems(1)
				.build());
		assertJsonArrays(body, ROOT_PATH, this::assertInnerFields);

		return environment;
	}

	private void assertInnerFields(JsonObject body) {

		Set<String> enumType = Sets.newHashSet("CONTA_DEPOSITO_A_VISTA", "CONTA_POUPANCA", "CONTA_PAGAMENTO_PRE_PAGA");

		assertField(body,
			new StringField
				.Builder("brandName")
				.setPattern("[\\w\\W\\s]*")
				.setMaxLength(80)
				.build());

		assertField(body,
			new StringField
				.Builder("companyCnpj")
				.setPattern("\\d{14}|^NA$")
				.setMaxLength(14)
				.build());

		assertField(body,
			new StringField
				.Builder("type")
				.setEnums(enumType)
				.setMaxLength(24)
				.build());

		assertField(body,
			new StringField
				.Builder("compeCode")
				.setPattern("\\d{3}|^NA$")
				.setMaxLength(3)
				.build());

		assertField(body,
			new StringField
				.Builder("branchCode")
				.setPattern("\\d{4}|^NA$")
				.setMaxLength(4)
				.build());

		assertField(body,
			new StringField
				.Builder("number")
				.setPattern("^\\d{8,20}$|^NA$")
				.setMaxLength(20)
				.build());

		assertField(body,
			new StringField
				.Builder("checkDigit")
				.setPattern("[\\w\\W\\s]*")
				.setMaxLength(1)
				.build());

		assertField(body,
			new StringField
				.Builder("accountId")
				.setPattern("^[a-zA-Z0-9][a-zA-Z0-9\\-]{0,99}$")
				.setMaxLength(100)
				.build());


	}
}
