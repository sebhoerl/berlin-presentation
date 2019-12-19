package berlin;

import static org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType.FastAStarLandmarks;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
import org.matsim.core.config.groups.VspExperimentalConfigGroup;
import org.matsim.core.router.TripStructureUtils;

public class Quickfix {
	static public void backportBerlin(Config config, Scenario scenario) {
		// Old MATSIm needs the subpopulation in the PersonAttributes object
		for (Person person : scenario.getPopulation().getPersons().values()) {
			scenario.getPopulation().getPersonAttributes().putAttribute(person.getId().toString(), "subpopulation",
					person.getAttributes().getAttribute("subpopulation"));
		}

		// Old MainModeIdentifier does not understand non_network_walk (it returns it as
		// main mode)
		for (Person person : scenario.getPopulation().getPersons().values()) {
			for (Plan plan : person.getPlans()) {
				for (Leg leg : TripStructureUtils.getLegs(plan)) {
					if (leg.getMode().equals("non_network_walk")) {
						leg.setMode("access_walk");
					}
				}
			}
		}

		config.plansCalcRoute().removeModeRoutingParams("non_network_walk");

		// Remaining stuff copied from MATSim Open Berlin run script to adjust settings

		config.controler().setRoutingAlgorithmType(FastAStarLandmarks);

		config.subtourModeChoice().setProbaForRandomSingleTripMode(0.5);

		config.plansCalcRoute().setRoutingRandomness(3.);
		config.plansCalcRoute().removeModeRoutingParams(TransportMode.ride);
		config.plansCalcRoute().removeModeRoutingParams(TransportMode.pt);
		config.plansCalcRoute().removeModeRoutingParams(TransportMode.bike);
		config.plansCalcRoute().removeModeRoutingParams("undefined");

		config.qsim().setInsertingWaitingVehiclesBeforeDrivingVehicles(true);

		// vsp defaults
		config.vspExperimental().setVspDefaultsCheckingLevel(VspExperimentalConfigGroup.VspDefaultsCheckingLevel.info);
		config.plansCalcRoute().setInsertingAccessEgressWalk(true);
		config.qsim().setUsingTravelTimeCheckInTeleportation(true);
		config.qsim().setTrafficDynamics(TrafficDynamics.kinematicWaves);

		// activities:
		for (long ii = 600; ii <= 97200; ii += 600) {
			ActivityParams params;

			params = new ActivityParams("home_" + ii + ".0");
			params.setTypicalDuration(ii);
			config.planCalcScore().addActivityParams(params);

			params = new ActivityParams("work_" + ii + ".0");
			params.setTypicalDuration(ii);
			params.setOpeningTime(6. * 3600.);
			params.setClosingTime(20. * 3600.);
			config.planCalcScore().addActivityParams(params);

			params = new ActivityParams("leisure_" + ii + ".0");
			params.setTypicalDuration(ii);
			params.setOpeningTime(9. * 3600.);
			params.setClosingTime(27. * 3600.);
			config.planCalcScore().addActivityParams(params);

			params = new ActivityParams("shopping_" + ii + ".0");
			params.setTypicalDuration(ii);
			params.setOpeningTime(8. * 3600.);
			params.setClosingTime(20. * 3600.);
			config.planCalcScore().addActivityParams(params);

			params = new ActivityParams("other_" + ii + ".0");
			params.setTypicalDuration(ii);
			config.planCalcScore().addActivityParams(params);
		}

		ActivityParams params = new ActivityParams("freight");
		params.setTypicalDuration(12. * 3600.);
		config.planCalcScore().addActivityParams(params);
	}

	static public void fixBerlinForDMC(Scenario scenario) {
		// DMC currently has a bug when interpreting maximum duration of activities
		// See https://github.com/matsim-eth/discrete-mode-choice/issues/73

		for (Person person : scenario.getPopulation().getPersons().values()) {
			for (Plan plan : person.getPlans()) {
				double time = 0.0;

				for (PlanElement element : plan.getPlanElements()) {
					if (element instanceof Activity) {
						Activity activity = (Activity) element;

						if (Double.isFinite(activity.getEndTime())) {
							time = activity.getEndTime();
						} else {
							time += activity.getMaximumDuration();
							activity.setEndTime(time);
							activity.setMaximumDuration(Double.NaN);
						}
					} else {
						Leg leg = (Leg) element;
						time += leg.getRoute().getTravelTime();
					}
				}
			}
		}
	}

	/*
	 * static public class OverrideAVRoutingModule extends AbstractModule {
	 * 
	 * @Override public void install() { }
	 * 
	 * @Provides public AVRoutingModule
	 * provideAVRoutingModule(AVOperatorChoiceStrategy choiceStrategy,
	 * AVRouteFactory routeFactory, Map<Id<AVOperator>, AVInteractionFinder>
	 * interactionFinders, Map<Id<AVOperator>, WaitingTime> waitingTimes,
	 * PopulationFactory populationFactory,
	 * 
	 * @Named("walk") RoutingModule walkRoutingModule, AVConfigGroup config,
	 * 
	 * @Named("car") Provider<RoutingModule> roadRoutingModuleProvider,
	 * PriceCalculator priceCalculator) { Map<Id<AVOperator>, Boolean>
	 * predictRouteTravelTime = new HashMap<>(); boolean needsRoutingModule = false;
	 * 
	 * for (OperatorConfig operatorConfig : config.getOperatorConfigs().values()) {
	 * predictRouteTravelTime.put(operatorConfig.getId(),
	 * operatorConfig.getPredictRouteTravelTime()); needsRoutingModule |=
	 * operatorConfig.getPredictRouteTravelTime(); }
	 * 
	 * Provider<RoutingModule> decorator = () -> { RoutingModule delegate =
	 * roadRoutingModuleProvider.get();
	 * 
	 * return new RoutingModule() {
	 * 
	 * @Override public StageActivityTypes getStageActivityTypes() { return
	 * delegate.getStageActivityTypes(); }
	 * 
	 * @Override public List<? extends PlanElement> calcRoute(Facility fromFacility,
	 * Facility toFacility, double departureTime, Person person) { for (PlanElement
	 * element : delegate.calcRoute(fromFacility, toFacility, departureTime,
	 * person)) { if (element instanceof Leg) { Leg leg = (Leg) element;
	 * 
	 * if (leg.getMode().equals("car")) { return Collections.singletonList(leg); } }
	 * }
	 * 
	 * throw new IllegalStateException(); } }; };
	 * 
	 * return new AVRoutingModule(choiceStrategy, routeFactory, interactionFinders,
	 * waitingTimes, populationFactory, walkRoutingModule,
	 * config.getUseAccessEgress(), predictRouteTravelTime, needsRoutingModule ?
	 * decorator.get() : null, priceCalculator); } }
	 */
}
