package berlin;

import org.matsim.core.controler.AbstractModule;

import ch.ethz.matsim.av.framework.AVUtils;

public class MyDispatcherModule extends AbstractModule {

	@Override
	public void install() {
		AVUtils.bindDispatcherFactory(binder(), "MyDispatcher").to(implementation);
	}

}
