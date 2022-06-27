package net.openid.conformance.apis.account.v1;

import net.openid.conformance.apis.AbstractJsonResponseConditionUnitTest;
import net.openid.conformance.condition.ConditionError;
import net.openid.conformance.condition.client.jsonAsserting.ErrorMessagesUtils;
import net.openid.conformance.openbanking_brasil.account.v1.AccountBalancesResponseValidator;
import net.openid.conformance.util.UseResurce;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@UseResurce("jsonResponses/account/balances/accountBalancesResponse.json")
public class AccountBalancesResponseValidatorTest extends AbstractJsonResponseConditionUnitTest {

	@Test
	public void validateStructure() {
		AccountBalancesResponseValidator condition = new AccountBalancesResponseValidator();
		run(condition);
	}

	@Test
	@UseResurce("jsonResponses/account/balances/accountBalancesResponse_missing_consents.json")
	public void validateStructureWithMissingField() {
		AccountBalancesResponseValidator condition = new AccountBalancesResponseValidator();
		ConditionError error = runAndFail(condition);
		String expected = ErrorMessagesUtils.createElementNotFoundMessage("blockedAmount", condition.getApiName());
		assertThat(error.getMessage(), containsString(expected));
	}

	@Test
	@UseResurce("jsonResponses/account/balances/errors/accountBalancesResponseWrongPattern.json")
	public void validateStructureWithWrongRegexp() {
		AccountBalancesResponseValidator condition = new AccountBalancesResponseValidator();
		ConditionError error = runAndFail(condition);
		String expected = ErrorMessagesUtils.createFieldValueNotMatchPatternMessage("blockedAmountCurrency", condition.getApiName());
		assertThat(error.getMessage(), containsString(expected));
	}
}
