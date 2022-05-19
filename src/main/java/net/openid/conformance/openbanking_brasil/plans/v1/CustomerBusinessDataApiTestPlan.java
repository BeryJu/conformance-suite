package net.openid.conformance.openbanking_brasil.plans.v1;

import net.openid.conformance.openbanking_brasil.OBBProfile;
import net.openid.conformance.openbanking_brasil.plans.PlanNames;
import net.openid.conformance.openbanking_brasil.testmodules.customerAPI.testmodule.CustomerBusinessDataApiTestModule;
import net.openid.conformance.openbanking_brasil.testmodules.customerAPI.testmodule.CustomerBusinessWrongPermissionsTestModule;
import net.openid.conformance.plan.PublishTestPlan;
import net.openid.conformance.plan.TestPlan;
import net.openid.conformance.variant.FAPI1FinalOPProfile;
import net.openid.conformance.openbanking_brasil.testmodules.PreFlightCertCheckModule;

import java.util.List;

@PublishTestPlan(
	testPlanName = "Business Customer Data api test",
	profile = OBBProfile.OBB_PROFIlE_PHASE2,
	displayName = PlanNames.CUSTOMER_BUSINESS_DATA_API_PLAN_NAME,
	summary = "Structural and logical tests for OpenBanking Brasil-conformant Business Customer Data API"
)
public class CustomerBusinessDataApiTestPlan implements TestPlan {
	public static List<ModuleListEntry> testModulesWithVariants() {
		return List.of(
			new ModuleListEntry(
				List.of(
					PreFlightCertCheckModule.class,
					CustomerBusinessDataApiTestModule.class,
					CustomerBusinessWrongPermissionsTestModule.class
				),
				List.of(
					new Variant(FAPI1FinalOPProfile.class, "openbanking_brazil")
				)
			)
		);
	}
}
