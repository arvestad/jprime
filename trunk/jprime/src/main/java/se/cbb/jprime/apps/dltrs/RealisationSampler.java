package se.cbb.jprime.apps.dltrs;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import se.cbb.jprime.io.SampleInt;
import se.cbb.jprime.io.Sampleable;
import se.cbb.jprime.math.Continuous1DPDDependent;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.mcmc.Iteration;
import se.cbb.jprime.topology.BooleanMap;
import se.cbb.jprime.topology.DoubleArrayMap;
import se.cbb.jprime.topology.DoubleMap;
import se.cbb.jprime.topology.GenericMap;
import se.cbb.jprime.topology.IntMap;
import se.cbb.jprime.topology.MPRMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTreeArcDiscretiser;
import se.cbb.jprime.topology.RBTreeEpochDiscretiser;
import se.cbb.jprime.topology.RootedBifurcatingTree;
import se.cbb.jprime.topology.StringMap;
import se.cbb.jprime.topology.TimesMap;

/**
 * 
 * Enables sampling of <i>realisations</i>, i.e., dated embeddings
 * of G in S according to the probability distribution of embeddings under the DLRS model.
 * @author Mehmood Alam Khan.
 * 
 */

public class RealisationSampler implements Sampleable {

	/** Output stream. */
	private BufferedWriter out;

	/** Iteration. */
	private Iteration iteration;

	/** PRNG. */
	private PRNG prng;

	/** Host tree. */
	private RootedBifurcatingTree S;

	/** Guest tree. */
	private RootedBifurcatingTree G;

	/** Guest tree names. */
	private NamesMap names;

	/** The divergence times t for the discretised tree S'. */
	protected RBTreeEpochDiscretiser times;  
	
	/** The divergence times t for the discretised tree S'. */
	protected RBTreeEpochDiscretiser msTimes;  

	/** Upper limits for placement of vertices v of G in S'. */
	protected IntMap upLims;

	/** P11, etc. */
	private EpochDLTProbs dltProbs;  

	/** The branch lengths l. */
	protected DoubleMap lengths;

	/** Substitution rate distribution. */
	private Continuous1DPDDependent substPD;

	///** At-probabilities for vertices v of G. */
	/** Probability of rooted subtree G_u for each valid placement of u in S'. */
	protected GenericMap<EpochPtMap> ats;

	/** Probability of planted subtree G^u for each valid placement of tip of u's parent arc in S'. */
	protected GenericMap<EpochPtMap> belows;
	
	/** P11, etc. */
	private EpochDLTProbs msDltProbs;  

	/** The branch lengths l. */
	protected DoubleMap msLengths;

	/** Substitution rate distribution. */
	private Continuous1DPDDependent msSubstPD;

	///** At-probabilities for vertices v of G. */
	/** Probability of rooted subtree G_u for each valid placement of u in S'. */
	protected GenericMap<EpochPtMap> msAts;

	/** Probability of planted subtree G^u for each valid placement of tip of u's parent arc in S'. */
	protected GenericMap<EpochPtMap> msBelows;

	/** No. of realisations per sampling round. */
	private int noOfRealisations;

	/** Reconciliations helper. */  
	protected ReconciliationHelper reconcHelper;

	/** Reconciliations helper. */  
	protected ReconciliationHelper msReconcHelper;

	/**
	 * Constructor.
	 * @param file f the output str.
	 * @param iteration iteration.
	 * @param prng pseudo-random number generator.
	 * @param S host tree S.
	 * @param G guest tree G.
	 * @param names leaf names of G.
	 * @param times times of discretised host tree S'.
	 * @param loLims lowest possible placement of u of V(G) in discretised S'.
	 * @param upLims Upper possible placement of u of V(G) in discretised S'.
	 * @param ePochDLTProbs p11, etc.
	 * @param ats rooted subtree G_u probability for u of V(G).
	 * @param noOfRealisations number of realisations per sampling round.
	 * @throws IOException.
	 */
	public RealisationSampler(String filename, int noOfRealisations, Iteration iteration, PRNG prng, DLTRModel model, DLTRModelMaxSampling msModel, NamesMap names) throws IOException {
		this.out = new BufferedWriter(new FileWriter(filename));
		this.noOfRealisations = noOfRealisations;
		this.iteration = iteration;
		this.prng = prng;
		this.S = model.s;
		this.G = model.g;
		this.names = names;
		this.times = model.reconcHelper.times;
		this.lengths = model.lengths;
		this.reconcHelper= model.reconcHelper;  
		this.dltProbs = model.dltProbs;  
		this.substPD = model.substPD;
		this.ats = model.ats;  
		this.belows= model.belows; 
		this.msTimes = msModel.reconcHelper.times;
		this.msLengths = msModel.lengths;
		this.msReconcHelper= msModel.reconcHelper;  
		this.msDltProbs = msModel.dltProbs;  
		this.msSubstPD = msModel.substPD;
		this.msAts = msModel.ats;  
		this.msBelows= msModel.belows;

		
		// Write header.
		this.out.write("# Host tree: " + this.times.toString() + "\n");
		if (this.noOfRealisations > 0) {
			this.out.write("RealisationID\tSubsample\tRealisation\n");
		}
	}


	/**
	 * Retrieves the maximum probability realisation given the current guest tree, "at-probabilities", p11-probabilities, etc.
	 * @param vertices vertices of G in topological ordering from root to leaves.
	 */
	public Realisation getMaximumProbabilityRealisation(List<Integer> vertices) {
		int n = vertices.size();
		int[][] placements = new int[n][];  // Sampled points.
		double[] abst = new double[n];      // Absolute times.
		double[] arct = new double[n];      // Arc times.
		boolean[] isDups = new boolean[n];  // Type of point
		boolean[] isTrans = new boolean[n];  // Type of point..  

		// For each vertex v of G.
		String[] placementss = new String[n];
		for (int v : vertices) {
			getMaxPointLTG(v, placements, abst, arct, isDups, isTrans);
			System.out.println(this.G.toString());
			System.out.println(this.S.toString());
			placementss[v] = "(" + placements[v][0] + "," + placements[v][1] + ")"; 

		}

		// Finally, generate guest tree with times.
		return new Realisation(this.G, this.names, new TimesMap("RealisationTimes", abst, arct), new BooleanMap("RealisationIsDups", isDups), new BooleanMap("RealisationIsTrans", isTrans), new StringMap("DiscPts",placementss));

	}



	/**
	 * Samples a realisation given the current guest tree, "at-probabilities", p11-probabilities, etc.
	 * @param vertices vertices of G in topological ordering from root to leaves.
	 */
	public Realisation sample(List<Integer> vertices) {

		int n = vertices.size();
		int[][] placements = new int[n][];  // Sampled points.
		double[] abst = new double[n];      // Absolute times.
		double[] arct = new double[n];      // Arc times.
		boolean[] isDups = new boolean[n];  // Type of point.
		boolean[] isTrans = new boolean[n];  // Type of point..  // changed

		// For each vertex v of G.
		String[] placementss = new String[n];
		for (int v : vertices) {
			getSamplePointLTG(v, placements, abst, arct, isDups, isTrans);
			placementss[v] = "(" + placements[v][0] + "," + placements[v][1] + ")"; 
		}

		// Finally, generate guest tree with times.
		return new Realisation(this.G, this.names, new TimesMap("RealisationTimes", abst, arct), new BooleanMap("RealisationIsDups", isDups), new BooleanMap("RealisationIsTrans", isTrans), new StringMap("DiscPts",placementss));
	}

	/////////////////////////////////////////////////////////New Get Max Point Functino///////////////////////////////////////////////

	/**
	 * Gets the maximum probability point y in S' for placement of vertex v of G, given that the parent of v has been sampled already.
	 * @param v vertex of G.
	 * @param placements placements in S'
	 * @param absTimes absolute times of sampled tree.
	 * @param arcTimes arc times of sampled tree.
	 * @param isDups type of point. True if its duplication.
	 * @param isTrans type of point. True if its transfer.
	 */
	private void getMaxPointLTG(int v, int[][] placements, double[] absTimes, double[] arcTimes, boolean[] isDups, boolean[] isTrans) {

		// Get placement of parent of v in S'.
		int[] s;
		if (this.G.isRoot(v)) {
			s = this.msTimes.getEpochPtAtTop();
		} else {
			s = placements[this.G.getParent(v)];
		}

		double sTime = msReconcHelper.getTime(s);
		double l = msLengths.get(v);
		double[] lins = this.msBelows.get(v).get(s[0], s[1]);
		int sz = lins.length;
		int[] t = this.msReconcHelper.getLoLim(v);

		if (this.G.isLeaf(v)) { // if v is a leaf node of G
			int sigma = this.msReconcHelper.getHostLeafIndex(v);
			double rateDens = msSubstPD.getPDF(l / sTime);  // Assumes leaf time 0.

			// For each edge e where lineage can start at time s.
			for (int e = 0; e < sz; ++e) {
				lins[e] = this.msDltProbs.getOneToOneProbs().get(0, 0, sigma, s[0], s[1], e) * rateDens;
			}
			t = new int[] {0, 0};
			placements[v] = t;
			absTimes[v]= this.msReconcHelper.getTime(t);
			arcTimes[v]= this.msReconcHelper.getTime(s)-absTimes[v];

			System.out.println("V: "+v+ "\t is Leaf");

		}else{ // if v is not a leaf of G

			// Stores all valid placement y's.
			ArrayList<int[]> ys = new ArrayList<int[]>(); // added after May 23: need to check if this.ats.getSize() == delirious.ats.length

			// Cumulative probabilities for the y's.
			ArrayList<Double> cps = new ArrayList<Double>();// added after May 23

			// Store Arc 'f' of duplication or transfer on species tree 
			ArrayList<Integer> arcF = new ArrayList<Integer>();// added after May 23


			// Reset values.
			for (int i = 0; i < sz; ++i) {
				lins[i] = 0.0;
			}

			// We always ignore last time index for at-probs of current epoch,
			// since such values are correctly stored at index 0 of next epoch.
			//int[] t = this.reconcHelper.getLoLim(v);  // changed after May 23
			if (msReconcHelper.isLastEpochTime(t)) {
				t = new int[] {t[0]+1, 0};
			}

			double maxp 	= 0.0;
			int[] maxT	= null;
			int maxArc		= -1;
			int maxE		= -1;
			// For each valid time t where u can be placed (strictly beneath s).
			while (t[0] < s[0] || (!(s[0] < t[0]) && t[1] < s[1])) {
				double rateDens = msSubstPD.getPDF(l / (sTime - msReconcHelper.getTime(t)));

				// For each edge e where lineage can start at time s.
				double[] ats = this.msAts.get(v).get(t[0], t[1]);
				for (int e = 0; e < sz; ++e) {
					// For each edge f where u can be placed at time t.
					for (int f = 0; f < ats.length; ++f) {
						double p= msDltProbs.getOneToOneProbs().get(t[0], t[1], f, s[0], s[1], e) * rateDens * ats[f];
						if  (p > maxp) {
							maxp = p;
							maxT = t;
							maxArc= f;
							maxE= e;				
						}
						System.out.println(v+"\tEdgeInUpperEdgeGeneration["+e+"]\t EdgeInLowerEdgeGeneration["+f+ "]\ts["+s[0]+", "+ s[1]+ "] \tt["+t[0] +","+ t[1]+"] \tProb ["+ p+"]" );
					}
				}
				t = msReconcHelper.getEpochTimeAboveNotLast(t);
			}

			t=maxT;

			// Finally, store the properties.
			placements[v] = t;
			absTimes[v]= this.msReconcHelper.getTime(t);
			arcTimes[v]= this.msReconcHelper.getTime(s)-absTimes[v];

			// from where the transfer has happend
			System.out.print("\nspecieLineageE "+maxE+"\tt\t["+ t[0] + ", "+ t[1]+ "]\t ArcF ["+maxArc+"] maxProb: "+maxp+"\t");

			// check if the event is duplication or transfer
			if (t[1] != 0){
				int lc = G.getLeftChild(v);
				int rc = G.getRightChild(v);
				double dt = msReconcHelper.getTimestep(t[0]);	
				double[] ats = this.msAts.get(v).get(t[0], t[1]);
				double dupFact = 2 * msDltProbs.getDuplicationRate();
				int adjFact = (this.msDltProbs.getTransferProbabilityAdjustment() ? ats.length - 1 : 1);   // Adjust for contemporary species or not.
				double trFact = this.msDltProbs.getTransferRate() / adjFact;

				double[] lclins = msBelows.get(lc).get(t[0], t[1]);
				double[] rclins = msBelows.get(rc).get(t[0], t[1]);

				double dupProb	=	0.0;
				double[] transProb= new double[ats.length];
				double[] transProbVtoW= new double[ats.length];
				double[] transProbWtoV= new double[ats.length];

				double transProbSum= 0.0;
				double maxTransProb= 0.0;
				double maxLinTransProb= 0.0;

				if (ats.length > 1) {
					
					double lcMax = 0.0;
					int lcEdgeIndex=-1;
					for (int i=0; i< lclins.length; i++) {
						if (lcMax < lclins[i]){
							lcMax = lclins[i];
							lcEdgeIndex=i;
						}
					}
					double rcMax = 0.0;
					int rcEdgeIndex=-1;
					for (int i=0; i< rclins.length; i++) {
						if (lcMax < rclins[i]){
							lcMax = rclins[i];
							rcEdgeIndex=i;
						}
					}
					dupProb 	= dt * (dupFact * lclins[maxArc] * rclins[maxArc]);
					// here f refers to different arcs/lineages of species tree in LowerEdgeGeneration
					for (int f = 0; f < ats.length; ++f) {
						if (f != maxArc){
							transProbVtoW[f] += dt * (trFact * (lclins[f] * rcMax ));
							transProbWtoV[f] += dt * (trFact * (rclins[f] * lcMax ));
							transProb[f] += transProbWtoV[f]  + transProbVtoW[f];
							if (maxLinTransProb < transProb[f]){
								maxLinTransProb=transProb[f];
							}
							if( maxTransProb < transProb[f]){
								maxTransProb = transProb[f];
							}						
							transProbSum += transProb[f];
						}
					}

					if (dupProb > maxTransProb ){
						System.out.println("Duplication");
						isDups[v]	=	true;
					}else{
						System.out.println("Transfer Happens at gene vertix u: "+ v);
						isTrans[v]		=true;
						double maxVtoW	= 0.0;
						double maxWtoV	= 0.0;
						int maxEforVtoW			= -1;
						int maxEforWtoV			= -1;
						

						// child that receive the transfered lineage will be
						for (int e = 0; e < ats.length; ++e) {
							if (e != maxArc){

								if (maxVtoW < (transProbVtoW[e]/transProbSum)){  
									maxVtoW = (transProbVtoW[e]/transProbSum);
									maxEforVtoW = e;
								}						
								if (maxWtoV < (transProbWtoV[e]/transProbSum)){  
									maxWtoV =  (transProbWtoV[e]/transProbSum);
									maxEforWtoV = e;
								}
							}
						}
						if (maxVtoW > maxWtoV){
							// select the child where V stays but W get transfered to specie lineage e, also Normalizing each component
							System.out.println("Child 'V': "+ lc + " Stays but Child 'W': "+ rc +" got Transfered to specie Arc:" + maxEforVtoW);
						}else{
							// select the child where W stays but V get transfered to specie lineage e,  also Normalizing each component
							System.out.println("Child 'W': "+ rc + " Stays but Child 'V': "+ lc +" got Transfered to specie Arc:" + maxEforWtoV);
						}
					}

				} else {
					// Case with top time edge. No transfer possible.
					ats[0] = dt * dupFact * lclins[0] * rclins[0];
					System.out.println("Duplication");
					isDups[v]=true;
				}

			}else{
				System.out.println("V: "+v+ "\t Speciation");
			}


		} // else ends here


	}

	/////////////////////////////////////////////////////////New Get Sample Point Functino///////////////////////////////////////////////

	/**
	 * Samples a point y in S' for placement of vertex v of G, given that the parent of v has been sampled already.
	 * @param v vertex of G.
	 * @param placements placements in S'
	 * @param absTimes absolute times of sampled tree.
	 * @param arcTimes arc times of sampled tree.
	 * @param isDups type of point. True if its duplication.
	 * @param isTrans type of point. True if its transfer.
	 */

	private void getSamplePointLTG(int v, int[][] placements, double[] absTimes, double[] arcTimes, boolean[] isDups, boolean[] isTrans) {

		// Get placement of parent of v in S'.
		int[] s;
		if (this.G.isRoot(v)) {
			s = this.times.getEpochPtAtTop();
		} else {
			s = placements[this.G.getParent(v)];
		}

		double sTime = reconcHelper.getTime(s);
		double l = lengths.get(v);
		double[] lins = this.belows.get(v).get(s[0], s[1]);
		int sz = lins.length;
		int[] t = this.reconcHelper.getLoLim(v);

		if (this.G.isLeaf(v)) { // if v is a leaf node of G
			int sigma = this.reconcHelper.getHostLeafIndex(v);
			double rateDens = substPD.getPDF(l / sTime);  // Assumes leaf time 0.

			// For each edge e where lineage can start at time s.
			for (int e = 0; e < sz; ++e) {
				lins[e] = this.dltProbs.getOneToOneProbs().get(0, 0, sigma, s[0], s[1], e) * rateDens;
			}
			t = new int[] {0, 0};
			placements[v] = t;
			absTimes[v]= this.reconcHelper.getTime(t);
			arcTimes[v]= this.reconcHelper.getTime(s)-absTimes[v];

			System.out.println("V: "+v+ "\t is Leaf");

		}else{ // if v is not a leaf of G

			// Stores all valid placement y's.
			ArrayList<int[]> ys = new ArrayList<int[]>(); // added after May 23: need to check if this.ats.getSize() == delirious.ats.length

			// Cumulative probabilities for the y's.
			ArrayList<Double> cps = new ArrayList<Double>();// added after May 23

			// Store Arc 'f' of duplication or transfer on species tree 
			ArrayList<Integer> arcF = new ArrayList<Integer>();// added after May 23


			// Reset values.
			for (int i = 0; i < sz; ++i) {
				lins[i] = 0.0;
			}

			// We always ignore last time index for at-probs of current epoch,
			// since such values are correctly stored at index 0 of next epoch.
			//int[] t = this.reconcHelper.getLoLim(v);  // changed after May 23
			if (reconcHelper.isLastEpochTime(t)) {
				t = new int[] {t[0]+1, 0};
			}
			double tempCps=0.0;

			// For each valid time t where u can be placed (strictly beneath s).
			while (t[0] < s[0] || (!(s[0] < t[0]) && t[1] < s[1])) {
				double rateDens = substPD.getPDF(l / (sTime - reconcHelper.getTime(t)));

				// For each edge e where lineage can start at time s.
				double[] ats = this.ats.get(v).get(t[0], t[1]);
				for (int e = 0; e < sz; ++e) {
					lins[e]=tempCps;
					// For each edge f where u can be placed at time t.
					for (int f = 0; f < ats.length; ++f) {
						double p= dltProbs.getOneToOneProbs().get(t[0], t[1], f, s[0], s[1], e) * rateDens * ats[f];
						lins[e] += p;
						ys.add(t);  // added after May 23
						cps.add(lins[e]); // added after May 23
						arcF.add(f);
						System.out.println(v+"\tBelows["+e+"]\t arcs["+f+ "]\tS["+s[0]+", "+ s[1]+ "] \tt["+t[0] +","+ t[1]+"] \tProb ["+ p+"]" );
					}
					tempCps=lins[e];
				}

				t = reconcHelper.getEpochTimeAboveNotLast(t);
			}

			int idx=-1;
			for (int e = 0; e < sz; ++e){
				// Sample a point in the host tree.
				if (lins[e] < 1e-256) {
					// No signal: choose a point uniformly.
					idx = this.prng.nextInt(ys.size());
					t = ys.get(idx);
				} else {
					// Sample according to probabilities of placements.
					double rnd = this.prng.nextDouble() * cps.get(cps.size()-1);
					idx = 0;
					while (cps.get(idx) < rnd && idx < ys.size()) {
						++idx;
					}
					t = ys.get(idx);

					//System.out.println("\n\n");
					//System.out.println(v+"\tArcs["+e+"]\t prng["+rnd+ "]\tcps["+cps.get(idx)+ "] \tt["+t[0] +","+ t[1]+"] " );
				}
			}
			// Finally, store the properties.
			placements[v] = t;
			absTimes[v]= this.reconcHelper.getTime(t);
			arcTimes[v]= this.reconcHelper.getTime(s)-absTimes[v];

			// Lineage from where the transfer has happend
			int transFromLineage= arcF.get(idx);
			System.out.print("\nt\t["+ t[0] + ", "+ t[1]+ "]\t ArcF ["+transFromLineage+"] \t");

			// check if the event is duplication or transfer
			if (t[1] != 0){
				int lc = G.getLeftChild(v);
				int rc = G.getRightChild(v);
				double dt = reconcHelper.getTimestep(t[0]);	 // get timestep for epoch identifier
				double[] ats = this.ats.get(v).get(t[0], t[1]);
				double dupFact = 2 * dltProbs.getDuplicationRate();
				int adjFact = (this.dltProbs.getTransferProbabilityAdjustment() ? ats.length - 1 : 1);   // Adjust for contemporary species or not.
				double trFact = this.dltProbs.getTransferRate() / adjFact;

				double[] lclins = belows.get(lc).get(t[0], t[1]);
				double[] rclins = belows.get(rc).get(t[0], t[1]);

				double dupProb	=	0.0;
				double[] transProb= new double[ats.length];
				double[] transProbVtoW= new double[ats.length];
				double[] transProbWtoV= new double[ats.length];

				double transProbSum= 0.0;
				double maxLinTransProb= 0.0;

				if (ats.length > 1) {
					double lcsum = 0.0;
					for (double val : lclins) {
						lcsum += val;
					}
					double rcsum = 0.0;
					for (double val : rclins) {
						rcsum += val;
					}
					// duplication probability at lineage arcf
					dupProb 	+= dt * (dupFact * lclins[transFromLineage] * rclins[transFromLineage]); 
					
					// here f refers to different arcs/lineages of species tree
					for (int f = 0; f < ats.length; ++f) {
						transProbVtoW[f] += dt * (trFact * (lclins[f] * (rcsum - rclins[f])));
						transProbWtoV[f] += dt * (trFact * (rclins[f] * (lcsum - lclins[f])));
						transProb[f] += transProbWtoV[f]  + transProbVtoW[f];
						if (maxLinTransProb < transProb[f]){
							maxLinTransProb=transProb[f];
						}
						transProbSum += transProb[f];
					}
					// Generate Random number ranging between (0 to sum(dupProb+transProb) )
					double rnd = this.prng.nextDouble() * (dupProb+transProbSum);
					if (rnd < dupProb ){
						System.out.println("Duplication");
						isDups[v]=true;
					}else{
						System.out.println("Transfer Happens at gene vertix u: "+ v);
						isTrans[v]=true;
						// child that receive the transfered lineage will be
						rnd = this.prng.nextDouble() * (maxLinTransProb/transProbSum);
						for (int e = 0; e < ats.length; ++e) {
							if (e != transFromLineage){
								if (rnd < (transProbVtoW[e]/transProbSum)){  // select the child where V stays but W get transfered to specie lineage e, also Normalizing each component
									System.out.println("Child 'V': "+ lc + " Stays but Child 'W': "+ rc +" got Transfered to specie Arc:" + e);
									break;								
								}else if (rnd < (transProbWtoV[e]/transProbSum)){  // select the child where W stays but V get transfered to specie lineage e,  also Normalizing each component
									System.out.println("Child 'W': "+ rc + " Stays but Child 'V': "+ lc +" got Transfered to specie Arc:" + e);
									break;
								}
								else if(e== ats.length && (rnd == 0.0 && transProb[e] == 0.0)){
									System.out.println("Transfered Child lineage goes extinct in the middle before reaching Arc:" + e);
								}
							}
						}
					}

				} else {
					// Case with top time edge. No transfer possible.
					ats[0] = dt * dupFact * lclins[0] * rclins[0];
					System.out.println("Duplication");
					isDups[v]=true;
				}

			}else{
				System.out.println("V: "+v+ "\t Speciation");
			}





		} // else ends here

	}



	/**
	 * Closes the underlying buffer.
	 * @throws IOException 
	 */
	public void close() throws IOException {
		this.out.close();
	}

	/**
	 * Flushes the underlying buffer.
	 * @throws IOException 
	 */
	public void flush() throws IOException {
		this.out.flush();
	}

	/**
	 * Returns a proper representation of a lower limit.
	 * @param loLim the lower limit, holding arc and discretisation point in one int.
	 * @return [arc in S, discretisation point].
	 */
	private static int[] getProperLolim(int loLim) {
		int[] prop = new int[2];
		prop[0] = ((loLim << 16) >>> 16);   // Arc (=head vertex of arc).
		prop[1] = (loLim >>> 16);           // Discretisation point.
		return prop;
	}

	@Override
	public Class<?> getSampleType() {
		return SampleInt.class;
	}


	@Override
	public String getSampleHeader() {
		return "RealisationID\tMaxProbabilityRealisation";
	}


	@Override
	public String getSampleValue(SamplingMode mode) {
		StringBuilder str = new StringBuilder(1024);

		// Use current iteration as ID to be able to tie MCMC sample and realisation samples together.
		String id = "" + this.iteration.getIteration();
		str.append(id);

		// Vertices of G in topological ordering from root to leaves.
		List<Integer> vertices = this.G.getTopologicalOrdering();

		// Output max prob. realisation in ordinary file.
		Realisation real = this.getMaximumProbabilityRealisation(vertices);

		str.append('\t').append(real.toString());

		// Do sampling to own file, in ordinary cases.
		if (mode == SamplingMode.ORDINARY) {
			for (int i = 0; i < this.noOfRealisations; ++i) {
				try {
					this.out.write(id);
					this.out.write('\t');
					this.out.write("" + i);
					this.out.write('\t');
					real = this.sample(vertices);
					this.out.write(real.toString());
					this.out.write('\n');
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return str.toString();
	}

}
