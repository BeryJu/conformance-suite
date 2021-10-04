package net.openid.conformance.openbanking_brasil.paymentInitiation;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.condition.client.AbstractJsonAssertingCondition;
import net.openid.conformance.logging.ApiName;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.util.field.DatetimeField;
import net.openid.conformance.util.field.ObjectField;
import net.openid.conformance.util.field.StringField;

import java.util.Set;

/**
 * https://openbanking-brasil.github.io/areadesenvolvedor/swagger/swagger_payments_apis.yaml
 * Version: v1.0.0-rc8.8
 */

@ApiName("Payment Initiation Consent")
public class PaymentInitiationConsentValidator extends AbstractJsonAssertingCondition {
	@Override
	@PreEnvironment(required = "consent_endpoint_response")
	public Environment evaluate(Environment environment) {

		JsonObject body = environment.getObject("consent_endpoint_response");
		assertHasField(body, ROOT_PATH);
		assertJsonObject(body, ROOT_PATH, this::assertInnerFields);

		return environment;
	}

	private void assertInnerFields(JsonObject body) {
		Set<String> statusList = Sets.newHashSet("AWAITING_AUTHORISATION", "AUTHORISED", "REJECTED", "CONSUMED");

		assertField(body, CommonFields.consentId());

		assertField(body,
			new DatetimeField
				.Builder("creationDateTime")
				.setPattern(DatetimeField.ALTERNATIVE_PATTERN)
				.build());

		assertField(body,
			new DatetimeField
				.Builder("expirationDateTime")
				.setSecondsOlderThan(300, "creationDateTime")
				.setPattern(DatetimeField.ALTERNATIVE_PATTERN)
				.build());

		assertField(body,
			new DatetimeField
				.Builder("statusUpdateDateTime")
				.setPattern(DatetimeField.ALTERNATIVE_PATTERN)
				.build());

		assertField(body,
			new StringField
				.Builder("status")
				.setEnums(statusList)
				.setMaxLength(22)
				.build());

		assertJsonObject(body, "loggedUser", this::assertLoggedUser);

		assertField(body,
			new ObjectField
				.Builder("businessEntity")
				.setValidator(this::assertBusinessEntity)
				.setOptional()
				.build());

		assertJsonObject(body, "creditor", this::assertCreditor);
		assertJsonObject(body, "payment", this::assertPayment);

		assertField(body,
			new ObjectField
				.Builder("debtorAccount")
				.setValidator(this::assertDebtorAccount)
				.setOptional()
				.build());

	}

	private void assertLoggedUser(JsonObject loggedUser) {
		assertJsonObject(loggedUser, "document", this::assertLoggedUserDocument);
	}

	private void assertLoggedUserDocument(JsonObject document) {
		assertField(document,
			new StringField
				.Builder("identification")
				.setPattern("^\\d{11}$")
				.setMaxLength(11)
				.build());

		assertField(document,
			new StringField
				.Builder("rel")
				.setPattern("^[A-Z]{3}$")
				.setMaxLength(3)
				.build());
	}

	private void assertBusinessEntity(JsonObject businessEntity) {
		assertJsonObject(businessEntity, "document", this::assertBusinessEntityDocument);
	}

	private void assertBusinessEntityDocument(JsonObject document) {
		assertField(document,
			new StringField
				.Builder("identification")
				.setPattern("^\\d{14}$")
				.setMaxLength(14)
				.build());

		assertField(document,
			new StringField
				.Builder("rel")
				.setPattern("^[A-Z]{4}$")
				.setMaxLength(4)
				.build());
	}

	private void assertCreditor(JsonObject creditor) {
		Set<String> personTypes = Sets.newHashSet("PESSOA_NATURAL", "PESSOA_JURIDICA");

		assertField(creditor,
			new StringField
				.Builder("personType")
				.setEnums(personTypes)
				.setMaxLength(15)
				.build());

		assertField(creditor,
			new StringField
				.Builder("cpfCnpj")
				.setPattern("^\\d{11}$|^\\d{14}$")
				.setMinLength(11)
				.setMaxLength(14)
				.build());

		assertField(creditor,
			new StringField
				.Builder("name")
				.setPattern("[\\w\\W\\s]*")
				.setMaxLength(140)
				.build());
	}

	private void assertPayment(JsonObject payment) {
		Set<String> types = Sets.newHashSet("PIX");

		assertField(payment,
			new StringField
				.Builder("type")
				.setMaxLength(3)
				.setEnums(types)
				.build());

		assertField(payment,
			new DatetimeField
				.Builder("date")
				.setPattern("^(\\d{4})-(1[0-2]|0?[1-9])-(3[01]|[12][0-9]|0?[1-9])$")
				.setMaxLength(10)
				.build());

		assertField(payment,
			new StringField
				.Builder("currency")
				.setPattern("^([A-Z]{3})$")
				.setMaxLength(3)
				.build());

		assertField(payment,
			new StringField
				.Builder("amount")
				.setMinLength(4)
				.setMaxLength(19)
				.setPattern("^((\\d{1,16}\\.\\d{2}))$")
				.build());

		assertField(payment,
			new ObjectField
				.Builder("details")
				.setValidator(this::assertPaymentDetails)
				.build());


	}

	private void assertPaymentDetails(JsonObject details) {

		Set<String> localInstrumentEnum = Sets.newHashSet("MANU", "DICT", "QRDN", "QRES");

		assertField(details,
			new StringField
				.Builder("localInstrument")
				.setEnums(localInstrumentEnum)
				.setMaxLength(4)
				.build());

		assertField(details,
			new StringField
				.Builder("qrCode")
				.setPattern("[\\w\\W\\s]*")
				.setMaxLength(512)
				.setMinLength(1)
				.setOptional()
				.build());

		assertField(details,
			new StringField
				.Builder("proxy")
				.setPattern("[\\w\\W\\s]*")
				.setMaxLength(77)
				.setMinLength(1)
				.setOptional()
				.build());

		assertField(details,
			new ObjectField
				.Builder("creditorAccount")
				.setValidator(this::assertPayemtCreditor)
				.build());
	}

	private void assertDebtorAccount(JsonObject debtorAccount) {
		Set<String> accountTypes = Sets.newHashSet("CACC", "SLRY", "SVGS", "TRAN");

		assertField(debtorAccount,
			new StringField
				.Builder("ispb")
				.setPattern("^[0-9]{8}$")
				.setMaxLength(8)
				.setMinLength(8)
				.build());

		assertField(debtorAccount,
			new StringField
				.Builder("issuer")
				.setPattern("^\\d{4}$")
				.setMaxLength(4)
				.setOptional()
				.build());

		assertField(debtorAccount,
			new StringField
				.Builder("number")
				.setPattern("^\\d{3,20}$")
				.setMaxLength(20)
				.setMinLength(3)
				.build());

		assertField(debtorAccount,
			new StringField
				.Builder("accountType")
				.setEnums(accountTypes)
				.setMaxLength(4)
				.build());
	}

	private void assertPayemtCreditor(JsonObject creditor) {
		Set<String> accountTypes = Sets.newHashSet("CACC", "SLRY", "SVGS", "TRAN");

		assertField(creditor,
			new StringField
				.Builder("ispb")
				.setPattern("^[0-9]{8}$")
				.setMaxLength(8)
				.setMinLength(8)
				.build());

		assertField(creditor,
			new StringField
				.Builder("issuer")
				.setPattern("^\\d{4}$")
				.setMaxLength(4)
				.setOptional()
				.build());

		assertField(creditor,
			new StringField
				.Builder("accountType")
				.setMaxLength(4)
				.setMinLength(4)
				.setEnums(accountTypes)
				.build());

	}

}
