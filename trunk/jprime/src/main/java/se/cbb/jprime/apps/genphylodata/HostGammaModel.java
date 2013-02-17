//package se.cbb.jprime.apps.genphylodata;
//
//import java.util.Map;
//
//import se.cbb.jprime.io.PrIMENewickTree;
//import se.cbb.jprime.topology.DoubleMap;
//import se.cbb.jprime.topology.GuestHostMap;
//import se.cbb.jprime.topology.NamesMap;
//import se.cbb.jprime.topology.RBTree;
//import se.cbb.jprime.topology.RootedTree;
//import se.cbb.jprime.topology.TimesMap;
//
///**
// * Implements the host-specific rates of Rasmussen-Kellis 2011.
// * Every arc in the host tree is associated with a gamma distribution.
// * The rate of a guest tree arc is derived from the distributions of the
// * host tree arcs it passes over.
// * <p>
// * Back-and-forth transfer scenarios are not accounted for.
// * 
// * @author Joel Sjöstrand.
// */
//public class HostGammaModel implements RateModel {
//
//	private RBTree hostTree;
//	
//	private MPRMap sigma;
//	
//	
//	public HostGammaModel(PrIMENewickTree hostTree, GuestHostMap gs) {
//		
//	}
//	
//	@Override
//	public Map<String, String> getModelParameters() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public String getModelName() {
//		return "HostGammaModel";
//	}
//
//	@Override
//	public DoubleMap getRates(RootedTree t, NamesMap names, DoubleMap origLengths) {
//		// We require a times map.
//		TimesMap times = (TimesMap) origLengths;
//		for (int x = 0; x < t.getNoOfVertices(); ++x) {
//			sigma =
//		}
//		return null;
//	}
//
//}
