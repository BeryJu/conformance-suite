package net.openid.conformance.openbanking_brasil.registrationData;

import com.google.gson.JsonObject;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.condition.client.AbstractJsonAssertingCondition;
import net.openid.conformance.logging.ApiName;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.util.field.ArrayField;
import net.openid.conformance.util.field.DatetimeField;
import net.openid.conformance.util.field.StringArrayField;
import net.openid.conformance.util.field.StringField;


import java.util.Set;

/**
 *  * API: https://github.com/OpenBanking-Brasil/areadesenvolvedor/blob/gh-pages/swagger/swagger_accounts_apis.yaml
 *  * URL: /business/financial-relations
 *  * Api git hash: 152a9f02d94d612b26dbfffb594640f719e96f70
 **/

@ApiName("Business Financial-relations")
public class BusinessRelationsResponseValidator extends AbstractJsonAssertingCondition {

	public static final Set<String> PRODUCT_SERVICE_TYPES = Set.of("CONTA_DEPOSITO_A_VISTA",
		"CONTA_POUPANCA", "CONTA_PAGAMENTO_PRE_PAGA", "CARTAO_CREDITO", "OPERACAO_CREDITO",
		"SEGURO", "PREVIDENCIA", "INVESTIMENTO", "OPERACOES_CAMBIO", "CONTA_SALARIO",
		"CREDENCIAMENTO", "OUTROS");
	public static final Set<String> TYPES = Set.of("CONTA_DEPOSITO_A_VISTA", "CONTA_POUPANCA",
		"CONTA_PAGAMENTO_PRE_PAGA", "SEM_TIPO_CONTA");
	public static final Set<String> TYPES1 = Set.of("REPRESENTANTE_LEGAL", "PROCURADOR", "NAO_POSSUI");

	@Override
	@PreEnvironment(strings = "resource_endpoint_response")
	public Environment evaluate(Environment environment) {
		JsonObject body = bodyFrom(environment);
		assertHasField(body, ROOT_PATH);
		JsonObject data = findByPath(body, ROOT_PATH).getAsJsonObject();
		assertData(data);

		return environment;
	}

	private void assertData(JsonObject body) {

		assertField(body, new DatetimeField.Builder("updateDateTime").build());
		assertField(body, new DatetimeField.Builder("startDate").build());

		assertField(body,
			new StringArrayField
				.Builder("productsServicesType")
				.setMinItems(1)
				.setMaxItems(12)
				.setEnums(PRODUCT_SERVICE_TYPES)
				.setMaxLength(24)
				.build());

		assertProcurators(body);
		assertAccounts(body);
	}

	private void assertAccounts(JsonObject body) {
		assertHasField(body, "accounts");

		assertField(body,
			new ArrayField.Builder("accounts")
				.setMinItems(1)
				.build());

		assertJsonArrays(body, "accounts",
			this::assertInnerFieldsForAccounts);
	}

	private void assertInnerFieldsForAccounts(JsonObject body) {

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
				.Builder("type")
				.setEnums(TYPES)
				.build());
	}

	private void assertProcurators(JsonObject body) {
		assertHasField(body, "procurators");

		assertField(body,
			new ArrayField.Builder("procurators")
				.setMinItems(1)
				.build());

		assertJsonArrays(body, "procurators",
			this::assertInnerFieldsForProcurators);
	}

	private void assertInnerFieldsForProcurators(JsonObject body) {

		assertField(body,
			new StringField
				.Builder("type")
				.setMaxLength(19)
				.setEnums(TYPES1)
				.build());

		assertField(body,
			new StringField
				.Builder("cnpjCpfNumber")
				.setMaxLength(14)
				.setPattern("^\\d{11}$|^\\d{14}$|^NA$")
				.build());

		assertField(body,
			new StringField
				.Builder("civilName")
				.setMaxLength(70)
				.setPattern("[\\w\\W\\s]*")
				.build());

		assertField(body,
			new StringField
				.Builder("socialName")
				.setMaxLength(70)
				.setPattern("[\\w\\W\\s]*")
				.build());
	}
}
