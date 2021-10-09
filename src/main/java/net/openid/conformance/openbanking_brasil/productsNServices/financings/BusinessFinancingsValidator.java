package net.openid.conformance.openbanking_brasil.productsNServices.financings;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.condition.client.AbstractJsonAssertingCondition;
import net.openid.conformance.logging.ApiName;
import net.openid.conformance.openbanking_brasil.CommonFields;
import net.openid.conformance.openbanking_brasil.productsNServices.CommonValidatorParts;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.util.field.ObjectArrayField;
import net.openid.conformance.util.field.ObjectField;
import net.openid.conformance.util.field.StringArrayField;
import net.openid.conformance.util.field.StringField;

import java.util.Set;

/**
 * Api url: https://github.com/OpenBanking-Brasil/areadesenvolvedor/blob/91e2ff8327cb35eb1ae571c7b2264e6173b34eeb/swagger/swagger_products_services_apis.yaml
 * Api endpoint: /business-financings
 * Api version: 1.0.2
 * Api git hash: ba747ce30bdf7208a246ebf1e8a2313f85263d91
 *
 */
@ApiName("ProductsNServices Business Financings")
public class BusinessFinancingsValidator extends AbstractJsonAssertingCondition {

	public static final Set<String> TYPES = Sets.newHashSet("FINANCIAMENTO_AQUISICAO_BENS_VEICULOS_AUTOMOTORES",
		"FINANCIAMENTO_AQUISICAO_BENS_OUTROS_BENS", "FINANCIAMENTO_MICROCREDITO",
		"FINANCIAMENTO_RURAL_CUSTEIO", "FINANCIAMENTO_RURAL_INVESTIMENTO", "FINANCIAMENTO_RURAL_COMERCIALIZACAO",
		"FINANCIAMENTO_RURAL_INDUSTRIALIZACAO", "FINANCIAMENTO_IMOBILIARIO_SISTEMA_FINANCEIRO_HABITACAO_SFH",
		"FINANCIAMENTO_IMOBILIARIO_SISTEMA_FINANCEIRO_HABITACAO_SFI");
	public static final Set<String> REQUIRED_WARRANTIES = Sets.newHashSet("CESSAO_DIREITOS_CREDITORIOS", "CAUCAO",
		"PENHOR", "ALIENACAO_FIDUCIARIA", "HIPOTECA", "OPERACOES_GARANTIDAS_PELO_GOVERNO",
		"OUTRAS_GARANTIAS_NAO_FIDEJUSSORIAS", "SEGUROS_ASSEMELHADOS", "GARANTIA_FIDEJUSSORIA",
		"BENS_ARRENDADOS", "GARANTIAS_INTERNACIONAIS", "OPERACOES_GARANTIDAS_OUTRAS_ENTIDADES",
		"ACORDOS_COMPENSACAO", "NAO_APLICAVEL");

	private static class Fields extends CommonFields {}
	private final CommonValidatorParts parts;

	public BusinessFinancingsValidator() {
		parts = new CommonValidatorParts(this);
	}

	@Override
	@PreEnvironment(strings = "resource_endpoint_response")
	public Environment evaluate(Environment environment) {
		setLogOnlyFailure();
		JsonObject body = bodyFrom(environment);
		assertHasField(body, ROOT_PATH);
		assertJsonObject(body, ROOT_PATH,
			(data) -> assertField(data,
				new ObjectField.Builder("brand").setValidator(
					(brand) -> {
						assertField(brand, Fields.name().build());
						assertField(brand,
							new ObjectArrayField.Builder("companies")
								.setMinItems(1)
								.setValidator(this::assertCompanies)
								.build());
					}
				).build())
		);
		logFinalStatus();
		return environment;
	}

	private void assertCompanies(JsonObject companies) {
		assertField(companies, Fields.cnpjNumber().build());
		assertField(companies,Fields.name().build());
		assertField(companies, Fields.urlComplementaryList().build());

		assertField(companies,
			new ObjectArrayField
				.Builder("businessFinancings")
				.setValidator(this::assertBusinessFinancings)
				.setMinItems(1)
				.setMaxItems(9)
				.build());
	}

	private void assertBusinessFinancings(JsonObject businessFinancings) {

		assertField(businessFinancings, CommonFields.type(TYPES).build());

		assertField(businessFinancings,
			new ObjectField.Builder("fees").setValidator(
				fees -> assertField(fees,
					new ObjectArrayField.Builder("services")
						.setMinItems(1).setMaxItems(9).setValidator(
							services -> {
								assertField(services, Fields.name().setMaxLength(250).build());
								assertField(services, Fields.code().build());
								assertField(services, Fields.chargingTriggerInfo().build());
								parts.assertPrices(services);
								parts.applyAssertingForCommonMinimumAndMaximum(services);
							}).build())
			).build());

		parts.applyAssertingForCommonRates(businessFinancings,"interestRates", false);

		assertField(businessFinancings,
			new StringArrayField
				.Builder("requiredWarranties")
				.setMinItems(1)
				.setMaxItems(14)
				.setEnums(REQUIRED_WARRANTIES)
				.build());

		assertField(businessFinancings,
			new StringField
				.Builder("termsConditions")
				.setMaxLength(2000)
				.setPattern("[\\w\\W\\s]*")
				.build());
	}
}
