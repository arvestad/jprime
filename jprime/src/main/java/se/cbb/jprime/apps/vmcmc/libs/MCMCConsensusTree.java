package se.cbb.jprime.apps.vmcmc.libs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 *
 * MCMCConsensusTree: Static helper functions for creation of consensus trees.
 *	Created by: M Bark & J Mir� Arredondo (2010)
 *   E-mail: mikbar at kth dot se & jorgma at kth dot se
 *
 *   This file is part of the bachelor thesis "Verktyg f�r visualisering av MCMC-data" - VMCMC
 *	Royal Institute of Technology, Sweden
 * 
 *	File version: 1.0
 *	VMCMC version: 1.0
 *
 *	Modification history for this file:
 *	v1.0  (2010-06-15) First released version.
 */
abstract public class MCMCConsensusTree{
	/*
	 * addGenericKeyCodes: Calculate and store key codes recursively. 
	 */
	public static int addGenericKeyCodes(MCMCTreeNode node) {

        int sum = 0;
        if(node.getNumChildren() > 0) {
            for(int i = 0; i < node.getNumChildren(); i++){
                sum += addGenericKeyCodes(node.getChildren().get(i));
            }
            node.setGenericKey(sum);
        }
        else{
            int genericKey = calculateLeafKey(node.getName().toCharArray());
            node.setGenericKey(genericKey);
            return genericKey;
        }
        return sum;
    }

	/*
	 * calculateLeafKey: Calculate key code for leaf node.
	 */
    public static int calculateLeafKey(char[] arr) {
        int sum = 0;
        for( int i = 0; i < arr.length; i++) {
            sum += (i+1)*Character.getNumericValue(arr[i]);
        }

        return sum;
    }
    
    /*
     * initNodeDuplicates: Set duplicates for each tree.
     */
    public static void initNodeDuplicates(MCMCTreeNode node, int numDuplicates) {
    	node.setNumDuplicates(numDuplicates);
    	for(int i=0; i<node.getNumChildren(); i++)
    		initNodeDuplicates(node.getChild(i), numDuplicates);
    }

    /*
     * initializeConsensusTree: Central to creation of a consensus tree. Will gather and update
     * information for each node and then proceed to construct a consensus tree. Returns root
     * node for the finished consensus tree.
     */
	public static MCMCTreeNode initializeConsensusTree(ArrayList<MCMCTree> treeArray, int numTrees){
		ArrayList<MCMCTreeNode> nodeArray = new ArrayList<MCMCTreeNode>();
		
		for(int i=0; i<treeArray.size(); i++) {
			MCMCTree tempTree = treeArray.get(i);
			tempTree.setRoot(MCMCFileReader.treeArrayToTreeNode(tempTree.getData()));
			addGenericKeyCodes(tempTree.getRoot());
			initNodeDuplicates(tempTree.getRoot(), tempTree.getNumDuplicates());
			
			nodeArray.add(treeArray.get(i).getRoot());
		}
		
		MCMCTreeNode root = nodeArray.get(0);
		
		TreeMap<Integer, MCMCTreeNode> nodeMap = new TreeMap<Integer, MCMCTreeNode>();
		TreeMap<Integer, TreeMap<Integer, Integer>> childMap = new TreeMap<Integer, TreeMap<Integer, Integer>>();
		
		MCMCTreeNode consRoot = new MCMCTreeNode();
		consRoot.setNumDuplicates(numTrees);
		consRoot.setGenericKey(root.getGenericKey());
		gatherNodeData(nodeArray, nodeMap, childMap);
		gatherAllMajorityNodes(nodeMap, childMap, numTrees);
		buildConsensusTree(root, consRoot, 0, nodeMap, childMap, numTrees);

		return consRoot;
	}

	/*
	 * gatherNodeData: Collects data on a series of tree nodes.
	 */
	public static void gatherNodeData(ArrayList<MCMCTreeNode> nodeArray, TreeMap<Integer, MCMCTreeNode> nodeMap, TreeMap<Integer, TreeMap<Integer, Integer>> childMap) {
		for(int i = 0; i < nodeArray.size(); i++) {
			gatherNodeData(nodeArray.get(i), nodeMap, childMap);
		}
	}

	/*
	 * gatherNodeData: Collects data in form of keys and relatives. Recursive.
	 */
	private static void gatherNodeData(MCMCTreeNode node, TreeMap<Integer, MCMCTreeNode> nodeMap, TreeMap<Integer, TreeMap<Integer, Integer>> childMap ) {

		int genericKey = node.getGenericKey();
		int numDuplicates = node.getNumDuplicates();
		String nodeName = node.getName();

		if(nodeMap.containsKey(genericKey)) {
			MCMCTreeNode tempNode = nodeMap.get(genericKey);
			tempNode.setNumDuplicates(tempNode.getNumDuplicates() + numDuplicates);
			nodeMap.put(genericKey, tempNode);
		}
		else {
			MCMCTreeNode newConsensusNode =  new MCMCTreeNode();
			newConsensusNode.setGenericKey(genericKey);
			newConsensusNode.setName(nodeName);
			newConsensusNode.setNumDuplicates(numDuplicates);
			nodeMap.put(genericKey, newConsensusNode);
		}

		if(node.getNumChildren() > 0)
			for(int i=0; i<node.getNumChildren(); i++) {
				int childGenericKey = node.getChildren().get(i).getGenericKey();

				if(childMap.containsKey(genericKey)) {
					TreeMap<Integer, Integer> nodeEntry = childMap.get(genericKey);

					if(!nodeEntry.containsKey(childGenericKey)) {
						nodeEntry.put(childGenericKey, childGenericKey);
						childMap.put(genericKey, nodeEntry);
					}
				}
				else{
					TreeMap<Integer, Integer> newEntry = new TreeMap<Integer, Integer>();
					newEntry.put(childGenericKey, childGenericKey);
					childMap.put(genericKey, newEntry);
				}
			}
		else
			childMap.put(genericKey, new TreeMap<Integer, Integer>());

		for(int i = 0; i < node.getChildren().size(); i++) {
			gatherNodeData(node.getChildren().get(i), nodeMap, childMap);
		}
	}

	/*
	 * gatherAllMajorityNodes: Gathers all majority nodes.
	 */
	private static void gatherAllMajorityNodes(TreeMap<Integer, MCMCTreeNode> nodeMap, TreeMap<Integer, TreeMap<Integer, Integer>> childMap, int numTrees) {		

		if(nodeMap != null){   
			Object[] values = nodeMap.keySet().toArray();
			int majorityNode = 0;
			for(int i = 0; i < values.length; i++) {
				majorityNode = (Integer)values[i];

				if(isMajorityNode(majorityNode, numTrees, nodeMap))
					decideMajorityNodes(majorityNode, nodeMap, childMap, numTrees);
			}
		}	
	}

	/*
	 * isMajorityNode: Returns true if genericKey matches majority node. False otherwise.
	 */
	private static boolean isMajorityNode(int genericKey, int numTrees, TreeMap<Integer, MCMCTreeNode> nodeMap) {
		if(nodeMap.containsKey(genericKey)){
			if((double)nodeMap.get(genericKey).getNumDuplicates()/numTrees > 0.5)
				return true;
		}
		return false;
	}

	/*
	 *  decideMajorityNodes: Decides whether a nodes grandchildren have to be 
	 *  extracted to their grandparent.
	 */
	private static void decideMajorityNodes(int parentNode, TreeMap<Integer, MCMCTreeNode> nodeMap, TreeMap<Integer, TreeMap<Integer, Integer>> childMap, int numTrees) {

		int child = 0;
		Object[] childNodes = childMap.get(parentNode).keySet().toArray();

		for(int i = 0 ; i < childNodes.length; i++){
			child = (Integer)childNodes[i];
			if(!isMajorityNode(child, numTrees, nodeMap)){
				extractMajorityNodes(parentNode, child,nodeMap, childMap, numTrees);
				childMap.get(parentNode).remove(child);
			}
		}
	}

	/*
	 * extractMajorityNodes: Extracts majority nodes.
	 */
	public static void extractMajorityNodes(int parentNode, int childNode,TreeMap<Integer, MCMCTreeNode> nodeMap, TreeMap<Integer, TreeMap<Integer, Integer>> childMap, int numTrees) {

		int child  = 0;
		Object[] childNodes = childMap.get(childNode).keySet().toArray();
		
		for(int i = 0 ; i < childNodes.length; i++){
			child = (Integer)childNodes[i];
			if(childMap.get(childNode).containsKey(child)){
				if( isMajorityNode(child, numTrees, nodeMap))
					childMap.get(parentNode).put(child, child );
				else
					extractMajorityNodes(parentNode, child, nodeMap, childMap, numTrees);
			}
		}
	}

	/*
	 * FindMajorityNodes: Find and return majority nodes as list.
	 */
	private static ArrayList<Integer> findMajorityNodes(TreeMap<Integer, Integer> childMap, TreeMap<Integer, MCMCTreeNode> nodes,  int numTrees) {
		ArrayList<Integer> majorityKeys = new ArrayList<Integer>();
			int child = 0;
			Object[] values = childMap.values().toArray();

			for(int i = 0; i < values.length; i++) {
				child = (Integer)values[i];
				if(isMajorityNode(child, numTrees, nodes))
					majorityKeys.add(child);
			}		
		Collections.sort(majorityKeys);
		Collections.reverse(majorityKeys);
		return majorityKeys;       
	}

	/*
	 * buildConsensusTree: Construct consensus tree.
	 */
	private static void buildConsensusTree(MCMCTreeNode node, MCMCTreeNode root, int level, TreeMap<Integer, MCMCTreeNode> nodeMap, TreeMap<Integer, TreeMap<Integer, Integer>> childMap, int numTrees) {

		ArrayList<Integer> keyNodes = findMajorityNodes(childMap.get(node.getGenericKey()), nodeMap, numTrees);
		for(int i = 0; i < keyNodes.size(); i++){

			MCMCTreeNode childNode = new MCMCTreeNode();
			MCMCTreeNode consChild;

			int childKey = keyNodes.get(i);

			childNode.setGenericKey(childKey);
			consChild = nodeMap.get(childKey);

			if(nodeMap.containsKey(childNode.getGenericKey())){   
				if(isMajorityNode(childNode.getGenericKey(),numTrees, nodeMap))
				{
					childNode.setLevel(level+1);
					consChild.setLevel(level+1);
					root.addChild(consChild);
					nodeMap.remove(childNode.getGenericKey());
				}
			}
			buildConsensusTree(childNode , consChild, level+1, nodeMap, childMap, numTrees);
		}
	}

	/*
	 * Print childMap
	 */
	public static void printChildMap(TreeMap<Integer, MCMCTreeNode> nodeMap, TreeMap<Integer, TreeMap<Integer, Integer>> childMap) {

		Iterator<Entry<Integer, TreeMap<Integer, Integer>>> iter = childMap.entrySet().iterator();
		ArrayList<Object> arr = new ArrayList<Object>();
		while(iter.hasNext())
			arr.add(iter.next());
		System.out.println(arr);
		System.out.println();

		Iterator<Entry<Integer, MCMCTreeNode>> iter2 = nodeMap.entrySet().iterator();
		while(iter2.hasNext())
			System.out.print("key map: " + iter2.next() + "\t");
		System.out.println();

	}

}