package se.cbb.jprime.apps.vmcmc.libs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 *
 * MCMCFileReader: Static class responsible for handling files. Interprets files of formats
 * (.mcmc) and stores the results as a MCMCDataContainer. Class containins static methods for 
 * converting byte arrays to appropriate datastructures.
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
abstract public class MCMCFileReader {
	static int p;	//Pointer to current position in file.

	/*
	 * formatCommentLine: Extracts names and types into two strings separated by spaces.
	 */
	private static String[] formatCommentLine(char[] data) {
		String names = "";
		String types = "";

		for(int i=0; i<data.length && data[i] != '\r' && data[i] != '\n'; i++) {
			if(data[i] == '#') {		//Skip comment char
				if(data[i+1] == ' ')	//If followed by space; skip space.
					i++;
				continue;
			}
			if(data[i] == '\t')		//Replace tabs with spaces for conformity
				data[i] = ' ';
			if(data[i] == ';')		//Skip ; chars
				continue;

			if(data[i] != ' ') {	//Space means new parameter
				//Extract name:
				while(data[i] != '(' && data[i] != ' ') {
					names += data[i];
					i++;
				}
				names += ' ';	//Add space between names

				if(data[i] == ' ')	//If there is no type add "none" as type
					types += "none ";
				else {			//Otherwise extract type
					i++;
					while(data[i] != ')') {
						types += data[i];
						i++;
					}
					types += ' ';	//Add space between types
				}
			}
		}

		String[] returnValues = {names, types};
		return returnValues;
	}
	
	/*
	 * formatCommentLine: Extracts names and types into two strings separated by spaces.
	 */
	private static String[] formatCommentLine1(char[] data) {
		String names = "";
		String types = "";

		for(int i=0; i<data.length && data[i] != '\r' && data[i] != '\n'; i++) {
			if(data[i] == '\t')		//Replace tabs with spaces for conformity
				data[i] = ' ';
			if(data[i] == ';')		//Skip ; chars
				continue;

			if(data[i] != ' ') {	//Space means new parameter
				//Extract name:
				while(data[i] != ' ' && data[i] != '\t' && data[i] != '\r') {
					names += data[i];
					i++;
				}
				names += ' ';	//Add space between names
				types += "none ";
			}
		}

		String[] returnValues = {names, types};
		return returnValues;
	}
	
	/*
	 * readNumber: Converts a byte array terminated by \t or ' ' to Double.
	 */
	private static Double readNumber(byte[] data) {
		String number = "";

		while(p < data.length && data[p] != '\t' && data[p] != ' ' && data[p] != '\r' && data[p] != '\n') {
			number += (char) data[p];
			p++;

			if(data[p] == ';')
				p++;
		}
		if(number!= "")
			return Double.parseDouble(number);
		else
			return Double.parseDouble("1000000000");
	}

	/*
	 * readLine: Extract sub array terminated by \n from byte array. 
	 */
	private static byte[] readLine(byte[] data, int sizeofFile) {
		byte[] line;
		int size = 0;

		while(p < sizeofFile && data[p] != '\n') {
			p++;
			size++;
		}

		line = new byte[size];
		System.arraycopy(data, p-size, line, 0, size);
		return line;
	}

	/*
	 * readTreeStructure: Recursive function that converts byte array to treestructure MCMCTreeNode.
	 */
	private static MCMCTreeNode readTreeStructure(byte[] data, final int level) {
		MCMCTreeNode node = new MCMCTreeNode();
		node.setLevel(level);
		
		//Left parenthesis marks the existence of children to be added
		if(data[p] == '(') {
			p++;	//Skip left parenthesis
			
			node.addChild(readTreeStructure(data, level+1));	//Add one child per default
			
			//Check for more children until right parenthesis is found
			while(data[p] != ')') {
				if(data[p] == ',') {	//Comma means one more child to be added
					p++;	//Skip comma
					if(data[p] == ' ')
							p++;	//Skip space
					
					node.addChild(readTreeStructure(data, level+1));	//Add child
				}
			}
			
			p++;	//Skip right parenthesis
		}
		else {
			String name = "";
			
			//Extract name of node
			while(data[p] != ',' && data[p] != ')') {
				name += (char) data[p];
				p++;	//Skip character
			}
			
			node.setName(name);		//Set the nodes name
		}
		
		//If there is a colon attached after non-leaf node. Ignore it.
		if(p < data.length && data[p] == ':') {
			while(data[p] != ']')
				p++;
			
			p++;
		}
		
		return node;	//Return created node
	}
	
	/*
	 * treeArrayToTreeNode: same functionality as method readTreeStructure(byte[]) but with p=0.
	 */
	public static MCMCTreeNode treeArrayToTreeNode(byte[] data) {
		p=0;
		return readTreeStructure(data, 0);
	}
	
	/*
	 * findArrayKey: Creates and returns an unique key for specified byte array.
	 */
	private static int findArrayKey(byte[] data) {
		int key=0;
		
		for(int i=0; i<data.length; i++)
			key += data[i]*i;
		
		return key;
	}
	
	/*
	 * readTreeArray: Extract and return tree as newick byte array. This array can later
	 * be converted to a treeStructure through the method treeArrayToTreeNode.
	 */
	private static byte[] readTreeArray(byte[] data) { 
		int size=0;

		while(data[p] != ';') {
			p++;
			size++;
		}
		
		byte[] tree = new byte[size];
		System.arraycopy(data, p-size, tree, 0, size);
		
		p++;

		return tree;
	}

	/*
	 * readMCMCFile: Reads file of format mcmc and extracts information to MCMCDataContainer. 
	 */
	public static MCMCDataContainer readMCMCFile(File file) throws IOException
	{
		MCMCDataContainer datacontainer = new MCMCDataContainer();
		
		FileInputStream inputStream = new FileInputStream(file);

		datacontainer.setFileName(file.getName());
		
		int size = (int) file.length();
		
		byte[] data = new byte[size];
		inputStream.read(data, 0, size);	//Read entire file to memory for fast processing
		
		int lineIndex = 0;
		byte[] commentline=null;
		
		for(p=0; p<size; p++) 
		{
			if(data[p] == '#') 
			{
				commentline = readLine(data, size);	//Store comment line
				continue;
			}

			//Reaching this clause means end top portion of comments. Process last comment line which stores parameter names and types
			int numTypes=0;
			if(datacontainer.getNumSeries() == 0) 
			{
				
				String values[] = null;
				//Extract names and types from stored comment line
				if(commentline != null) 
				{
					values = formatCommentLine((new String(commentline).toCharArray()));
				}
				else 
				{
					commentline = readLine(data, size);
					values = formatCommentLine1((new String(commentline).toCharArray()));
				}

				String names = values[0];
				String types = values[1];

				//Names and types are stored in two separate string which needs to be tokenized in order to be used
				StringTokenizer nameTokenizer = new StringTokenizer(names);
				StringTokenizer typeTokenizer = new StringTokenizer(types);

				//Store datacontainer with information about names and types
				while(nameTokenizer.hasMoreTokens() && typeTokenizer.hasMoreTokens()) {
					String type = typeTokenizer.nextToken();
					numTypes++;

					//Tree parameters will be read as newick strings for later handling
					if(type.compareTo("tree") == 0) {
						datacontainer.addTreeName(nameTokenizer.nextToken());
						datacontainer.addSerieType(SerieType.TREE);
						datacontainer.addNewTreeSerie();
					}
					//These three types will all be handles as floats
					else if(type.compareTo("none") == 0 ||
							type.compareTo("float") == 0 || 
							type.compareTo("logfloat") == 0) {

						datacontainer.addValueName(nameTokenizer.nextToken());
						datacontainer.addSerieType(SerieType.FLOAT);
						datacontainer.addNewValueSerie();
					}
					//Other types are ignored
					else {
						nameTokenizer.nextToken();
						datacontainer.addSerieType(SerieType.OTHER);
					}
				}
			}
			
			
			int valueindex=0;
			int treeindex=0;
			int count = 0;

			//For each row. Iterate all parameters and read value according to type
			for(int i=0; i<datacontainer.getNumSeries(); i++) {
				switch(datacontainer.getSerieTypes().get(i)) {
				case FLOAT:
					Double number = readNumber(data);
					
					
					if(number!=(double)(1000000000))
					{
						datacontainer.addValueToSerie(number, valueindex);
						valueindex++;
					}
					else if(count <= 1)
					{
						i = i-1;
						count++;
					}
					break;
				case TREE:
					byte[] treeArray = readTreeArray(data);
					
					int key = findArrayKey(treeArray);
					
					MCMCTree tree = new MCMCTree();
					tree.setKey(key);
					tree.setData(treeArray);
					tree.addDuplicate();
					tree.addIndex(lineIndex);
					datacontainer.addTreeToSerie(tree, treeindex);
					
					treeindex++;
					
					break;
				//If type does not match any of the enumerations above. Skip column
				default:
					while(data[p] != '\t' && data[p] != ' ') {
						p++;	//Skip character
					}
					break;
				}
				p++;
			}
			lineIndex++;	//Index of current line handled
		}
		
		datacontainer.setNumLines(lineIndex);	//Lineindex will equal total amount of lines at end of reading

		return datacontainer;
	}
}
