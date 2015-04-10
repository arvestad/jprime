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
 * of G in S according to the probability distribution of embeddings under the DLTRS model.
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

	protected boolean stemDoneFlag= false;

	/** Max realization computation flag. */
	protected boolean maxRealizationFlag= false;
	
	/** Realization header . */
	protected String realizationHeader;

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
	public RealisationSampler(String filename, int noOfRealisations, Iteration iteration, PRNG prng, DLTRModel model, DLTRMAPModel msModel, NamesMap names, Boolean maxRealizationFlag) throws IOException {
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
		this.maxRealizationFlag= maxRealizationFlag;
		
		if (this.maxRealizationFlag == true){
			this.realizationHeader= "MaxProbabilityRealisation";
		}else{
			this.realizationHeader= "SampledRealisation";
		}


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
		int n 					= vertices.size();
		int[][] placements 		= new int[n][];  // Sampled points only discretization is stored.
		int[][] fromTo 			= new int[n][];  // Transfer from-to lineage .
		int[][] edgePlacements	= new int[n][2];

		double[] abst 			= new double[n];      // Absolute times.
		double[] arct 			= new double[n];      // Arc times.
		boolean[] isDups 		= new boolean[n];  // Type of point
		boolean[] isTrans 		= new boolean[n];  // Type of point..  

		// For each vertex v of G.
		String[] placementss 	= new String[n];
		String[] fromToLinage	= new String[n];
		String[] speciesEdge	= new String[n];

		//		System.out.println("n=" + n);

		// initializing fromTo array with -1
		for (int v : vertices) {
			int [] t 			= new int[] {-1, -1, -1}; // {-1, -1, -1} for initialzation purpose 
			fromTo[v]			= t;
			edgePlacements[v][0]	= -1;
			edgePlacements[v][1]	= -1;
		}


		for (int v : vertices) {
			getMaxPointLTG(v, placements, fromTo, edgePlacements, abst, arct, isDups, isTrans);
			placementss[v] = "(" + placements[v][0] + "," + placements[v][1] + ")"; 
			fromToLinage[v]= "(" + fromTo[v][0] + "," + fromTo[v][1] + "," + fromTo[v][2] + ")";
			speciesEdge[v]= "("+edgePlacements[v][0]+","+edgePlacements[v][1]+")";
			//			System.out.println("\n placementss["+v+"]"+ placementss[v]);
			//			System.out.println("\n toFromLinage["+v+"]"+ fromToLinage[v]);
			//System.out.println(this.G.toString());
			//System.out.println(this.S.toString());

		}

		// Finally, generate guest tree with times.
		return new Realisation(this.G, this.names, new TimesMap("RealisationTimes", abst, arct), new BooleanMap("RealisationIsDups", isDups), new BooleanMap("RealisationIsTrans", isTrans), new StringMap("DiscPts",placementss), new StringMap("fromToLineage",fromToLinage), new StringMap("speciesEdge",speciesEdge) );

	}



	/**
	 * Samples a realisation given the current guest tree, "at-probabilities", p11-probabilities, etc.
	 * @param vertices vertices of G in topological ordering from root to leaves.
	 */
	public Realisation sample(List<Integer> vertices) {

		int n 					= vertices.size();
		int[][] placements 		= new int[n][];  // Sampled points only discretization is stored.
		int[][] fromTo 			= new int[n][];  // Transfer from-to lineage .
		int[][] edgePlacements	= new int[n][2];

		double[] abst 			= new double[n];      // Absolute times.
		double[] arct 			= new double[n];      // Arc times.
		boolean[] isDups 		= new boolean[n];  // Type of point.
		boolean[] isTrans 		= new boolean[n];  // Type of point..  // changed

		// For each vertex v of G.
		String[] placementss 	= new String[n];
		String[] fromToLinage	= new String[n];
		String[] speciesEdge	= new String[n];

		// initializing fromTo array with -1
		for (int v : vertices) {
			int [] t 			= new int[] {-1, -1, -1}; // {-1, -1, -1} for initialzation purpose 
			fromTo[v]			= t;
			edgePlacements[v][0]	= -1;
			edgePlacements[v][1]	= -1;
		}

		for (int v : vertices) {
			getSamplePointLTG(v, placements, fromTo, edgePlacements, abst, arct, isDups, isTrans);
			fromToLinage[v] = "(" + fromTo[v][0] + "," + fromTo[v][1] + "," + fromTo[v][2] + ")";
			placementss[v] = "(" + placements[v][0] + "," + placements[v][1] + ")"; 
			speciesEdge[v]= "("+edgePlacements[v][0]+","+edgePlacements[v][1]+")";
		}

		// Finally, generate guest tree with times.
		return new Realisation(this.G, this.names, new TimesMap("RealisationTimes", abst, arct), new BooleanMap("RealisationIsDups", isDups), new BooleanMap("RealisationIsTrans", isTrans), new StringMap("DiscPts",placementss),  new StringMap("fromToLineage",fromToLinage),  new StringMap("speciesEdge",speciesEdge) );
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
	private void getMaxPointLTG(int v, int[][] placements, int [][] fromTo, int[][] edgePlacements, double[] absTimes, double[] arcTimes, boolean[] isDups, boolean[] isTrans) {

		// Get placement of parent of v in S'.
		int[] s;
		if (this.G.isRoot(v)) {
			s = this.msTimes.getEpochPtAtTop();
		} else {
			s = placements[this.G.getParent(v)];
			//System.out.println("S " + "(" + s[0] + "," + s[1] + ")");
		}

		double sTime = msReconcHelper.getTime(s);
		double l = msLengths.get(v);
		double[] lins = this.msBelows.get(v).get(s[0], s[1]);
		int sz = lins.length;
		int[] t = this.msReconcHelper.getLoLim(v);
		int lc = G.getLeftChild(v);
		int rc = G.getRightChild(v);

		if (this.G.isLeaf(v)) { // if v is a leaf node of G
			int sigma = this.msReconcHelper.getHostLeafIndex(v);
			//			double rateDens = msSubstPD.getPDF(l / sTime);  // Assumes leaf time 0.
			//
			//			// For each edge e where lineage can start at time s.
			//			// Note: in future you may need to place v on which edge e of the species tree.
			//			double temp= 0.0;
			//			int recordE= -1;
			//			for (int e = 0; e < sz; ++e) {
			//				lins[e] = this.msDltProbs.getOneToOneProbs().get(0, 0, sigma, s[0], s[1], e) * rateDens;
			//				if (temp < lins[e]){
			//					temp = lins[e];
			//					recordE= e;
			//				}
			//			}

			t = new int[] {0, 0};
			placements[v] = t;
			absTimes[v]= this.msReconcHelper.getTime(t);
			arcTimes[v]= this.msReconcHelper.getTime(s)-absTimes[v];

			//			System.out.println("V: "+v+ "\t is Leaf and placed at HostLeafIndex: "+ sigma);

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
			int maxF		= -1;
			int maxE		= -1;
			int totalSpeciesEdges= 0;

			// For each valid time t where u can be placed (strictly beneath s).
			while (t[0] < s[0] || (!(s[0] < t[0]) && t[1] < s[1])) {
				double rateDens = msSubstPD.getPDF(l / (sTime - msReconcHelper.getTime(t)));

				// For each edge e where lineage can start at time s.
				double[] ats = this.msAts.get(v).get(t[0], t[1]);

				if (!this.G.isRoot(v)){
					int e= edgePlacements[this.G.getParent(v)][0];
					// For each edge f where u can be placed at time t.
					for (int f = 0; f < ats.length; ++f) {

						double p= msDltProbs.getOneToOneProbs().get(t[0], t[1], f, s[0], s[1], e) * rateDens * ats[f];
						if  (p > maxp) {
							maxp = p;
							maxT = t;
							maxF= f;
							totalSpeciesEdges= ats.length;
							maxE= e;				
						}

						//						System.out.print(v+"\tEdgeInUpperEdgeGeneration["+e);
						//						System.out.print("]\t EdgeInLowerEdgeGeneration["+f);
						//						System.out.print("]\ts["+s[0]);
						//						System.out.print(", "+ s[1]);
						//						System.out.print("] \tt["+t[0] );
						//						System.out.print(","+ t[1]);
						//						System.out.print("] \tProb ["+ p+"]" );
						//						System.out.println("");
					}
				}else{
					for (int e = 0; e < sz; ++e) {
						// For each edge f where u can be placed at time t.
						for (int f = 0; f < ats.length; ++f) {
							double p= msDltProbs.getOneToOneProbs().get(t[0], t[1], f, s[0], s[1], e) * rateDens * ats[f];
							if  (p > maxp) {
								maxp = p;
								maxT = t;
								maxF= f;
								totalSpeciesEdges= ats.length;
								maxE= e;				
							}

							//							System.out.print(v+"\tEdgeInUpperEdgeGeneration["+e);
							//							System.out.print("]\t EdgeInLowerEdgeGeneration["+f);
							//							System.out.print("]\ts["+s[0]);
							//							System.out.print(", "+ s[1]);
							//							System.out.print("] \tt["+t[0] );
							//							System.out.print(","+ t[1]);
							//							System.out.print("] \tProb ["+ p+"]" );
							//							System.out.println("");
						} //loop over f ends
					} // loop over e ends
				} // else end

				t = msReconcHelper.getEpochTimeAboveNotLast(t);
			}

			t=maxT;

			// Finally, store the properties.
			placements[v] = t;
			edgePlacements[v][0]= maxF;
			edgePlacements[v][1]= totalSpeciesEdges;
			absTimes[v]= this.msReconcHelper.getTime(t);
			arcTimes[v]= this.msReconcHelper.getTime(s)-absTimes[v];

			//			System.out.println("\nE ["+maxE+"] \t F ["+maxF+"] \tt\t["+ t[0] + ", "+ t[1]+ "] maxProb: "+maxp);
			//			System.out.println(" Node ["+v+"] left child ["+lc+"] right child ["+rc+"]");



			// check if the event is duplication or transfer
			if (t[1] != 0) {

				double dt = msReconcHelper.getTimestep(t[0]);	
				double[] ats = this.msAts.get(v).get(t[0], t[1]);
				double dupFact = 2 * msDltProbs.getDuplicationRate();
				int adjFact = (this.msDltProbs.getTransferProbabilityAdjustment() ? ats.length - 1 : 1);   // Adjust for contemporary species or not.
				double trFact = this.msDltProbs.getTransferRate() / adjFact;

				double[] lclins = msBelows.get(lc).get(t[0], t[1]);
				double[] rclins = msBelows.get(rc).get(t[0], t[1]);

				double dupProb	=	0.0;
				double[] transProb= new double[ats.length];
				double[] transProbUtoW= new double[ats.length];
				double[] transProbUtoV= new double[ats.length];

				double transProbSum= 0.0;
				double maxProbAtf=-0.0;
				int maxfIndex=0;

				if (lclins.length > 1) {

					dupProb 	= dt * (dupFact * lclins[maxF] * rclins[maxF]); // duplication part of second equation on paper page 6
					// here f refers to different arcs/lineages of species tree in LowerEdgeGeneration
					// v is the left child of u in G and w is the right child of u in G. in code v refers to u in theory. 
					// Transfer part of second equation on paper page 6
					for (int f = 0; f < lclins.length; ++f) {
						if (maxF == f){
							transProbSum += dt * (trFact * (lclins[maxF] * rclins[f] ));
							transProbSum += dt * (trFact * (rclins[maxF] * lclins[f] ));

						}
						else{
							transProbUtoW[f] += dt * (trFact * (lclins[maxF] * rclins[f] ));
							transProbUtoV[f] += dt * (trFact * (rclins[maxF] * lclins[f] ));
							transProb[f] += transProbUtoV[f]  + transProbUtoW[f];

							if( maxProbAtf < transProb[f]){
								maxProbAtf = transProb[f];
								maxfIndex= f;
							}
							transProbSum+= transProb[f];
						}
					}

					if (dupProb > transProbSum ){
						// check Special transfer
						// check if the parent edge and child each are same and also check if the parent node has transfer or duplication event associated

						if (! this.G.isRoot(v)) {
							//System.out.println("Duplication");
							if (isDups[this.G.getParent(v)] == true && edgePlacements[this.G.getParent(v)][0] != edgePlacements[v][0] ){
								//System.out.println("Special Transfer");
								isTrans[v]	= true;
								fromTo[v][0]= edgePlacements[this.G.getParent(v)][0];
								fromTo[v][1]= maxF;
								fromTo[v][2]= 1; // special transer vertix (set to 1) otherwise -1
							}else{
							//							System.out.println("Duplication");
							//System.out.println(v+"\t t\t["+ t[0] + ", "+ t[1]+ "]\t dupProb ["+dupProb+ "]  Duplication" );
							isDups[v]	=	true;
							}
						}else{
							isDups[v]	=	true;
						}


					}else{
						//						System.out.println("Transfer Happens at gene vertix u: "+ v);
						isTrans[v]		=true;
						//						System.out.println(" maxfIndex"+ maxfIndex+ "\t transProbUtoW.length \t"+ transProbUtoW.length  );
						double probW	= (transProbUtoW[maxfIndex]/transProbSum);
						double probV	= (transProbUtoV[maxfIndex]/transProbSum);


						if (probW > probV){
							fromTo[v][0]= maxF;
							fromTo[v][1]= maxfIndex;

							// select the child where V stays but W get transfered to species lineage f
							//							System.out.println("Child 'V': "+ lc + " Stays but Child 'W': "+ rc +" got Transfered to specie Arc:" + maxfIndex +" with probW: "+ probW );
						}else{
							fromTo[v][0]= maxF;
							fromTo[v][1]= maxfIndex;
							// select the child where W stays but V get transfered to species lineage f, 
							//							System.out.println("Child 'W': "+ rc + " Stays but Child 'V': "+ lc +" got Transfered to specie Arc:" + maxfIndex +" with probV: "+ probV );
						}
					}

				} 
				else {
					// Case with top time edge. No transfer possible.
					ats[0] = dt * dupFact * lclins[0] * rclins[0];
					//					System.out.println("Duplication");
					//System.out.println("\n"+v+"\t F["+maxF+"]\tt\t["+ t[0] + ", "+ t[1]+ "]\t dupProb ["+ats[0]+ "]  Duplication" );

					isDups[v]=true;
				}

			}else{
				//				System.out.println("\nV: "+v+ "\t Speciation");
				//System.out.println(v+"\t F["+maxF+"]\tt\t["+ t[0] + ", "+ t[1]+ "]\t SpeciProb ["+maxp+ "]  " );

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

	private void getSamplePointLTG(int v, int[][] placements, int [][] fromTo, int[][] edgePlacements, double[] absTimes, double[] arcTimes, boolean[] isDups, boolean[] isTrans) {

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
		double tempCps=0.0;

		if (this.G.isLeaf(v)) { // if v is a leaf node of G
			int sigma = this.reconcHelper.getHostLeafIndex(v);
			// donot neet to calculate belows probability for this vertices
			//			double rateDens = substPD.getPDF(l / sTime);  // Assumes leaf time 0.
			//
			//			// For each edge e where lineage can start at time s.
			//			for (int e = 0; e < sz; ++e) {
			//				lins[e] = this.dltProbs.getOneToOneProbs().get(0, 0, sigma, s[0], s[1], e) * rateDens;
			//			}
			t = new int[] {0, 0};
			placements[v] = t;
			absTimes[v]= this.reconcHelper.getTime(t);
			arcTimes[v]= this.reconcHelper.getTime(s)-absTimes[v];

			//			System.out.println("V: "+v+ "\t is Leaf and placed at HostLeafIndex: "+ sigma);

		}else{ // if v is not a leaf of G

			// Stores all valid placement y's.
			ArrayList<int[]> ys = new ArrayList<int[]>(); // added after May 23: need to check if this.ats.getSize() == delirious.ats.length

			// Cumulative probabilities for the y's.
			ArrayList<Double> cps = new ArrayList<Double>();

			// Store lineage 'f' of duplication or transfer on species tree 
			ArrayList<Integer> arcF = new ArrayList<Integer>();

			// Store lineage 'e' of duplication or transfer on species tree 
			ArrayList<Integer> arraylistE = new ArrayList<Integer>();

			// storing probabilities in arraylist
			ArrayList<Double> prob = new ArrayList<Double>();
			
			// store arcs at each epoch  in arraylist
			ArrayList<Integer> speciesArcs = new ArrayList<Integer>();

			// Reset values.
			for (int i = 0; i < sz; ++i) {
				lins[i] = 0.0;
			}

			// We always ignore last time index for at-probs of current epoch,
			// since such values are correctly stored at index 0 of next epoch.
			if (reconcHelper.isLastEpochTime(t)) {
				t = new int[] {t[0]+1, 0};
			}

			int index =0;
			// Compute relative cumulative probabilities for all valid placements y beneath x.
			// For each valid time t where u can be placed (strictly beneath s).
			while ((t[0] < s[0]) || (!(s[0] < t[0]) && t[1] < s[1])) {
				double rateDens = substPD.getPDF(l / (sTime - reconcHelper.getTime(t)));

				// For each edge e where lineage can start at time s.
				double[] ats = this.ats.get(v).get(t[0], t[1]);

				if (!this.G.isRoot(v)){

					int e= edgePlacements[this.G.getParent(v)][0];
					lins[e]=tempCps;
					// For each edge f where u can be placed at time t.
					for (int f = 0; f < ats.length; ++f) {
						double p= dltProbs.getOneToOneProbs().get(t[0], t[1], f, s[0], s[1], e) * rateDens * ats[f];
						lins[e] += p;
						prob.add(p);
						ys.add(t);  // added after May 23
						cps.add(lins[e]); // added after May 23
						arcF.add(f);
						speciesArcs.add(ats.length);
						arraylistE.add(e);
						//						System.out.println(v+"\tUpperEdgeGeneration_E["+e+"]\t LowerEdgeGeneration_F["+f+ "]\tS["+s[0]+", "+ s[1]+ "]\tt["+t[0] +","+ t[1]+"] index: "+index+" \tProb ["+ p+"]" );
						index++;
					}
					tempCps=lins[e];

				}else{

					for (int e = 0; e < sz; ++e) {
						lins[e]=tempCps;
						// For each edge f where u can be placed at time t.
						for (int f = 0; f < ats.length; ++f) {
							double p= dltProbs.getOneToOneProbs().get(t[0], t[1], f, s[0], s[1], e) * rateDens * ats[f];
							lins[e] += p;
							prob.add(p);
							ys.add(t);  // added after May 23
							cps.add(lins[e]); // added after May 23
							arcF.add(f);
							speciesArcs.add(ats.length);
							arraylistE.add(e);
							//							System.out.println(v+"\tUpperEdgeGeneration_E["+e+"]\t LowerEdgeGeneration_F["+f+ "]\tS["+s[0]+", "+ s[1]+ "]\tt["+t[0] +","+ t[1]+"] index: "+index+" \tProb ["+ p+"]" );
							index++;
						}
						tempCps=lins[e];
					}

				}// else end here
				// Move to point above.
				t = reconcHelper.getEpochTimeAboveNotLast(t);
			}

			int idx=-1;
			double sumProbAtDifferentLineagesE=tempCps;

			// Sample a point in the host tree.
			if (sumProbAtDifferentLineagesE < 1e-256) {
				// No signal: choose a point uniformly.
				idx = this.prng.nextInt(ys.size());
				t = ys.get(idx);

				//				System.out.println("Node: "+ v+" is placed at \t F["+arcF.get(idx)+"]\tt\t["+ t[0] + ", "+ t[1]+ "]\t prng["+1e-256+ "]\tcps["+cps.get(idx)+"] " );
			} else {

				// Sample according to probabilities of placements.
				double rnd = this.prng.nextDouble() * cps.get(cps.size()-1);
				idx = 0;
				while (cps.get(idx) < rnd && idx < ys.size()) {
					++idx;
				}

				while (prob.get(idx) == 0.0){
					//					System.out.println("prob Zero here: " + prob.get(idx));
					--idx;
				}


				t = ys.get(idx);

				//				System.out.println("\n\n");
				//				System.out.println("Node: "+ v+"\t F["+arcF.get(idx)+"]\tt\t["+ t[0] + ", "+ t[1]+ "]\t prng["+rnd+ "]\tcps["+cps.get(idx)+"] " );
			}

			// Finally, store the properties.
			placements[v] = t;
			edgePlacements[v][0]= arcF.get(idx);
			edgePlacements[v][1]= speciesArcs.get(idx);
			
			absTimes[v]= this.reconcHelper.getTime(t);
			arcTimes[v]= this.reconcHelper.getTime(s)-absTimes[v];

			// Lineage from where the transfer has happend
			int indexF= arcF.get(idx);
			int indexE= arraylistE.get(idx);


			// check if the event is duplication or transfer
			if (t[1] != 0 ){
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
				double[] transProbUtoW= new double[ats.length];
				double[] transProbUtoV= new double[ats.length];

				double transProbSum= 0.0;
				double maxLinTransProb= 0.0;

				//if (ats.length > 1 || this.stemDoneFlag== true) {
				if (ats.length > 1 ) {
					double lcsum = 0.0;
					for (double val : lclins) {
						lcsum += val;
					}
					double rcsum = 0.0;
					for (double val : rclins) {
						rcsum += val;
					}
					// duplication probability at lineage arcf
					dupProb 	+= dt * (dupFact * lclins[indexF] * rclins[indexF]); 

					for (int f = 0; f < lclins.length; ++f) {
						if (indexF == f){
							transProbSum += dt * (trFact * (lclins[indexF] * rclins[f] ));
							transProbSum += dt * (trFact * (rclins[indexF] * lclins[f] ));
						}else{
							transProbUtoW[f] += dt * (trFact * (lclins[indexF] * rclins[f] ));
							transProbUtoV[f] += dt * (trFact * (rclins[indexF] * lclins[f] ));
							transProb[f] += transProbUtoV[f]  + transProbUtoW[f];

							if (maxLinTransProb < transProb[f]){
								maxLinTransProb=transProb[f]; // this will help in defining the range for generating random value below
							}

							transProbSum+= transProb[f];
						}
					}

					// Generate Random number ranging between (0 to sum(dupProb+transProb) )
					double rnd = this.prng.nextDouble() * (dupProb+transProbSum);
					if (rnd < dupProb ){
						if (! this.G.isRoot(v)) {
							if (isDups[this.G.getParent(v)] == true && edgePlacements[this.G.getParent(v)][0] != edgePlacements[v][0] ){
								//								System.out.println("Special Transfer");
								isTrans[v]	= true;
								fromTo[v][0]= edgePlacements[this.G.getParent(v)][0];
								fromTo[v][1]= edgePlacements[v][0];
								fromTo[v][2]= 1; // special transer vertix (set to 1) otherwise -1
							}else{
								isDups[v]	=	true;
							}
						}else{
							//							System.out.println("Duplication");
							//System.out.println(v+"\t t\t["+ t[0] + ", "+ t[1]+ "]\t dupProb ["+dupProb+ "]  Duplication" );
							isDups[v]	=	true;
						}

					}else{
						//						System.out.println("Transfer Happens at gene vertix u: "+ v);

						this.stemDoneFlag= true;

						int j=1;
						int transferedToLineage=-1;
						boolean foundFlag= false;

						while(true && j <= ats.length  ){
							transferedToLineage= this.prng.nextInt(ats.length);
							if (transferedToLineage != indexF){
								foundFlag= true;
								break;
							}
							j++;		
						}

						if (foundFlag == true){
							isTrans[v]=true;
							fromTo[v][0]= indexF;
							fromTo[v][1]= transferedToLineage;
						}else{
							//							System.out.println("This transfer is not possible. since there is only one species lineage at this time point");
							//							System.out.println("Special duplication");
							isDups[v]=true;
						}


						/*
						// child that receive the transfered lineage will be
						rnd = this.prng.nextDouble() * (maxLinTransProb/transProbSum);

						for (int f = 0; f < lclins.length; ++f) {
							if (f != indexF){
								if (rnd <= (transProbUtoW[f]/transProbSum)){  // select the child where V stays but W get transfered to specie lineage e, also Normalizing each component
									fromTo[v][0]= indexF;
									fromTo[v][1]= f;
									System.out.println("Child 'V': "+ lc + " Stays but Child 'W': "+ rc +" got Transfered to specie edge:" + f);
									break;								
								}else if (rnd <= (transProbUtoV[f]/transProbSum)){  // select the child where W stays but V get transfered to specie lineage e,  also Normalizing each component
									fromTo[v][0]= indexF;
									fromTo[v][1]= f;
									System.out.println("Child 'W': "+ rc + " Stays but Child 'V': "+ lc +" got Transfered to specie edge:" + f);
									break;
								}
							}
						}

						 */

					}

				} else {
					// Case with top time edge. No transfer possible.
					ats[0] = dt * dupFact * lclins[0] * rclins[0];
					//					System.out.println("Duplication");
					//System.out.println(v+"\t F["+arcF.get(idx)+"\tt\t["+ t[0] + ", "+ t[1]+ "]\t dupProb ["+ats[0]+ "]\tcps["+cps.get(idx)+"]  Duplication" );
					isDups[v]=true;
				}

			}else{
				//				System.out.println("V: "+v+ "\t Speciation");
				//System.out.println(v+"\t F["+arcF.get(idx)+"\tt\t["+ t[0] + ", "+ t[1]+ "]\t Prob ["+ prob.get(idx)+ "]\tcps["+cps.get(idx)+"] " );
				//this.stemDoneFlag= true;
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
		return this.realizationHeader;
	}


	@Override
	public String getSampleValue(SamplingMode mode) {
		StringBuilder str = new StringBuilder(1024);

		// Vertices of G in topological ordering from root to leaves.
		List<Integer> vertices = this.G.getTopologicalOrdering();

		// Output max prob. realisation in ordinary file.
		Realisation real;
		if (this.maxRealizationFlag == true){
			real = this.getMaximumProbabilityRealisation(vertices);
		}else{
			real = this.sample(vertices);
		}


		//Realisation real = this.sample(vertices);  // uncomment it when test the random sampling and also when everything is working.
		// and comment the line above Realisation real = this.getMaximumProbabilityRealisation(vertices);
		str.append(real.toString());

		return str.toString();
	}

}
