package se.cbb.jprime.io;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

import se.cbb.jprime.apps.realise.Realisation;
import se.cbb.jprime.topology.BooleanMap;
import se.cbb.jprime.topology.NamesMap;
import se.cbb.jprime.topology.RBTree;
import se.cbb.jprime.topology.RBTreeArcDiscretiser;
import se.cbb.jprime.topology.RootedBifurcatingTree;
import se.cbb.jprime.topology.StringMap;
import se.cbb.jprime.topology.TimesMap;
import se.cbb.jprime.topology.TopologyException;

/**
 * Holds an unparsed rooted tree realisation as a PrIME Newick tree. All the properties of the realisation can be accessed through the
 * PrIME Newick tree properties. Some useful methods are provided, however, such as string representations for e.g. hashing or comparisons.
 * 
 * @author Joel Sjöstrand.
 * @author Mehmood Alam Khan
 */
public class UnparsedRealisation {

	/**
	 * For comparisons with other realisations, provides different types of string representations where various levels
	 * of info have been stripped away.
	 * 
	 * @author Joel Sjöstrand.
	 */
	public enum Representation {
		/** Includes only the topology. */
		TOPOLOGY,
		/** Includes point info, where speciations such as (3,0) are untouched but non-speciations such as (3,2) turns into (3,X). */
		RECONCILIATION,
		/** Includes point info. */
		REALISATION,
		/** Includes point info along with auxiliary info such as branch lengths, etc. */
		REALISATION_PLUS
	}
	
	/** For finding PrIME tags + optionally branch lengths. */
	private static final Pattern PRIME_TAG = Pattern.compile("(:[0-9\\+\\-\\.eE]+)?(\\[&&PRIME [^\\]]*\\])");
	
	/** For point tags. */
	private static final Pattern DISC_PT_TAG = Pattern.compile("(DISCPT=\"?\\([0-9\\+\\-\\.eE]+,[0-9\\+\\-\\.eE]+\\))\"?");
	
	/** Sorted string representation of the sample. */
	public final String treeAsNewickString;
	
	/** Tree. */
	public final PrIMENewickTree tree;
	
	/** Realisation ID. */
	public final int realID;
	
	/** Subsample ID. */
	public final int subSampleID;
	
	/** Realisation. */
	public Realisation realisation;;
	
	/**
	 * Constructor.
	 * @param real realisation.
	 * @param realId realisation's mapping to MCMC sample.
	 * @param subSampleID realisation's subsample within the MCMC sample.
	 * @throws TopologyException 
	 * @throws NewickIOException.
	 */
	public UnparsedRealisation(String real, int realID, int subSampleID) throws NewickIOException, TopologyException {
		this.tree = PrIMENewickTreeReader.readTree(real, true, true);
		this.treeAsNewickString = tree.toString();  // Guaranteed to be sorted unlike original string.
		this.realID = realID;
		this.subSampleID = subSampleID;
		this.realisation = parseDLRSRealisation(real);
	}
	
	
	/**
	 * Parse a string of realization (DLRS) to the realization object
	 * @throws NewickIOException 
	 * @throws TopologyException 
	 */
	public static Realisation parseDLRSRealisation(String real) throws NewickIOException, TopologyException
	{
		PrIMENewickTree tree = PrIMENewickTreeReader.readTree(real, true, true);
		TimesMap times = tree.getTimesMap(real);	
		String[] names = new String[tree.getNoOfVertices()];
		boolean[] isdups = new boolean[tree.getNoOfVertices()];
		boolean[] istrans = new boolean[tree.getNoOfVertices()];
		String[] placements = new String[tree.getNoOfVertices()];
		String[] fromTos = new String[tree.getNoOfVertices()];
		String[] speciesEdge = new String[tree.getNoOfVertices()];
		
		
		for(int v =0; v < tree.getNoOfVertices(); v++)
		{
			names[v]=tree.getVertex(v).getName();
			String meta = tree.getVertex(v).getMeta();
			// 0 for leaf, 1 for speciation, 2 for duplication, 3 for transfer
			int vertextype = getVertexType(meta);
			
			isdups[v] = false;
			istrans[v] = false;
			
			if (vertextype == 2)
				isdups[v]=true;
			else if( vertextype == 3)
				istrans[v]=true;
			
			int [] fromtos = {-1, -1, -1};	
			if (vertextype == 3){
				fromtos = getFromToPoints(meta);
				fromTos[v] = "("+fromtos[0]+","+fromtos[1]+","+fromtos[2]+")";
				
				int[] specieEdgePlacement= getSpeciesEdge(meta);
				speciesEdge[v]= "("+specieEdgePlacement[0]+","+specieEdgePlacement[1]+")";
			}
			int[] placement = getRealisedPoint(meta);
			placements[v]= "("+placement[0]+","+placement[1]+")";
		}
		
		
		NamesMap Names = new NamesMap("GuestTreeNames", names);
		BooleanMap isDups = new BooleanMap("RealisationIsDups", isdups);
		BooleanMap isTrans = new BooleanMap("RealisationIsTrans", istrans);
		StringMap Placements = new StringMap("DiscPts",placements);
		StringMap FromTos = new StringMap("fromToLineage",fromTos);
		StringMap SpeciesEdge =new StringMap("speciesEdge",speciesEdge); 
		

		RBTree rbtree = new RBTree((NewickTree) tree,"");
		Realisation realisation = new Realisation((RootedBifurcatingTree) rbtree, Names, times, isDups, isTrans, Placements, FromTos, SpeciesEdge);
		return (realisation );
	}	
	
	
	/**
	 * Parse a string of realization (DLTRS) to the realization object
	 * @throws NewickIOException 
	 * @throws TopologyException 
	 */
	public static Realisation parseRealisation(String real) throws NewickIOException, TopologyException
	{
		PrIMENewickTree tree = PrIMENewickTreeReader.readTree(real, true, true);			// Sorts the gene tree
		TimesMap times = tree.getTimesMap(real);	
		String[] names = new String[tree.getNoOfVertices()];
		boolean[] isdups = new boolean[tree.getNoOfVertices()];
		boolean[] istrans = new boolean[tree.getNoOfVertices()];
		String[] placements = new String[tree.getNoOfVertices()];
		String[] fromTos = new String[tree.getNoOfVertices()];
		String[] speciesEdge = new String[tree.getNoOfVertices()];
		
		for(int v =0; v < tree.getNoOfVertices(); v++)
		{
			names[v]=tree.getVertex(v).getName();
			String meta = tree.getVertex(v).getMeta();
			// 0 for leaf, 1 for speciation, 2 for duplication, 3 for transfer
			int vertextype = getVertexType(meta);
			
			isdups[v] = false;
			istrans[v] = false;
			
			if (vertextype == 2)
				isdups[v]=true;
			else if( vertextype == 3)
				istrans[v]=true;
			
			
			int [] fromtos = {-1, -1, -1};	
			if (vertextype == 3){
				fromtos = getFromToPoints(meta);
				fromTos[v] = "("+fromtos[0]+","+fromtos[1]+","+fromtos[2]+")";
				
				int[] specieEdgePlacement= getSpeciesEdge(meta);
				speciesEdge[v]= "("+specieEdgePlacement[0]+","+specieEdgePlacement[1]+")";
			}
			int[] placement = {0,0};
			if(!tree.getVertex(v).isLeaf())
			{
				placement = getRealisedPoint(meta);
				placements[v]= "("+placement[0]+","+placement[1]+")";
			}
		}
		
		
		NamesMap Names = new NamesMap("GuestTreeNames", names);
		BooleanMap isDups = new BooleanMap("RealisationIsDups", isdups);
		BooleanMap isTrans = new BooleanMap("RealisationIsTrans", istrans);
		StringMap Placements = new StringMap("DiscPts",placements);
		StringMap FromTos = new StringMap("fromToLineage",fromTos);
		StringMap SpeciesEdge =new StringMap("speciesEdge",speciesEdge); 
		
		RBTree rbtree = new RBTree((NewickTree) tree,"");
		Realisation realisation = new Realisation((RootedBifurcatingTree) rbtree, Names, times, isDups, isTrans, Placements, FromTos, SpeciesEdge);
		return (realisation );
	}
	
	
	/**
	 * Parse a string of realization (DLTRS) to the realization object (also sorts the vertices of sampled
	 * gene tree with the true one)
	 * @throws NewickIOException 
	 * @throws TopologyException 
	 */
	public static Realisation parseRealisation(String real, NamesMap trueNamesMap) throws NewickIOException, TopologyException
	{
		PrIMENewickTree tree = PrIMENewickTreeReader.readTree(real, true, true);			// Sorts the gene tree
		RBTree rbTree= new RBTree((NewickTree) tree,"");
		String[] names = new String[tree.getNoOfVertices()];
		TimesMap times = tree.getTimesMap(real);	

		boolean[] isdups = new boolean[tree.getNoOfVertices()];
		boolean[] istrans = new boolean[tree.getNoOfVertices()];
		String[] placements = new String[tree.getNoOfVertices()];
		String[] fromTos = new String[tree.getNoOfVertices()];
		String[] speciesEdge = new String[tree.getNoOfVertices()];
		
		for(int v =0; v < tree.getNoOfVertices(); v++)
		{
			names[v]=tree.getVertex(v).getName();
			String meta = tree.getVertex(v).getMeta();
			// 0 for leaf, 1 for speciation, 2 for duplication, 3 for transfer
			int vertextype = getVertexType(meta);
			
			isdups[v] = false;
			istrans[v] = false;
			
			if (vertextype == 2)
				isdups[v]=true;
			else if( vertextype == 3)
				istrans[v]=true;
			
			
			int [] fromtos = {-1, -1, -1};	
			if (vertextype == 3){
				fromtos = getFromToPoints(meta);
				fromTos[v] = "("+fromtos[0]+","+fromtos[1]+","+fromtos[2]+")";
			}
                        if(vertextype!=0){
                            int[] specieEdgePlacement= getSpeciesEdge(meta);
                            speciesEdge[v]= "("+specieEdgePlacement[0]+","+specieEdgePlacement[1]+")";
                        }
			int[] placement = {0,0};
			if(!tree.getVertex(v).isLeaf())
			{
				placement = getRealisedPoint(meta);
				placements[v]= "("+placement[0]+","+placement[1]+")";
			}
			
		}
		
		
		NamesMap Names = new NamesMap("GuestTreeNames", names);
		BooleanMap isDups = new BooleanMap("RealisationIsDups", isdups);
		BooleanMap isTrans = new BooleanMap("RealisationIsTrans", istrans);
		StringMap Placements = new StringMap("DiscPts",placements);
		StringMap FromTos = new StringMap("fromToLineage",fromTos);
		StringMap SpeciesEdge =new StringMap("speciesEdge",speciesEdge); 
		
		RBTree rbtree = new RBTree((NewickTree) tree,"");
		Realisation realisation = new Realisation((RootedBifurcatingTree) rbtree, Names, times, isDups, isTrans, Placements, FromTos, SpeciesEdge);
		return (realisation );
	}
        
        
	/**
	 * Returns the vertex type of the meta
	 * 0 for leaf, 1 for speciation, 2 for duplication, 3 for transfer
	 * @param meta represents the realized information of vertex
	 * @return vertex type
	 */
	private static int getVertexType(String meta){
		String str = meta.substring(meta.indexOf("VERTEXTYPE=")+11 , meta.indexOf("VERTEXTYPE=")+15 );
		if (str.equals("Leaf"))
			return 0;
		else if (str.equals("Spec"))
			return 1;
		else if (str.equals("Dupl"))
			return 2;
		else if (str.equals("Tran"))
			return 3;
		else 
			return -1;

	}
		
        	/**
	 * Returns the FromTo transfer points of the meta
	 * @param meta represents the realized information of vertex
	 * @return y [realised points in array]
	 */
	private static int[] getFromToPoints(String meta){
		
		String str = meta.substring(meta.indexOf("FROMTOLINEAGE=("), meta.length());
		str = meta.substring(meta.indexOf("FROMTOLINEAGE=("), meta.indexOf(")"));
		int y1=-1, y2=-1, y3=-1;
		
		int noOfelements = StringUtils.countMatches(str, ",")+1;
		if (noOfelements == 2)
		{
			y1 = Integer.parseInt(str.substring(str.indexOf("FROMTOLINEAGE=(")+15 , str.indexOf(",") ));
			y2 = Integer.parseInt(str.substring(str.indexOf(",")+1 , str.length() ));
		}else if(noOfelements == 3)
		{
			y1 = Integer.parseInt(str.substring(str.indexOf("FROMTOLINEAGE=(")+15 , str.indexOf(",") ));
			y2 = Integer.parseInt(str.substring(str.indexOf(",")+1 , str.lastIndexOf(",") ));
			y3 = Integer.parseInt(str.substring(str.lastIndexOf(",")+1 , str.length() ));
		}
		
//		String str = meta.substring(meta.indexOf("FROMTOLINEAGE=("), meta.indexOf("DISCPT"));
//		
//		int y1 = Integer.parseInt(str.substring(str.indexOf("FROMTOLINEAGE=(")+15 , str.indexOf(",") ));
//		int y2 = Integer.parseInt(str.substring(str.indexOf(",")+1 , str.lastIndexOf(",") ));
//		int y3 = Integer.parseInt(str.substring(str.lastIndexOf(",")+1 , str.lastIndexOf(")") ));

		int y[] = {y1, y2, y3};
		return y;
		
	}	
        
        
	/**
	 * Returns the FromTo transfer points of the meta
	 * @param meta represents the realized information of vertex
	 * @return y [realised points in array]
	 */
	private static int[] getSpeciesEdge(String meta){
		String str = meta.substring(meta.indexOf("SPECIES_EDGE=("), meta.indexOf("DISCPT"));
		
		int s1 = Integer.parseInt(str.substring(str.indexOf("SPECIES_EDGE=(")+14 , str.indexOf(",") ));
		int s2 = Integer.parseInt(str.substring(str.lastIndexOf(",")+1 , str.lastIndexOf(")") ));

		int s[] = {s1, s2};
		return s;
		
	}
	
	/**
	 * Returns the Realization points of the meta
	 * @param meta represents the realized information of vertex
	 * @return y [realised points in array]
	 */
	private static int[] getRealisedPoint(String meta){
		
		int y1 = Integer.parseInt(meta.substring(meta.indexOf("DISCPT=(")+8 , meta.lastIndexOf(",") ));
		int y2 = Integer.parseInt(meta.substring(meta.lastIndexOf(",")+1 , meta.lastIndexOf(")") ));
		int y[] = {y1, y2};
		return y;
	}
	/**
	 * For string comparisons, hashing, etc., returns a sorted Newick tree representation where a user-defined
	 * level of info has been stripped away.
	 * @param rt the desired info-level.
	 * @return the string representation.
	 */
	public String getStringRepresentation(Representation rt) {
		if (rt == Representation.REALISATION_PLUS) {
			return this.treeAsNewickString;
		}
		StringBuffer nw = new StringBuffer(this.treeAsNewickString.length());
		Matcher m = PRIME_TAG.matcher(this.treeAsNewickString);
		Matcher m2;
		switch (rt) {
		case REALISATION:
			while (m.find()) {
				m2 = DISC_PT_TAG.matcher(m.group(m.groupCount()));
				if (m2.find()) {
					m.appendReplacement(nw, "[&&PRIME " + m2.group(1) + ']');
				}
			}
			break;
		case RECONCILIATION:
			while (m.find()) {
				m2 = DISC_PT_TAG.matcher(m.group(m.groupCount()));
				if (m2.find()) {
					m.appendReplacement(nw, "[&&PRIME " +  m2.group(1).replaceAll(",[1-9][0-9]*", ",X") + ']');
				}
			}
			break;
		case TOPOLOGY:
			while (m.find()) {
				m2 = DISC_PT_TAG.matcher(m.group(m.groupCount()));
				if (m2.find()) {
					m.appendReplacement(nw, "");
				}
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown String representation type for realisation.");
		}
		m.appendTail(nw);
		return nw.toString();
	}
	
	
	/**
	 * Compares two realisations
	 * @throws TopologyException 
	 * @throws NewickIOException 
	 * @params realisation1
	 * @params realisation2
	 * returns true if similar or false otherwise
	 */
	public static boolean compareRealisation(String real1, String real2) throws NewickIOException, TopologyException
	{
		boolean flag = true;
		
		Realisation r1=UnparsedRealisation.parseRealisation(real1);
		Realisation r2=UnparsedRealisation.parseRealisation(real2);
		RootedBifurcatingTree t1 =  r1.getTree();
		RootedBifurcatingTree t2 =  r2.getTree();
		assert(t1==t2);									// Assumes the realisations have same gene tree otherwise comparing realisation makes no sense
		
		int noofvertices = r1.getTree().getNoOfVertices();
		
		for (int i=0; i<noofvertices; i++)
		{
			if(Integer.parseInt(r1.getPlacements().get(i).substring(r1.getPlacements().get(i).indexOf("(")+1, r1.getPlacements().get(i).indexOf(","))) != Integer.parseInt(r2.getPlacements().get(i).substring(r2.getPlacements().get(i).indexOf("(")+1 , r2.getPlacements().get(i).indexOf(",") ))  
					|| (Integer.parseInt(r1.getPlacements().get(i).substring(r1.getPlacements().get(i).indexOf(",")+1, r1.getPlacements().get(i).indexOf(")"))) != Integer.parseInt(r2.getPlacements().get(i).substring(r2.getPlacements().get(i).indexOf(",")+1 , r2.getPlacements().get(i).indexOf(")") ))))
					return false;
		}
		return flag;
	}
	
	
	/**
	 * Compares two realisations
	 * @throws TopologyException 
	 * @throws NewickIOException 
	 * @params realisation
	 * @params Vertices-Realisation Points Map
	 * returns the populated map
	 */
	public static void verticesDistribution(String real, LinkedHashMap<Integer, List<Double>> vrMap, RBTreeArcDiscretiser dtimes) throws NewickIOException, TopologyException
	{
		boolean flag = true;
		Realisation r=UnparsedRealisation.parseRealisation(real);
		RootedBifurcatingTree t =  r.getTree();
		int noofvertices = t.getNoOfVertices();
		
		for (int i=0; i<noofvertices; i++){
			if(vrMap.get(i) != null){
				List<Double> list = vrMap.get(i);
				String meta = r.getPlacements().get(i);
				int [] point = {0,0};
				point[0]=Integer.parseInt(meta.substring(meta.indexOf("(")+1 , meta.lastIndexOf(",") ));
				point[1]=Integer.parseInt(meta.substring(meta.indexOf(",")+1 , meta.lastIndexOf(")") ));
				Double time = dtimes.getDiscretisationTime(point[0], point[1]);
				list.add(time);
				vrMap.put(i, list);
			}
			else{
				List<Double> list = new ArrayList<Double>();
				String meta = r.getPlacements().get(i);
				int [] point={0,0};
				point[0]=Integer.parseInt(meta.substring(meta.indexOf("(")+1 , meta.lastIndexOf(",") ));
				point[1]=Integer.parseInt(meta.substring(meta.indexOf(",")+1 , meta.lastIndexOf(")") ));
				Double time = dtimes.getDiscretisationTime(point[0], point[1]);
				list.add(time);
				vrMap.put(i, list);
			}			
		}
	}
}
