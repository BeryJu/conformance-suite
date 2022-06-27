package net.openid.conformance.apis.creditOperations.loans.v1;

import net.openid.conformance.apis.AbstractJsonResponseConditionUnitTest;
import net.openid.conformance.condition.ConditionError;
import net.openid.conformance.condition.client.jsonAsserting.ErrorMessagesUtils;
import net.openid.conformance.openbanking_brasil.creditOperations.loans.v1.GetLoansResponseValidator;
import net.openid.conformance.openbanking_brasil.generic.ErrorValidator;
import net.openid.conformance.util.UseResurce;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@UseResurce("jsonResponses/creditOperations/loans/getLoans/getLoansResponseOK.json")
public class GetLoansResponseValidatorTest extends AbstractJsonResponseConditionUnitTest {

	@Test
	public void validateStructure() {
		GetLoansResponseValidator condition = new GetLoansResponseValidator();
		run(condition);
	}

	@Test
	@UseResurce("jsonResponses/creditOperations/loans/getLoans/getLoansResponseWithError.json")
	public void validateStructureWithMissingField() {
		GetLoansResponseValidator condition = new GetLoansResponseValidator();
		ConditionError error = runAndFail(condition);
		assertThat(error.getMessage(), containsString(ErrorMessagesUtils.createElementNotFoundMessage("ipocCode", condition.getApiName())));
	}

	@Test
	@UseResurce("jsonResponses/creditOperations/loans/getLoans/getLoansResponse(WrongEnum).json")
	public void validateStructureWrongEnum() {
		GetLoansResponseValidator condition = new GetLoansResponseValidator();
		ConditionError error = runAndFail(condition);
		assertThat(error.getMessage(), containsString(ErrorMessagesUtils.createFieldValueNotMatchEnumerationMessage("productType", condition.getApiName())));
	}

	@Test
	@UseResurce("jsonResponses/creditOperations/loans/getLoans/getLoansResponse(WrongMaxLength).json")
	public void validateStructureWrongMaxLength() {
		GetLoansResponseValidator condition = new GetLoansResponseValidator();
		ConditionError error = runAndFail(condition);
		assertThat(error.getMessage(), containsString(ErrorMessagesUtils.createFieldValueIsMoreThanMaxLengthMessage("ipocCode", condition.getApiName())));
	}

	@Test
	@UseResurce("jsonResponses/creditOperations/loans/getLoans/getLoansResponse(WrongRegexp).json")
	public void validateStructureWrongRegexp() {
		GetLoansResponseValidator condition = new GetLoansResponseValidator();
		ConditionError error = runAndFail(condition);
		assertThat(error.getMessage(), containsString(ErrorMessagesUtils.createFieldValueNotMatchPatternMessage("companyCnpj", condition.getApiName())));
	}

	@Test
	@UseResurce("jsonResponses/creditOperations/loans/loansErrorResponse.json")
	public void validateErrorResponse() {
		ErrorValidator condition = new ErrorValidator();
		environment.putInteger("resource_endpoint_response_status", 403);
		run(condition);
		environment.removeNativeValue("resource_endpoint_response_status");
	}
}

