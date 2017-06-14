package se.cbb.jprime.apps.phylotools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import se.cbb.jprime.io.NewickIOException;
import se.cbb.jprime.io.NewickVertex;
import se.cbb.jprime.io.PrIMENewickTree;
import se.cbb.jprime.io.PrIMENewickTreeReader;

/**
 * First script used for 
 * (i) Testing whether I can code in JPrIME, and 
 * (ii) reading homology event in true reconciliation file generated 
 * by prime_generateTree of PrIME C++ version
 * PS. This code needs refactoring and clean-up. To be done soon.
 * 
 * @author Ikram Ullah
 */

public class GeneTreeHomologyReader{
	public static void main(String[] args)  throws IOException, NewickIOException {
		if(args.length != 1){
			System.err.println("Usage: java -classpath jprime.jar se.cbb.jprime.apps.phylotools.GeneTreeHomologyReader gene_tree_name");
			System.out.println("Exiting now...");
			System.exit(1);
		}
		System.out.println("Input true reconciliation file is " + args[0]);								
		String treepath = parseTreeFromReconciliationFile(args[0]);
		File gFile = new File(treepath);
		BufferedWriter writer = new BufferedWriter(new FileWriter(args[0]+".original"));
		
		PrIMENewickTree sRaw = PrIMENewickTreeReader.readTree(gFile, false, true);
		//System.out.println("Tree is " + sRaw.toString());
		//System.out.println(sRaw.toString());		
		System.out.println("Extracting true reconciliation events...");
		System.out.println("The tree is : " + sRaw);
		List<NewickVertex> vertices = sRaw.getVerticesAsList();
		int[] dupStatus = sRaw.getVertexDuplicationValues();
		for (NewickVertex v : vertices) {
			int id = v.getNumber();
			if(dupStatus[id] != Integer.MAX_VALUE){
				ArrayList<NewickVertex> children = v.getChildren();
				//String lchild = getLeafIds(children.get(0));
				//String rchild = getLeafIds(children.get(1));
				String lchild = getLeafNames(children.get(0));
				String rchild = getLeafNames(children.get(1));
				//System.out.println("The childern ids are " + lchild + " and " + rchild);
				//System.out.println("The node number " + id + " has dupStatus = " + dupStatus[id]);
				writer.write("["+lchild+", "+rchild+"]"+"\t"+dupStatus[id]+"\n");
				System.out.println("["+lchild+", "+rchild+"]"+"\t"+dupStatus[id]);
			}
		}
		writer.flush();
		writer.close();
		gFile.deleteOnExit();
		System.out.println("Done...");
		System.out.println("True values has been written to " + args[0]+".original");
	}
	
	private static String parseTreeFromReconciliationFile(String fileName) {
		try {
			String gfileName = "treeFromReconFile.tree";
			File f = new File(gfileName);
			if(f.exists())
			    f.delete();
			BufferedReader buf = new BufferedReader(new FileReader(fileName));
			BufferedWriter bw = new BufferedWriter(new FileWriter(gfileName));		
			String line = "";
			while((line = buf.readLine()) != null){
				line = line.trim();
				if(line.charAt(0) == '#')
					continue;
				else {
	//				StringTokenizer stk = new StringTokenizer(line);
	//				String tree = "";
	//				
	//				// true reconciliations is the last token
	//				while(stk.hasMoreTokens())
	//					tree = stk.nextToken();
					String[] token = line.split(";");
					
					String trueFile = "";
					if(token.length == 1)
						trueFile = token[0].trim();
					else
						trueFile = token[4].trim();
					bw.write(trueFile);
					bw.flush();
					bw.close();
					buf.close();
					
					return gfileName;
				}
			}
			bw.flush();
			bw.close();
			buf.close();
		}catch(Exception ex){
			System.err.println("Error in reading reconciliation file");
			System.err.println("Reason: " + ex.getMessage());
		}
		return null;
	}

	public static String getLeafNames(NewickVertex vertex){		 
		 String lNames = getLeafNamesRecursive(vertex);
		return lNames;
	}

	private static String getLeafNamesRecursive(NewickVertex vertex) {
		if(vertex.isLeaf())
			return vertex.getName();
		else {
			ArrayList<NewickVertex> ch = vertex.getChildren();
			return getLeafNamesRecursive(ch.get(0)) + " " + getLeafNamesRecursive(ch.get(1));
		}
	}
	
	public static String getLeafIds(NewickVertex vertex){
		String lNames = "";
		lNames += getLeafIdsRecursive(vertex);
		return lNames + "";
	}

	private static String getLeafIdsRecursive(NewickVertex vertex) {
		if(vertex.isLeaf())
			return vertex.getNumber()+"";
		else {
			ArrayList<NewickVertex> ch = vertex.getChildren();
			return getLeafIdsRecursive(ch.get(0)) + " " + getLeafIdsRecursive(ch.get(1));
		}
	}
}
