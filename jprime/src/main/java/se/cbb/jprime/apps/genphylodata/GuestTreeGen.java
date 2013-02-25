package se.cbb.jprime.apps.genphylodata;

import java.io.BufferedWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.beust.jcommander.JCommander;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.apps.genphylodata.GuestVertex.Event;
import se.cbb.jprime.apps.genphylodata.GuestVertex.Prunability;
import se.cbb.jprime.io.JCommanderUsageWrapper;
import se.cbb.jprime.io.NewickTree;
import se.cbb.jprime.io.NewickTreeWriter;
import se.cbb.jprime.io.NewickVertex;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.math.PRNG;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeEpochDiscretiser;
import se.cbb.jprime.topology.TimesMap;

/**
 * Generates an ultrametric guest tree ("gene tree") involving inside a host tree ("species tree"),
 * by means of a BD process or similarly.
 * 
 * @author Joel Sjöstrand.
 */
public class GuestTreeGen implements JPrIMEApp {

	private RBTreeEpochDiscretiser hostTree;
	private NamesMap hostNames;
	private PRNG prng;
	private EventCreator mightyGodPlaysDice;
	private int attempts = 0;
	private PrIMENewickTree unprunedTree;
	private PrIMENewickTree prunedTree;


	@Override
	public String getAppName() {
		return "GuestTreeGen";
	}

	@Override
	public void main(String[] args) throws Exception {
		
		try {
			// ================ PARSE USER OPTIONS AND ARGUMENTS ================
			GuestTreeGenParameters params = new GuestTreeGenParameters();
			JCommander jc = new JCommander(params, args);
			if (args.length == 0 || params.help || params.args.size() != 5) {
				StringBuilder sb = new StringBuilder(65536);
				sb.append(
						"================================================================================\n" +
						"GuestTreeGen is part of the JPrIME-GenPhyloData suite of tools for creating\n" +
						"realistic phylogenetic data. GuestTreeGen takes a Newick \"host\" tree with\n" +
						"ultrametric branch lengths and generates a \"guest\" tree evolving inside the\n" +
						"host tree. This is achieved through a canonical extension of a birth-death\n" +
						"process, in which guest tree lineages may be duplicated, lost, or laterally\n" +
						"transferred (be split with one copy being transferred to a contemporaneous host\n" +
						"edge). Guest lineages branch deterministically at host tree vertices. Auxiliary\n" +
						"files detailing the process is also created by default.\n\n" +
						"References:\n" +
						"    In press\n\n" +
						"Releases, tutorial, etc: http://code.google.com/p/jprime/wiki/GenPhyloData\n\n" +
						"License: JPrIME is available under the New BSD License.\n" +
						"================================================================================\n");
				sb.append("Usage:\n" +
						"    java -jar jprime-X.Y.Z.jar GuestTreeGen [options] <host tree> <dup rate> <loss rate> <trans rate> <out prefix>\n");
				JCommanderUsageWrapper.getUnsortedUsage(jc, params, sb);
				System.out.println(sb.toString());
				return;
			}
			
			// Read host tree.
			PrIMENewickTree nw = params.getHostTree();
			RBTree S = new RBTree(nw, "HostTree");
			this.hostNames = nw.getVertexNamesMap(false, "HostNames");
			TimesMap STimes = nw.getTimesMap("HostTimes");
			if (params.stem != null) {
				STimes.getArcTimes()[S.getRoot()] = Double.parseDouble(params.stem);
			}
			// Hack: Set 0 stem to eps.
			if (STimes.getArcTime(S.getRoot()) <= 0.0) {
				STimes.getArcTimes()[S.getRoot()] = 1.0e-64;
			}
			hostTree = new RBTreeEpochDiscretiser(S, this.hostNames, STimes);
			
			// PRNG.
			prng = (params.seed == null ? new PRNG() : new PRNG(new BigInteger(params.seed)));
			
			// Helper.
			mightyGodPlaysDice = new EventCreator(hostTree, params.getDuplicationRate(), params.getLossRate(),
					params.getTransferRate(), Double.parseDouble(params.leafSamplingProb), prng);
			
			// Leaf size samples.
			ArrayList<Integer> leafSizes = params.getLeafSizes();
			
			// Produce tree.
			GuestVertex unprunedRoot;
			int exact = -1;
			do {
				if (attempts > params.maxAttempts) {
					if (!params.doQuiet) {
						BufferedWriter outinfo = params.getOutputFile(".info");
						outinfo.write("Failed to produce valid pruned tree within max allowed attempts.\n");
						outinfo.close();
					}
					System.err.println("Failed to produce valid pruned tree within max allowed attempts.");
					System.exit(0);
				}
				unprunedRoot = createRawUnprunedTree();
				int no = PruningHelper.labelUnprunableVertices(unprunedRoot, 0);
				PruningHelper.labelPrunableVertices(unprunedRoot, no, params.vertexPrefix);
				attempts++;
				if (leafSizes != null) {
					exact = leafSizes.get(this.prng.nextInt(leafSizes.size()));
				}
			} while (!isOK(unprunedRoot, hostTree, params.min, params.max, params.minper, params.maxper, exact));
			
			// Restore 0 length stem.
			if (unprunedRoot.getBranchLength() <= 1.0e-32) {
				unprunedRoot.setBranchLength(0.0);
			}
			
			// Meta.
			if (!params.excludeMeta) {
				this.setMeta(unprunedRoot);
			}
			
			// Finally, some trees.
			GuestVertex prunedRoot = PruningHelper.prune(unprunedRoot);
			this.unprunedTree = new PrIMENewickTree(new NewickTree(unprunedRoot, "[&&PRIME NAME=UnprunedTree]", false, false), false);
			this.prunedTree = new PrIMENewickTree(new NewickTree(prunedRoot, "[&&PRIME NAME=PrunedTree]", false, false), false);;
			
			// Print output.
			if (params.doQuiet) {
				System.out.println(NewickTreeWriter.write(this.prunedTree));
			} else {
				BufferedWriter out = params.getOutputFile(".unpruned.tree");
				out.write(NewickTreeWriter.write(this.unprunedTree));
				out.close();
				out = params.getOutputFile(".pruned.tree");
				out.write(NewickTreeWriter.write(this.prunedTree));
				out.close();
				out = params.getOutputFile(".info");
				out.write(this.getInfo());
				out.close();
				if (!params.excludeMeta) {
					out = params.getOutputFile(".unpruned.guest2host");
					out.write(this.getSigma(this.unprunedTree));
					out.close();
					out = params.getOutputFile(".unpruned.host2guest");
					out.write(this.getGamma(this.unprunedTree));
					out.close();
					out = params.getOutputFile(".pruned.guest2host");
					out.write(this.getSigma(this.prunedTree));
					out.close();
					out = params.getOutputFile(".pruned.host2guest");
					out.write(this.getGamma(this.prunedTree));
					out.close();
				}
			}
						
			// TODO: Implement helpers to create the tree.
			
			String outtree = "";
			int noOfUnprunedSpecs;
			int noOfPrunedSpecs;
			int noOfUnprunedDups;
			int noOfPrunedDups;
			int noOfUnprunedLosses;
			int noOfPrunedLosses;
			int noOfUnprunedTrans;
			int noOfPrunedTrans;
			double unprunedTotalTime;
			double prunedTotalTime;
			
			
			
//			// Output tree.
//			if (params.outputfile == null) {
//				System.out.println(outtree);
//			} else {
//				Pair<BufferedWriter, BufferedWriter> out = params.getOutputFiles();
//				out.first.write(outtree + '\n');
//				out.first.close();
//				out.second.write("# GuestTreeGen\n");
//				out.second.write("Arguments:\t" +  Arrays.toString(args) + '\n');
//				out.second.write("Host tree:\t" + params.args.get(0) + '\n');
//				out.second.write("Duplication rate:\t" + lambda + '\n');
//				out.second.write("Loss rate:\t" + mu + '\n');
//				out.second.write("Transfer rate:\t" + tau + '\n');
//				out.second.close();
//			}
//			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.print("\nUse option -h or --help to show usage.\n");
		}
	}

	private String getGamma(PrIMENewickTree unprunedTree2) {
		// TODO Auto-generated method stub
		return "";
	}

	private String getSigma(PrIMENewickTree unprunedTree2) {
		// TODO Auto-generated method stub
		return "";
	}

	private String getInfo() {
		// TODO Auto-generated method stub
		return "";
	}

	/**
	 * Creates an unpruned tree.
	 * @return the root.
	 */
	private GuestVertex createRawUnprunedTree() {
		LinkedList<GuestVertex> alive = new LinkedList<GuestVertex>();
				
		GuestVertex root = mightyGodPlaysDice.createGuestVertex(this.hostTree.getRoot(), this.hostTree.getTipToLeafTime());
		alive.add(root);
		while (!alive.isEmpty()) {
			GuestVertex lin = alive.pop();
			if (lin.event == Event.LOSS || lin.event == Event.SAMPLED_LEAF || lin.event == Event.UNSAMPLED_LEAF) {
				continue;	
			}
			
			GuestVertex lc = null;
			GuestVertex rc = null;
			if (lin.event == Event.SPECIATION) {
				lc = mightyGodPlaysDice.createGuestVertex(this.hostTree.getLeftChild(lin.sigma), lin.abstime);
				rc = mightyGodPlaysDice.createGuestVertex(this.hostTree.getRightChild(lin.sigma), lin.abstime);
			} else if (lin.event == Event.DUPLICATION) {
				lc = mightyGodPlaysDice.createGuestVertex(lin.sigma, lin.abstime);
				rc = mightyGodPlaysDice.createGuestVertex(lin.sigma, lin.abstime);
			} else if (lin.event == Event.TRANSFER) {
				if (this.prng.nextDouble() < 0.5) {
					lc = mightyGodPlaysDice.createGuestVertex(lin.sigma, lin.abstime);
					rc = mightyGodPlaysDice.createGuestVertex(lin.epoch.sampleArc(this.prng, lin.sigma), lin.abstime);
				} else {
					lc = mightyGodPlaysDice.createGuestVertex(lin.epoch.sampleArc(this.prng, lin.sigma), lin.abstime);
					rc = mightyGodPlaysDice.createGuestVertex(lin.sigma, lin.abstime);
				}
			}
			ArrayList<NewickVertex> children = new ArrayList<NewickVertex>(2);
			children.add(lc);
			children.add(rc);
			lin.setChildren(children);
			lc.setParent(lin);
			rc.setParent(lin);
			alive.add(lc);
			alive.add(rc);
		}
		return root;
	}
	
	
	
	/**
	 * Validates.
	 * @param root guest tree root.
	 * @param hostTree host tree.
	 * @param min min sampled leaves.
	 * @param max max sampled leaves.
	 * @param minPer min sampled leaves per host leaf.
	 * @param maxPer max sampled leaves per host leaf.
	 * @param exact -1 if not applicable, otherwise exact number of leaves required.
	 * @return true if OK; otherwise false.
	 */
	private boolean isOK(GuestVertex root, RBTreeEpochDiscretiser hostTree, int min, int max, int minPer, int maxPer, int exact) {
		int sampledLeaves = 0;
		LinkedList<NewickVertex> vertices = new LinkedList<NewickVertex>();
		HashMap<Integer, Integer> sigmaCnt = new HashMap<Integer, Integer>(512); 
		vertices.add(root);
		while (!vertices.isEmpty()) {
			GuestVertex v = (GuestVertex) vertices.pop();
			if (v.event == Event.SAMPLED_LEAF) {
				sampledLeaves++;
				Integer cnt = sigmaCnt.get(v.sigma);
				if (cnt != null) {
					sigmaCnt.put(v.sigma, cnt + 1);
				} else {
					sigmaCnt.put(v.sigma, 1);
				}
			} else if (!v.isLeaf()) {
				vertices.addAll(v.getChildren());
			}
		}
		if (exact != -1 && sampledLeaves != exact) {
			return false;
		}
		if (sampledLeaves < min || sampledLeaves > max) {
			return false;
		}
		for (int l : hostTree.getLeaves()) {
			Integer cnt = sigmaCnt.get(l) ;
			if (cnt == null) { cnt = 0; }
			if (cnt < minPer || cnt > maxPer) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param root
	 */
	private void setMeta(GuestVertex root) {
		LinkedList<NewickVertex> vertices = new LinkedList<NewickVertex>();
		vertices.add(root);
		while (!vertices.isEmpty()) {
			GuestVertex v = (GuestVertex) vertices.pop();
			StringBuilder sb = new StringBuilder(1024);
			sb.append("[&&PRIME");
			sb.append(" ID=").append(v.getNumber());
			switch (v.event) {
			case DUPLICATION:
				sb.append(" VERTEXTYPE=Duplication");
				break;
			case LOSS:
				sb.append(" VERTEXTYPE=Loss");
				break;
			case TRANSFER:
				sb.append(" VERTEXTYPE=Transfer");
				break;
			case SPECIATION:
				sb.append(" VERTEXTYPE=Speciation");
				break;
			case SAMPLED_LEAF:
				sb.append(" VERTEXTYPE=Leaf");
				break;
			case UNSAMPLED_LEAF:
				sb.append(" VERTEXTYPE=UnsampledLeaf");
				break;
			default:
				throw new UnsupportedOperationException("Invalid vertex event type.");	
			}
			sb.append("]");
			v.setMeta(sb.toString());
			if (!v.isLeaf()) {
				vertices.addAll(v.getChildren());
			}
		}
	}
	
}
