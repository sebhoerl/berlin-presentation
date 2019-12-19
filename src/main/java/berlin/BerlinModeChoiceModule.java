package berlin;

import ch.ethz.matsim.discrete_mode_choice.modules.AbstractDiscreteModeChoiceExtension;

public class BerlinModeChoiceModule extends AbstractDiscreteModeChoiceExtension {
	@Override
	protected void installExtension() {
		bindTourFilter("TourLengthFilter").to(TourLengthFilter.class);
		bindModeAvailability("BerlinModeAvailability").to(BerlinModeAvailability.class);
		bindTripConstraintFactory("KeepRide").to(KeepRideConstraint.Factory.class);
		bindTripEstimator("BerlinEstimator").to(BerlinUtilityEstimator.class);
		bindTourFinder("BerlinTourFinder").to(BerlinTourFinder.class);
	}
}
