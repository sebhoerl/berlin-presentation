package berlin;

import java.util.Collection;
import java.util.List;

import org.matsim.api.core.v01.population.Person;

import ch.ethz.matsim.discrete_mode_choice.model.DiscreteModeChoiceTrip;
import ch.ethz.matsim.discrete_mode_choice.model.trip_based.TripConstraint;
import ch.ethz.matsim.discrete_mode_choice.model.trip_based.TripConstraintFactory;
import ch.ethz.matsim.discrete_mode_choice.model.trip_based.candidates.TripCandidate;

public class KeepRideConstraint implements TripConstraint {
	public boolean validateBeforeEstimation(DiscreteModeChoiceTrip trip, String mode, List<String> previousModes) {
		String initialMode = trip.getInitialMode();
		String proposedMode = mode;

		if (initialMode.equals("ride") && !proposedMode.equals("ride")) {
			return false;
		}

		if (proposedMode.equals("ride") && !initialMode.equals("ride")) {
			return false;
		}

		return true;
	}

	public boolean validateAfterEstimation(DiscreteModeChoiceTrip trip, TripCandidate candidate,
			List<TripCandidate> previousCandidates) {
		return true;
	}

	static public class Factory implements TripConstraintFactory {
		public TripConstraint createConstraint(Person person, List<DiscreteModeChoiceTrip> planTrips,
				Collection<String> availableModes) {
			return new KeepRideConstraint();
		}
	}

}
