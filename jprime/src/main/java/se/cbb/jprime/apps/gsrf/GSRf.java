package se.cbb.jprime.apps.gsrf;

import java.io.BufferedWriter;

import se.cbb.jprime.io.SampleWriter;
import se.cbb.jprime.math.Continuous1DPDDependent;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.mcmc.DoubleParameter;
import se.cbb.jprime.mcmc.Iteration;
import se.cbb.jprime.mcmc.MCMCManager;
import se.cbb.jprime.mcmc.MultiProposerSelector;
import se.cbb.jprime.mcmc.ProposerSelector;
import se.cbb.jprime.mcmc.Thinner;
import se.cbb.jprime.misc.Pair;
import se.cbb.jprime.misc.Triple;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.GuestHostMap;
import se.cbb.jprime.topology.MPRMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.TimesMap;

import com.beust.jcommander.JCommander;

/**
 * TBD.
 * 
 * @author Joel Sjöstrand.
 */
public class GSRf {
	
	/**
	 * GSRf starter.
	 * @param args.
	 */
	public static void main(String[] args) {
		try {
			// Parse options.
			Parameters params = new Parameters();
			JCommander jc = new JCommander(params, args);
			if (args.length == 0 || params.help) {
				jc.usage();
				return;
			}
			
			Triple<RBTree, NamesMap, TimesMap> sNamesTimes = ParameterParser.getHostTree(params);
			// Multial....
			GuestHostMap gsMap = ParameterParser.getGSMap(params);
			SampleWriter out = ParameterParser.getOut(params);
			BufferedWriter info = ParameterParser.getInfo(params);
			PRNG prng = ParameterParser.getPRNG(params);
			Triple<RBTree, NamesMap, DoubleMap> gNamesLengths = ParameterParser.getGuestTreeAndLengths(params, gsMap, prng, null);
			Iteration iter = ParameterParser.getIteration(params);
			Thinner thinner = ParameterParser.getThinner(params, iter);
			Triple<RBTree, NamesMap, DoubleMap> gNamesLength = ParameterParser.getGuestTreeAndLengths(params, gsMap, prng, null);
			MPRMap mprMap = new MPRMap(gsMap, gNamesLength.first, gNamesLength.second, sNamesTimes.first, sNamesTimes.second);
			Triple<DoubleParameter, DoubleParameter, Continuous1DPDDependent> mvPD = ParameterParser.getEdgeRateDB(params);
			Triple<DoubleParameter, DoubleParameter, DupLossProbs> lambdaMuDupLossProbs = ParameterParser.getDupLossProbs(params, mprMap, sNamesTimes.first, sNamesTimes.third);
			
			MultiProposerSelector selector = new MultiProposerSelector(prng, null);
			
			//MCMCManager man = new MCMCManager(iter, thinner, selector,
		} catch (Exception e) {
			System.err.print(e);
			System.err.print("\nUse option -h or --help to show usage.");
		}
	}
	
	
}
