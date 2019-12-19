package berlin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.matsim.api.core.v01.population.Person;

import ch.ethz.matsim.discrete_mode_choice.model.DiscreteModeChoiceTrip;
import ch.ethz.matsim.discrete_mode_choice.model.mode_availability.ModeAvailability;

public class BerlinModeAvailability implements ModeAvailability {
	public Collection<String> getAvailableModes(Person person, List<DiscreteModeChoiceTrip> trips) {
		if (person.getId().toString().contains("freight")) {
			return Collections.singleton("freight");
		}

		return Arrays.asList("car", "pt", "bicycle", "walk", "ride", "av");
	}
}
