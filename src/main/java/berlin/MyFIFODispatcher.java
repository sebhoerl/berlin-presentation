package berlin;

import java.util.LinkedList;
import java.util.List;

import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.passenger.AVRequest;

public class MyFIFODispatcher implements AVDispatcher {
	private final List<AVVehicle> vehicles = new LinkedList<>();
	private final List<AVRequest> requests = new LinkedList<>();
	
	@Override
	public void onRequestSubmitted(AVRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNextTaskStarted(AVVehicle vehicle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNextTimestep(double now) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addVehicle(AVVehicle vehicle) {
		// TODO Auto-generated method stub
		
	}

}
