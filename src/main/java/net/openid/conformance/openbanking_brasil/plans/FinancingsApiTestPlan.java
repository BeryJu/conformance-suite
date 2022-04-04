package net.openid.conformance.openbanking_brasil.plans;

import net.openid.conformance.openbanking_brasil.OBBProfile;
import net.openid.conformance.openbanking_brasil.testmodules.PreFlightCertCheckModule;
import net.openid.conformance.openbanking_brasil.testmodules.creditOperations.financing.testmodules.FinancingApiWrongPermissionsTestModule;
import net.openid.conformance.openbanking_brasil.testmodules.creditOperations.financing.testmodules.FinancingsApiResourcesTestModule;
import net.openid.conformance.openbanking_brasil.testmodules.creditOperations.financing.testmodules.FinancingsApiTestModule;
import net.openid.conformance.plan.PublishTestPlan;
import net.openid.conformance.plan.TestPlan;
import net.openid.conformance.variant.FAPI1FinalOPProfile;

import java.util.List;

@PublishTestPlan(
	testPlanName = "Financings api test",
	profile = OBBProfile.OBB_PROFIlE_PHASE2,
	displayName = PlanNames.FINANCINGS_API_NAME,
	summary = "Structural and logical tests for OpenBanking Brasil-conformant Financings API"
)
public class FinancingsApiTestPlan implements TestPlan {
	public static List<ModuleListEntry> testModulesWithVariants() {
		return List.of(
			new ModuleListEntry(
				List.of(
					PreFlightCertCheckModule.class,
					FinancingsApiTestModule.class,
					FinancingApiWrongPermissionsTestModule.class,
					FinancingsApiResourcesTestModule.class
				),
				List.of(
					new Variant(FAPI1FinalOPProfile.class, "openbanking_brazil")
				)
			)
		);
	}
}
