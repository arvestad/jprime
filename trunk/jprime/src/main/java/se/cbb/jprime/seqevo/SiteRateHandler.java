package se.cbb.jprime.seqevo;

import java.util.Map;

import se.cbb.jprime.mcmc.ChangeInfo;
import se.cbb.jprime.mcmc.Dependent;
import se.cbb.jprime.mcmc.ProperDependent;

/**
 * 
 * 
 * @author Bengt Sennblad.
 * @author Lars Arvestad.
 * @author Joel Sjöstrand.
 */
public class SiteRateHandler implements ProperDependent {

	public int nCat() {
		return -1;
	}
	
	public double getRate(int cat) {
		return Double.NaN;
	}

	@Override
	public Dependent[] getParentDependents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cacheAndUpdate(Map<Dependent, ChangeInfo> changeInfos,
			boolean willSample) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearCache(boolean willSample) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreCache(boolean willSample) {
		// TODO Auto-generated method stub
		
	}
}
