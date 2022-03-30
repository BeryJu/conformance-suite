package net.openid.conformance.openbanking_brasil.plans;

import net.openid.conformance.openbanking_brasil.OBBProfile;
import net.openid.conformance.openbanking_brasil.testmodules.*;
import net.openid.conformance.plan.PublishTestPlan;
import net.openid.conformance.plan.TestPlan;
import net.openid.conformance.variant.FAPI1FinalOPProfile;
import net.openid.conformance.openbanking_brasil.testmodules.PreFlightCertCheckModule;

import java.util.List;

@PublishTestPlan(
	testPlanName = "Account api test",
	profile = OBBProfile.OBB_PROFIlE_PHASE2,
	displayName = PlanNames.ACCOUNT_API_NAME,
	summary = "Structural and logical tests for OpenBanking Brasil-conformant Account API."
)
public class AccountsApiTestPlan implements TestPlan {
	public static List<ModuleListEntry> testModulesWithVariants() {
		return List.of(
			new ModuleListEntry(
				List.of(
					PreFlightCertCheckModule.class,
					AccountApiTestModule.class,
					AccountsApiWrongPermissionsTestModule.class,
					AccountsApiReadPermissionsAreRestricted.class,
					AccountsApiNegativeTestModule.class,
					AccountsApiUXScreenshots.class,
					AccountsApiPageSizeTestModule.class,
					AccountsApiPageSizeTooLargeTestModule.class,
					AccountsApiMaxPageSizePagingTestModule.class,
					AccountApiBookingDateTest.class
				),
				List.of(
					new Variant(FAPI1FinalOPProfile.class, "openbanking_brazil")
				)
			)
		);
	}
}
