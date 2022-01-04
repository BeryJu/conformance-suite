package net.openid.conformance.openbanking_brasil.creditOperations.loans;

import com.google.gson.JsonObject;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.condition.client.AbstractJsonAssertingCondition;
import net.openid.conformance.logging.ApiName;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.util.field.StringField;

import java.util.Set;

/**
 * Api: swagger_loans_apis.yaml
 * Api endpoint: /contracts
 * Api git hash: 127e9783733a0d53bde1239a0982644015abe4f1
 */

@ApiName("Get Loans")
public class GetLoansResponseValidator extends AbstractJsonAssertingCondition {

	@Override
	@PreEnvironment(strings = "resource_endpoint_response")
	public Environment evaluate(Environment environment) {
		JsonObject body = bodyFrom(environment);
		assertHasField(body, ROOT_PATH);
		assertJsonArrays(body, ROOT_PATH, this::assertInnerFields);

		return environment;
	}

	private void assertInnerFields(JsonObject body) {
		final Set<String> productType = Set.of("EMPRESTIMOS");
		final Set<String> contractProductSubTypes = Set.of("HOME_EQUITY", "CHEQUE_ESPECIAL",
			"CONTA_GARANTIDA", "CAPITAL_GIRO_TETO_ROTATIVO",
			"CREDITO_PESSOAL_SEM_CONSIGNACAO", "CREDITO_PESSOAL_COM_CONSIGNACAO",
			"MICROCREDITO_PRODUTIVO_ORIENTADO", "CAPITAL_GIRO_PRAZO_VENCIMENTO_ATE_365_DIAS",
			"CAPITAL_GIRO_PRAZO_VENCIMENTO_SUPERIOR_365_DIAS");

		assertField(body,
			new StringField
				.Builder("contractId")
				.setPattern("^[a-zA-Z0-9][a-zA-Z0-9\\-]{0,99}$")
				.setMaxLength(100)
				.build());

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
				.Builder("productType")
				.setMaxLength(11)
				.setEnums(productType)
				.build());

		assertField(body,
			new StringField
				.Builder("productSubType")
				.setMaxLength(47)
				.setEnums(contractProductSubTypes)
				.build());

		assertField(body,
			new StringField
				.Builder("ipocCode")
				.setMaxLength(67)
				.build());
	}
}
