package se.cbb.jprime.apps.genphylodata;

import java.util.List;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.mcmc.GenerativeModel;
import se.cbb.jprime.mcmc.StateParameter;

/**
 * Relaxes ultrametric branch lengths for a tree by imposing iid rates or similarly.
 * 
 * @author Joel Sjöstrand.
 */
public class ClockRelaxer implements JPrIMEApp, GenerativeModel {

	@Override
	public String getAppName() {
		return "ClockRelaxer";
	}

	@Override
	public void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String getModelName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<StateParameter> getModelParameters() {
		// TODO Auto-generated method stub
		return null;
	}

}
