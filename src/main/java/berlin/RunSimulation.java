package berlin;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.Controler;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultSelector;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.AVScoringParameterSet;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.dispatcher.single_heuristic.SingleHeuristicDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVQSimModule;
import ch.ethz.matsim.av.generator.PopulationDensityGenerator;
import ch.ethz.matsim.discrete_mode_choice.modules.ConstraintModule;
import ch.ethz.matsim.discrete_mode_choice.modules.DiscreteModeChoiceConfigurator;
import ch.ethz.matsim.discrete_mode_choice.modules.DiscreteModeChoiceModule;
import ch.ethz.matsim.discrete_mode_choice.modules.EstimatorModule;
import ch.ethz.matsim.discrete_mode_choice.modules.ModelModule.ModelType;
import ch.ethz.matsim.discrete_mode_choice.modules.SelectorModule;
import ch.ethz.matsim.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;

public class RunSimulation {
	static public void main(String[] args) throws ConfigurationException {
		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("config-path") //
				.build();

		Config config = ConfigUtils.loadConfig(cmd.getOptionStrict("config-path"));
		config.transit().setUsingTransitInMobsim(false);
		config.controler().setWriteEventsInterval(10);
		config.controler().setLastIteration(10);

		Scenario scenario = ScenarioUtils.loadScenario(config);
		Quickfix.backportBerlin(config, scenario);
		Quickfix.fixBerlinForDMC(scenario);

		/*DiscreteModeChoiceConfigurator.configureAsSubtourModeChoiceReplacement(config);

		DiscreteModeChoiceConfigGroup dmcConfig = DiscreteModeChoiceConfigGroup.getOrCreate(config);
		dmcConfig.setTourFilters(Arrays.asList("TourLengthFilter"));
		dmcConfig.setModeAvailability("BerlinModeAvailability");

		List<String> tourConstraints = new LinkedList<>(dmcConfig.getTourConstraints());
		tourConstraints.add(ConstraintModule.FROM_TRIP_BASED);
		dmcConfig.setTourConstraints(tourConstraints);*/
		
		{ // Configure AV
			config.planCalcScore().addModeParams(new ModeParams("av"));

			DvrpConfigGroup dvrpConfig = new DvrpConfigGroup();
			config.addModule(dvrpConfig);

			AVConfigGroup avConfig = new AVConfigGroup();
			config.addModule(avConfig);

			avConfig.setAllowedLinkMode("car");

			OperatorConfig operatorConfig = new OperatorConfig();
			avConfig.addOperator(operatorConfig);

			operatorConfig.getDispatcherConfig().setType(SingleHeuristicDispatcher.TYPE);
			operatorConfig.getGeneratorConfig().setType(PopulationDensityGenerator.TYPE);
			operatorConfig.getGeneratorConfig().setNumberOfVehicles(1000);

			List<String> modes = new LinkedList<>(Arrays.asList(config.subtourModeChoice().getModes())); //
			modes.add("av");
			config.subtourModeChoice().setModes(modes.toArray(new String[modes.size()]));

			AVScoringParameterSet params;

			params = new AVScoringParameterSet();
			params.setMarginalUtilityOfWaitingTime(-0.1);
			params.setStuckUtility(-50.0);
			params.setSubpopulation("person");
			avConfig.addScoringParameters(params);

			params = new AVScoringParameterSet();
			params.setMarginalUtilityOfWaitingTime(-0.1);
			params.setStuckUtility(-50.0);
			params.setSubpopulation("freight");
			avConfig.addScoringParameters(params);

			operatorConfig.getDispatcherConfig().setType("MyAmodeus");
			operatorConfig.setPredictRouteTravelTime(true);

			operatorConfig.getParams().put("virtualNetworkPath", "berlin_virtual_network/berlin_virtual_network");
			operatorConfig.getParams().put("travelDataPath", "berlin_travel_data");
		}
		
		DiscreteModeChoiceConfigurator.configureAsModeChoiceInTheLoop(config);
		
		if (true) { // Configure DMC
			DiscreteModeChoiceConfigurator.configureAsModeChoiceInTheLoop(config);
			DiscreteModeChoiceConfigGroup dmcConfig = DiscreteModeChoiceConfigGroup.getOrCreate(config);

			dmcConfig.setTourFinder(BerlinTourFinder.NAME);

			dmcConfig.setModeAvailability("BerlinModeAvailability");
			dmcConfig.setTourFilters(Arrays.asList("TourLengthFilter"));

			dmcConfig.setModelType(ModelType.Tour);
			dmcConfig.setTripEstimator("BerlinEstimator");
			dmcConfig.setTourEstimator(EstimatorModule.CUMULATIVE);
			dmcConfig.setTourConstraints(
					Arrays.asList(ConstraintModule.VEHICLE_CONTINUITY, ConstraintModule.FROM_TRIP_BASED));
			dmcConfig.setTripConstraints(
					Arrays.asList(ConstraintModule.TRANSIT_WALK, "KeepRide"));
			dmcConfig.setSelector(SelectorModule.MULTINOMIAL_LOGIT);

			dmcConfig.setCachedModes(Arrays.asList("car", "pt", "bicycle", "walk", "freight", "av"));

			StrategyConfigGroup strategyConfigGroup = config.strategy();
			double replanningRate = 0.2;

			for (String subpopulation : Arrays.asList("person", "freight")) {
				StrategySettings dmcStrategy = new StrategySettings();
				dmcStrategy.setStrategyName(DiscreteModeChoiceModule.STRATEGY_NAME);
				dmcStrategy.setWeight(replanningRate);
				dmcStrategy.setSubpopulation(subpopulation);
				strategyConfigGroup.addStrategySettings(dmcStrategy);

				StrategySettings selectorStrategy = new StrategySettings();
				selectorStrategy.setStrategyName(DefaultSelector.KeepLastSelected);
				selectorStrategy.setWeight(1.0 - replanningRate);
				selectorStrategy.setSubpopulation(subpopulation);
				strategyConfigGroup.addStrategySettings(selectorStrategy);
			}

			//dmcConfig.getShapeFileConstraintConfigGroup().setConstrainedModes(Arrays.asList("av"));
			//dmcConfig.getShapeFileConstraintConfigGroup().setRequirement(Requirement.BOTH);
			//dmcConfig.getShapeFileConstraintConfigGroup().setPath("service_area.shp");
		}
		
		
		Controler controller = new Controler(scenario);
		controller.addOverridingModule(new SwissRailRaptorModule());
		controller.addOverridingModule(new DiscreteModeChoiceModule());

		controller.addOverridingModule(new BerlinModeChoiceModule());
		controller.addOverridingModule(new AVModule());
		controller.addOverridingModule(new DvrpModule()); // AV
		controller.addOverridingModule(new DvrpTravelTimeModule()); // AV
		
		controller.configureQSimComponents(AVQSimModule::configureComponents);

		controller.run();
	}
}
