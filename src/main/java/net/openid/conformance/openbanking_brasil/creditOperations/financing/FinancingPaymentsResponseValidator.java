package net.openid.conformance.openbanking_brasil.creditOperations.financing;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.condition.client.AbstractJsonAssertingCondition;
import net.openid.conformance.logging.ApiName;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.util.field.*;

import java.util.Set;

/**
 * API: swagger_financings_apis.yaml
 * URL: /contracts/{contractId}/payments
 * Api git hash: 127e9783733a0d53bde1239a0982644015abe4f1
 */

@ApiName("Financing Payments")
public class FinancingPaymentsResponseValidator extends AbstractJsonAssertingCondition {

	public static final Set<String> ENUM_CHARGE_TYPE = Sets.newHashSet("JUROS_REMUNERATORIOS_POR_ATRASO", "MULTA_ATRASO_PAGAMENTO", "JUROS_MORA_ATRASO", "IOF_CONTRATACAO", "IOF_POR_ATRASO", "SEM_ENCARGO", "OUTROS");

	@Override
	@PreEnvironment(strings = "resource_endpoint_response")
	public Environment evaluate(Environment environment) {

		JsonObject body = bodyFrom(environment);
		assertHasField(body, ROOT_PATH);
		assertInnerFields(body);

		return environment;
	}

	private void assertInnerFields(JsonObject body) {
		JsonObject data = findByPath(body, "$.data").getAsJsonObject();

		assertField(data,
			new IntField
				.Builder("paidInstalments")
				.setNullable()
				.setMaxLength(3)
				.build());

		assertField(data,
			new DoubleField
				.Builder("contractOutstandingBalance")
				.setPattern("^-?\\d{1,15}\\.\\d{2,4}$")
				.build());

		assertReleases(data);
	}

	private void assertReleases(JsonObject data) {
		assertField(data,
			new ArrayField
				.Builder("releases")
				.build());

		assertJsonArrays(data, "releases", this::assertInnerReleases);
	}

	private void assertInnerReleases(JsonObject body) {
		assertField(body,
			new StringField
				.Builder("paymentId")
				.setOptional()
				.setMaxLength(100)
				.build());

		assertField(body,
			new BooleanField
				.Builder("isOverParcelPayment")
				.build());

		assertField(body,
			new StringField
				.Builder("instalmentId")
				.setMaxLength(100)
				.build());

		assertField(body,
			new DatetimeField
				.Builder("paidDate")
				.setMaxLength(10)
				.setPattern("^(\\d{4})-(1[0-2]|0?[1-9])-(3[01]|[12][0-9]|0?[1-9])$")
				.build());

		assertField(body,
			new StringField
				.Builder("currency")
				.setMaxLength(3)
				.build());

		assertField(body,
			new DoubleField
				.Builder("paidAmount")
				.setPattern("^-?\\d{1,15}\\.\\d{2,4}$")
				.build());

		assertHasField(body, "overParcel");
		assertInnerFieldOverParcel(body);
	}

	private void assertInnerFieldOverParcel(JsonObject body) {
		JsonObject data = findByPath(body, "overParcel").getAsJsonObject();
		assertField(data,
			new ArrayField
				.Builder("fees")
				.build());

		assertField(data,
			new ArrayField
				.Builder("charges")
				.build());

		assertJsonArrays(data, "fees", this::assertInnerFieldFees);
		assertJsonArrays(data, "charges", this::assertInnerFieldsCharges);
	}

	private void assertInnerFieldFees(JsonObject body) {
		assertField(body,
			new StringField
				.Builder("feeName")
				.setMaxLength(140)
				.setPattern("[\\w\\W\\s]*")
				.build());

		assertField(body,
			new StringField
				.Builder("feeCode")
				.setMaxLength(140)
				.setPattern("[\\w\\W\\s]*")
				.build());

		assertField(body,
			new DoubleField
				.Builder("feeAmount")
				.setPattern("^-?\\d{1,15}\\.\\d{2,4}$")
				.setNullable()
				.build());
	}

	private void assertInnerFieldsCharges(JsonObject body) {

		assertField(body,
			new StringField
				.Builder("chargeType")
				.setMaxLength(31)
				.setEnums(ENUM_CHARGE_TYPE)
				.build());

		assertField(body,
			new StringField
				.Builder("chargeAdditionalInfo")
				.setMaxLength(140)
				.setPattern("[\\w\\W\\s]*")
				.build());

		assertField(body,
			new DoubleField
				.Builder("chargeAmount")
				.setPattern("^-?\\d{1,15}\\.\\d{2,4}$")
				.build());
	}
}
