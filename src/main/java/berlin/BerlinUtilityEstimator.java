package berlin;

import java.util.List;

import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.router.TripRouter;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.facilities.ActivityFacilities;

import com.google.inject.Inject;

import ch.ethz.matsim.discrete_mode_choice.components.estimators.AbstractTripRouterEstimator;
import ch.ethz.matsim.discrete_mode_choice.model.DiscreteModeChoiceTrip;
import ch.ethz.matsim.discrete_mode_choice.model.trip_based.candidates.TripCandidate;

public class BerlinUtilityEstimator extends AbstractTripRouterEstimator {
	@Inject
	public BerlinUtilityEstimator(TripRouter tripRouter, ActivityFacilities facilities) {
		super(tripRouter, facilities);
	}
	
	private double getTravelTime(List<? extends PlanElement> routedTrip) {
		double travelTime = 0.0;
		
		for (Leg leg : TripStructureUtils.getLegs(routedTrip)) {
			travelTime += leg.getTravelTime();
		}
		
		return travelTime;
	}

	protected double estimateTrip(Person person, String mainMode, DiscreteModeChoiceTrip trip,
			List<TripCandidate> previousTrips, List<? extends PlanElement> routedTrip) {
		double travelTime = getTravelTime(routedTrip);
		
		switch (mainMode) {
		case "car":
			return 1.35 - 0.0667 * travelTime / 60.0;
		case "pt":
			return 0.0 - 0.017 * travelTime / 60.0;
		case "bicycle":
			return 0.1 - 0.15 * travelTime / 60.0;
		case "walk":
			return 1.43 - 0.09 * travelTime / 60.0;
		case "av":
			return 0.0 - 0.017 * travelTime / 60.0;
		}
		
		return 0.0;		
	}
}
