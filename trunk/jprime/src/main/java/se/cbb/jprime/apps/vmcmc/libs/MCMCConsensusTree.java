package se.cbb.jprime.apps.vmcmc.libs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * MCMCConsensusTree: Static helper functions for creation of consensus trees.
 */
abstract public class MCMCConsensusTree{
	/* **************************************************************************** *
	 * 							CLASS PRIVATE FUNCTIONS								*
	 * **************************************************************************** */
	
	/** gatherNodeData: Collects data in form of keys and relatives. Recursive.*/
	private static void gatherNodeData(MCMCTreeNode node, TreeMap<Integer, MCMCTreeNode> nodeMap, TreeMap<Integer, TreeMap<Integer, Integer>> childMap ) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		int 							genericKey;
		int 							numDuplicates;
		int 							childGenericKey;
		String 							nodeName;
		MCMCTreeNode 					tempNode;
		MCMCTreeNode 					newConsensusNode;
		TreeMap<Integer, Integer> 		nodeEntry;
		TreeMap<Integer, Integer> 		newEntry;

		/* ******************** VARIABLE INITIALIZERS ***************************** */
		genericKey 						= node.getGenericKey();
		numDuplicates 					= node.getNumDuplicates();
		nodeName 						= node.getName();
		
		/* ******************** FUNCTION BODY ************************************* */
		if(nodeMap.containsKey(genericKey)) {
			tempNode = nodeMap.get(genericKey);
			tempNode.setNumDuplicates(tempNode.getNumDuplicates() + numDuplicates);
			nodeMap.put(genericKey, tempNode);
		} else {
			newConsensusNode =  new MCMCTreeNode();
			newConsensusNode.setGenericKey(genericKey);
			newConsensusNode.setName(nodeName);
			newConsensusNode.setNumDuplicates(numDuplicates);
			nodeMap.put(genericKey, newConsensusNode);
		}

		if(node.getNumChildren() > 0)
			for(int i = 0; i < node.getNumChildren(); i++) {
				childGenericKey = node.getChildren().get(i).getGenericKey();

				if(childMap.containsKey(genericKey)) {
					nodeEntry = childMap.get(genericKey);

					if(!nodeEntry.containsKey(childGenericKey)) {
						nodeEntry.put(childGenericKey, childGenericKey);
						childMap.put(genericKey, nodeEntry);
					}
				} else {
					newEntry = new TreeMap<Integer, Integer>();
					newEntry.put(childGenericKey, childGenericKey);
					childMap.put(genericKey, newEntry);
				}
			}
		else
			childMap.put(genericKey, new TreeMap<Integer, Integer>());

		for(int i = 0; i < node.getChildren().size(); i++) {
			gatherNodeData(node.getChildren().get(i), nodeMap, childMap);
		}
		
		/* ******************** END OF FUNCTION *********************************** */
	}

	/** gatherAllMajorityNodes: Gathers all majority nodes.*/
	private static void gatherAllMajorityNodes(TreeMap<Integer, MCMCTreeNode> nodeMap, TreeMap<Integer, TreeMap<Integer, Integer>> childMap, int numTrees) {		
		/* ******************** FUNCTION VARIABLES ******************************** */
		Object[] 			values;
		int 				majorityNode;
		
		/* ******************** FUNCTION BODY ************************************* */
		if(nodeMap != null){   
			values = nodeMap.keySet().toArray();
			majorityNode = 0;
			for(int i = 0; i < values.length; i++) {
				majorityNode = (Integer)values[i];

				if(isMajorityNode(majorityNode, numTrees, nodeMap))
					decideMajorityNodes(majorityNode, nodeMap, childMap, numTrees);
			}
		}	
		
		/* ******************** END OF FUNCTION *********************************** */
	}

	/** isMajorityNode: Returns true if genericKey matches majority node. False otherwise.*/
	private static boolean isMajorityNode(int genericKey, int numTrees, TreeMap<Integer, MCMCTreeNode> nodeMap) {
		/* ******************** FUNCTION BODY ************************************* */
		if(nodeMap.containsKey(genericKey))
			if((double)nodeMap.get(genericKey).getNumDuplicates()/numTrees > 0.5)
				return true;
		return false;
		
		/* ******************** END OF FUNCTION *********************************** */
	}

	/** decideMajorityNodes: Decides whether a nodes grandchildren have to be extracted to their grandparent.*/
	private static void decideMajorityNodes(int parentNode, TreeMap<Integer, MCMCTreeNode> nodeMap, TreeMap<Integer, TreeMap<Integer, Integer>> childMap, int numTrees) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		int 			child;
		Object[] 		childNodes = childMap.get(parentNode).keySet().toArray();
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		child 			= 0;
		
		/* ******************** FUNCTION BODY ************************************* */
		for(int i = 0 ; i < childNodes.length; i++){
			child = (Integer)childNodes[i];
			if(!isMajorityNode(child, numTrees, nodeMap)){
				extractMajorityNodes(parentNode, child,nodeMap, childMap, numTrees);
				childMap.get(parentNode).remove(child);
			}
		}
		
		/* ******************** END OF FUNCTION *********************************** */
	}

	/** FindMajorityNodes: Find and return majority nodes as list.*/
	private static ArrayList<Integer> findMajorityNodes(TreeMap<Integer, Integer> childMap, TreeMap<Integer, MCMCTreeNode> nodes,  int numTrees) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		ArrayList<Integer> 			majorityKeys;
		int 						child;
		Object[] 					values;

		/* ******************** VARIABLE INITIALIZERS ***************************** */
		majorityKeys 				= new ArrayList<Integer>();
		child 						= 0;
		values 						= childMap.values().toArray();
		
		/* ******************** FUNCTION BODY ************************************* */
		for(int i = 0; i < values.length; i++) {
			child = (Integer)values[i];
			if(isMajorityNode(child, numTrees, nodes))
				majorityKeys.add(child);
		}		
		Collections.sort(majorityKeys);
		Collections.reverse(majorityKeys);
		return majorityKeys;
		
		/* ******************** END OF FUNCTION *********************************** */
	}

	/** buildConsensusTree: Construct consensus tree.*/
	private static void buildConsensusTree(	MCMCTreeNode node, 
											MCMCTreeNode root, 
											int level, 
											TreeMap<Integer, MCMCTreeNode> nodeMap, 
											TreeMap<Integer, TreeMap<Integer, Integer>> childMap, 
											int numTrees) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		ArrayList<Integer> 					keyNodes;
		MCMCTreeNode 						consChild;
		MCMCTreeNode 						childNode;
		int 								childKey;

		/* ******************** VARIABLE INITIALIZERS ***************************** */
		keyNodes 							= findMajorityNodes(childMap.get(node.getGenericKey()), nodeMap, numTrees);
		
		/* ******************** FUNCTION BODY ************************************* */
		for(int i = 0; i < keyNodes.size(); i++){
			childNode = new MCMCTreeNode();
			childKey = keyNodes.get(i);
			childNode.setGenericKey(childKey);
			consChild = nodeMap.get(childKey);

			if(nodeMap.containsKey(childNode.getGenericKey())) {   
				if(isMajorityNode(childNode.getGenericKey(),numTrees, nodeMap)) {
					childNode.setLevel(level+1);
					consChild.setLevel(level+1);
					root.addChild(consChild);
					nodeMap.remove(childNode.getGenericKey());
				}
			}
			buildConsensusTree(childNode , consChild, level+1, nodeMap, childMap, numTrees);
		}
		
		/* ******************** END OF FUNCTION *********************************** */
	}
	
	/* **************************************************************************** *
	 * 							CLASS PUBLIC FUNCTIONS								*
	 * **************************************************************************** */
	
	/** addGenericKeyCodes: Calculate and store key codes recursively. */
	public static int addGenericKeyCodes(MCMCTreeNode node) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		int 				genericKey;
		int 				sum;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		sum 				= 0;

		/* ******************** FUNCTION BODY ************************************* */
		if(node.getNumChildren() > 0) {
            for(int i = 0; i < node.getNumChildren(); i++)
                sum += addGenericKeyCodes(node.getChildren().get(i));
            node.setGenericKey(sum);
        } else {
            genericKey = calculateLeafKey(node.getName().toCharArray());
            node.setGenericKey(genericKey);
            return genericKey;
        }
        return sum;
        
        /* ******************** END OF FUNCTION *********************************** */
    }

	/** calculateLeafKey: Calculate key code for leaf node.*/
    public static int calculateLeafKey(char[] arr) {
    	/* ******************** FUNCTION VARIABLES ******************************** */
    	int 			sum;

    	/* ******************** VARIABLE INITIALIZERS ***************************** */
    	sum 			= 0;
    	
    	/* ******************** FUNCTION BODY ************************************* */  	
    	for( int i = 0; i < arr.length; i++) 
            sum 									+= (i+1) * Character.getNumericValue(arr[i]);
        return sum;
        
        /* ******************** END OF FUNCTION *********************************** */
    }
    
    /** initNodeDuplicates: Set duplicates for each tree.*/
    public static void initNodeDuplicates(MCMCTreeNode node, int numDuplicates) {
    	/* ******************** FUNCTION BODY ************************************* */
    	node.setNumDuplicates(numDuplicates);
    	for(int i = 0; i < node.getNumChildren(); i++)
    		initNodeDuplicates(node.getChild(i), numDuplicates);
    	
    	/* ******************** END OF FUNCTION *********************************** */
    }

    /**
     * initializeConsensusTree: Central to creation of a consensus tree. Will gather and update
     * information for each node and then proceed to construct a consensus tree. Returns root
     * node for the finished consensus tree.
     */
	public static MCMCTreeNode initializeConsensusTree(ArrayList<MCMCTree> treeArray, int numTrees){
		/* ******************** FUNCTION VARIABLES ******************************** */
		MCMCTree 									tempTree;
		ArrayList<MCMCTreeNode> 					nodeArray;
		MCMCTreeNode 								root;
		TreeMap<Integer, MCMCTreeNode> 				nodeMap;
		TreeMap<Integer, TreeMap<Integer, Integer>> childMap;
		MCMCTreeNode 								consRoot;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		nodeArray 									= new ArrayList<MCMCTreeNode>();
		
		/* ******************** FUNCTION BODY ************************************* */
		for(int i = 0; i < treeArray.size(); i++) {
			tempTree 								= treeArray.get(i);
			tempTree.setRoot(MCMCFileReader.treeArrayToTreeNode(tempTree.getData()));
			addGenericKeyCodes(tempTree.getRoot());
			initNodeDuplicates(tempTree.getRoot(), tempTree.getNumDuplicates());
			nodeArray.add(treeArray.get(i).getRoot());
		}
		root 										= nodeArray.get(0);
		nodeMap 									= new TreeMap<Integer, MCMCTreeNode>();
		childMap 									= new TreeMap<Integer, TreeMap<Integer, Integer>>();
		consRoot 									= new MCMCTreeNode();
		
		consRoot.setNumDuplicates(numTrees);
		consRoot.setGenericKey(root.getGenericKey());
		gatherNodeData(nodeArray, nodeMap, childMap);
		gatherAllMajorityNodes(nodeMap, childMap, numTrees);
		buildConsensusTree(root, consRoot, 0, nodeMap, childMap, numTrees);

		return consRoot;
		
		/* ******************** END OF FUNCTION *********************************** */
	}

	/** gatherNodeData: Collects data on a series of tree nodes.*/
	public static void gatherNodeData(ArrayList<MCMCTreeNode> nodeArray, TreeMap<Integer, MCMCTreeNode> nodeMap, TreeMap<Integer, TreeMap<Integer, Integer>> childMap) {
		/* ******************** FUNCTION BODY ************************************* */
		for(int i = 0; i < nodeArray.size(); i++)
			gatherNodeData(nodeArray.get(i), nodeMap, childMap);
		
		/* ******************** END OF FUNCTION *********************************** */
	}

	/** extractMajorityNodes: Extracts majority nodes.*/
	public static void extractMajorityNodes(int parentNode, int childNode,TreeMap<Integer, MCMCTreeNode> nodeMap, TreeMap<Integer, TreeMap<Integer, Integer>> childMap, int numTrees) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		int 					child;
		Object[] 				childNodes;

		/* ******************** VARIABLE INITIALIZERS ***************************** */
		child 					= 0;
		childNodes 				= childMap.get(childNode).keySet().toArray();
		
		/* ******************** FUNCTION BODY ************************************* */
		for(int i = 0 ; i < childNodes.length; i++){
			child = (Integer)childNodes[i];
			if(childMap.get(childNode).containsKey(child)){
				if( isMajorityNode(child, numTrees, nodeMap))
					childMap.get(parentNode).put(child, child );
				else
					extractMajorityNodes(parentNode, child, nodeMap, childMap, numTrees);
			}
		}
		
		/* ******************** END OF FUNCTION *********************************** */
	}

	/** Print childMap */
	public static void printChildMap(TreeMap<Integer, MCMCTreeNode> nodeMap, TreeMap<Integer, TreeMap<Integer, Integer>> childMap) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		Iterator<Entry<Integer, TreeMap<Integer, Integer>>> iter;
		ArrayList<Object> 									arr;
		Iterator<Entry<Integer, MCMCTreeNode>> 				iter2;

		/* ******************** VARIABLE INITIALIZERS ***************************** */
		iter 												= childMap.entrySet().iterator();
		iter2 												= nodeMap.entrySet().iterator();
		arr 												= new ArrayList<Object>();
		
		/* ******************** FUNCTION BODY ************************************* */
		while(iter.hasNext())
			arr.add(iter.next());
		System.out.println(arr);
		System.out.println();

		while(iter2.hasNext())
			System.out.print("key map: " + iter2.next() + "\t");
		System.out.println();
		
		/* ******************** END OF FUNCTION *********************************** */
	}
	
	/* **************************************************************************** *
	 * 							END OF CLASS										*
	 * **************************************************************************** */
}