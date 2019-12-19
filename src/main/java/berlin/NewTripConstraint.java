package berlin;

import java.util.List;

import ch.ethz.matsim.discrete_mode_choice.model.DiscreteModeChoiceTrip;
import ch.ethz.matsim.discrete_mode_choice.model.trip_based.TripConstraint;
import ch.ethz.matsim.discrete_mode_choice.model.trip_based.candidates.TripCandidate;

public class NewTripConstraint implements TripConstraint {

	@Override
	public boolean validateBeforeEstimation(DiscreteModeChoiceTrip trip, String mode, List<String> previousModes) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean validateAfterEstimation(DiscreteModeChoiceTrip trip, TripCandidate candidate,
			List<TripCandidate> previousCandidates) {
		if (candidate.getMode().equals("drt")) {
			return !(candidate instanceof InvalidTripCandidate);
		}
		
		return true;
	}

}
