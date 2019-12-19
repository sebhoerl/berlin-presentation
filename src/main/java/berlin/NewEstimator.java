package berlin;

import java.util.List;

import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;

import ch.ethz.matsim.discrete_mode_choice.model.DiscreteModeChoiceTrip;
import ch.ethz.matsim.discrete_mode_choice.model.trip_based.TripEstimator;
import ch.ethz.matsim.discrete_mode_choice.model.trip_based.candidates.DefaultRoutedTripCandidate;
import ch.ethz.matsim.discrete_mode_choice.model.trip_based.candidates.DefaultTripCandidate;
import ch.ethz.matsim.discrete_mode_choice.model.trip_based.candidates.TripCandidate;

public class NewEstimator implements TripEstimator {

	@Override
	public TripCandidate estimateTrip(Person person, String mode, DiscreteModeChoiceTrip trip,
			List<TripCandidate> previousTrips) {
		
		if (mode.equals("drt")) {
			// routing 
			List<? extends PlanElement> routedPlanElements;
			
			if (routedPlanElements == null) {
				return new InvalidTripTripCandidate();
			}
			
			return new DefaultRoutedTripCandidate(0.0, "drt", routedPlanElements);
		} else {
			return new DefaultTripCandidate(0.0, mode);
		}
	}

}
