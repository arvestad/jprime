package se.cbb.jprime.apps.vmcmc.libs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * MCMCFileReader: Static class responsible for handling files. Interprets files of formats
 * (.mcmc) and stores the results as a MCMCDataContainer. Class containins static methods for 
 * converting byte arrays to appropriate datastructures.
 */
abstract public class MCMCFileReader {
	/* **************************************************************************** *
	 * 							CLASS VARIABLES										*
	 * **************************************************************************** */
	static int p;	//Pointer to current position in file.
	static String program;

	/* **************************************************************************** *
	 * 							CLASS PRIVATE FUNCTIONS								*
	 * **************************************************************************** */
	/** formatCommentLine: Extracts names and types into two strings separated by spaces.*/
	private static String[] formatCommentLine(char[] data) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		String 			names;
		String 			types;

		/* ******************** VARIABLE INITIALIZERS ***************************** */
		names 			= "";
		types 			= "";
		
		/* ******************** FUNCTION BODY ************************************* */
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
		/* ******************** END OF FUNCTION *********************************** */
	}
	
	/** formatCommentLine: Extracts names and types into two strings separated by spaces.*/
	private static String[] formatCommentLine1(char[] data, char[] firstLine) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		String 			names;
		String 			types;
		String 			dataEntry;

		/* ******************** VARIABLE INITIALIZERS ***************************** */
		names 			= "";
		types 			= "";
		
		/* ******************** FUNCTION BODY ************************************* */
		for(int i=0; i<data.length && data[i] != '\r' && data[i] != '\n'; i++) {
			if(data[i] == '\t')		//Replace tabs with spaces for conformity
				data[i] 		= ' ';
			if(data[i] == ';')		//Skip ; chars
				continue;
			if(data[i] != ' ') {	//Space means new parameter
				//Extract name:
				while(data[i] != ' ' && data[i] != '\t' && data[i] != '\r' && i < data.length-1) {
					names += data[i];
					i++;
				}
				names += ' ';	//Add space between names
			}
		}
		for(int i=0; i<firstLine.length && firstLine[i] != '\r' && firstLine[i] != '\n'; i++) {
			dataEntry 				= "";
			if(firstLine[i] == '\t')		//Replace tabs with spaces for conformity
				firstLine[i] 		= ' ';
			if(firstLine[i] == ';')		//Skip ; chars
				continue;
			if(firstLine[i] != ' ') {	//Space means new parameter
				//Extract data entry:
				while(firstLine[i] != ' ' && firstLine[i] != '\t' && firstLine[i] != '\r' && i < firstLine.length-1) {
					dataEntry += firstLine[i];
					i++;
				}
				try{
					Double.parseDouble(dataEntry);
					types += "float ";
				} catch(Exception e) {
					types += "tree ";
				}
			}
		}
		String[] returnValues = {names, types};
		return returnValues;
		/* ******************** END OF FUNCTION *********************************** */
	}
	
	/**
	 * readNumber: Converts a byte array terminated by \t or ' ' to Double.
	 */
	private static Double readNumber(byte[] data) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		String 				number;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		number 				= "";
		
		/* ******************** FUNCTION BODY ************************************* */
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
		/* ******************** END OF FUNCTION *********************************** */
	}

	/**
	 * readLine: Extract sub array terminated by \n from byte array. 
	 */
	private static byte[] readLine(byte[] data, int sizeofFile) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		byte[] 				line;
		int 				size;
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		size 				= 0;
		
		/* ******************** FUNCTION BODY ************************************* */
		while(p < sizeofFile && data[p] != '\n') {
			p++;
			size++;
		}

		line = new byte[size];
		System.arraycopy(data, p-size, line, 0, size);
		return line;
		/* ******************** END OF FUNCTION *********************************** */
	}

	/**
	 * readTreeStructure: Recursive function that converts byte array to treestructure MCMCTreeNode.
	 */
	private static MCMCTreeNode readTreeStructure(byte[] data, final int level) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		MCMCTreeNode 		node;
		String 				name;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		node 				= new MCMCTreeNode();
		name 				= "";
		
		/* ******************** FUNCTION BODY ************************************* */
		node.setLevel(level);
		if(data[p] == '(') {											//Left parenthesis marks the existence of children to be added
			p++;														//Skip left parenthesis
			node.addChild(readTreeStructure(data, level+1));			//Add one child per default
			
			while(data[p] != ')') {										//Check for more children until right parenthesis is found
				if(data[p] == ',') {									//Comma means one more child to be added
					p++;												//Skip comma
					if(data[p] == ' ')
						p++;											//Skip space
					node.addChild(readTreeStructure(data, level+1));	//Add child
				}
			}	
			p++;														//Skip right parenthesis
		} else if(data[p]!= ':'){
			while(data[p] != ',' && data[p] != ')') {					//Extract name of node
				name += (char) data[p];
				p++;													//Skip character
			}
			node.setName(name);											//Set the nodes name
		}
		
		if(p < data.length && data[p] == ':') {							//If there is a colon attached after non-leaf node. Ignore it.
			if(!program.equals("JPrIME")){
				while(data[p] != ']')
					p++;
				p++;
			} else {
				while(p < data.length && data[p] != ',' && data[p] != ')') {
					p++;
				}
			}
		}
		return node;													//Return created node
		/* ******************** END OF FUNCTION *********************************** */
	}
	
	/**
	 * findArrayKey: Creates and returns an unique key for specified byte array.
	 */
	private static int findArrayKey(byte[] data) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		int 		key;
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		key			= 0;	
		/* ******************** FUNCTION BODY ************************************* */
		for(int i = 0; i < data.length; i++)
			key += data[i]*i;
		return key;
		/* ******************** END OF FUNCTION *********************************** */
	}
	
	/**
	 * readTreeArray: Extract and return tree as newick byte array. This array can later
	 * be converted to a treeStructure through the method treeArrayToTreeNode.
	 */
	private static byte[] readTreeArray(byte[] data) { 
		/* ******************** FUNCTION VARIABLES ******************************** */
		int 		size;
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		size		= 0;
		
		/* ******************** FUNCTION BODY ************************************* */
		while(data[p] != ';') {
			p++;
			size++;
		}
		byte[] tree = new byte[size];
		System.arraycopy(data, p-size, tree, 0, size);
		p++;
		return tree;
		/* ******************** END OF FUNCTION *********************************** */
	}
	
	/* **************************************************************************** *
	 * 							CLASS PUBLIC FUNCTIONS								*
	 * **************************************************************************** */	
	/**
	 * treeArrayToTreeNode: same functionality as method readTreeStructure(byte[]) but with p=0.
	 */
	public static MCMCTreeNode treeArrayToTreeNode(byte[] data) {
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		p = 0;
		/* ******************** FUNCTION BODY ************************************* */
		return readTreeStructure(data, 0);
		/* ******************** END OF FUNCTION *********************************** */
	}

	/**
	 * readMCMCFile: Reads file of format mcmc and extracts information to MCMCDataContainer. 
	 */
	public static MCMCDataContainer readMCMCFile(File file) throws IOException {
		/* ******************** FUNCTION VARIABLES ******************************** */
		MCMCDataContainer 		datacontainer;
		FileInputStream 		inputStream;
		int 					size;
		int 					lineIndex;
		int 					numTypes;
		int 					valueindex;
		int 					treeindex;
		int 					count;
		int 					key;
		int						colIterInData;
		byte[] 					commentline;
		String 					values[];
		String 					names;
		String					name;
		String 					types;
		String 					type;
		StringTokenizer 		nameTokenizer;
		StringTokenizer 		typeTokenizer;
		Double 					number;
		byte[] 					treeArray;
		byte[] 					data;
		MCMCTree 				tree;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		datacontainer 			= new MCMCDataContainer();
		inputStream 			= new FileInputStream(file);
		size 					= (int) file.length();
		commentline				= null;
		lineIndex	 			= 0;
		count 					= 0;
		colIterInData			= 0;
		data 					= new byte[size];
		
		/* ******************** FUNCTION BODY ************************************* */
		datacontainer.setFileName(file.getName());
		inputStream.read(data, 0, size);			//Read entire file to memory for fast processing
		
		for(p=0; p<size; p++) {
			if(data[p] == '#') {
				commentline = readLine(data, size);	//Store comment line
				continue;
			}

			//Reaching this clause means end top portion of comments. Process last comment line which stores parameter names and types
			numTypes = 0;
			if(datacontainer.getNumSeries() == 0) {
				values = null;
				if(commentline != null) {			//Extract names and types from stored comment line
					program = "OTHER"; 
					values = formatCommentLine(new String(commentline).toCharArray());
				} else {
					commentline = readLine(data, size);
					p++;
					byte[] firstDataLine = readLine(data, size);
					p = p-firstDataLine.length-1;
					program = "JPrIME";
					values = formatCommentLine1(new String(commentline).toCharArray(), new String(firstDataLine).toCharArray());
				}
				names = values[0];
				types = values[1];

				//Names and types are stored in two separate string which needs to be tokenized in order to be used
				nameTokenizer = new StringTokenizer(names);
				typeTokenizer = new StringTokenizer(types);

				//Store datacontainer with information about names and types
				while(nameTokenizer.hasMoreTokens() && typeTokenizer.hasMoreTokens()) {
					type = typeTokenizer.nextToken();
					name = nameTokenizer.nextToken();
					if(name.equalsIgnoreCase("iteration") || name.equalsIgnoreCase("n"))
						colIterInData = numTypes;
					numTypes++;

					//Tree parameters will be read as newick strings for later handling
					if(type.compareTo("tree") == 0) {
						datacontainer.addTreeName(name);
						datacontainer.addSerieType(SerieType.TREE);
						datacontainer.addNewTreeSerie();
					} else if(type.compareTo("none") == 0 || type.compareTo("float") == 0 || type.compareTo("logfloat") == 0) {
						datacontainer.addValueName(name);
						datacontainer.addSerieType(SerieType.FLOAT);
						datacontainer.addNewValueSerie();
					} else {							//Other types are ignored
						datacontainer.addSerieType(SerieType.OTHER);
					}
				}
			}
			
			valueindex = 0;
			treeindex = 0;
			for(int i=0; i<datacontainer.getNumSeries(); i++) {	//For each row. Iterate all parameters and read value according to type
				switch(datacontainer.getSerieTypes().get(i)) {
				case FLOAT:
					number = readNumber(data);
					if(number!=(double)(1000000000)) {
						datacontainer.addValueToSerie(number, valueindex);
						valueindex++;
					} else if(count <= 1) {
						i--;
						count++;
					}
					break;

				case TREE:
					treeArray = readTreeArray(data);
					key = findArrayKey(treeArray);
					tree = new MCMCTree();
					tree.setKey(key);
					tree.setData(treeArray);
					tree.addDuplicate();
					tree.addIndex(lineIndex);
					datacontainer.addTreeToSerie(tree, treeindex);
					treeindex++;
					break;

				default: 			//If type does not match any of the enumerations above. Skip column
					while(data[p] != '\t' && data[p] != ' ')
						p++;	//Skip character
					break;
				}
				p++;
			}
			lineIndex++;	//Index of current line handled
		}
		
		datacontainer.removeSerie(colIterInData);
		datacontainer.setNumLines(lineIndex);	//Lineindex will equal total amount of lines at end of reading
		return datacontainer;
		/* ******************** END OF FUNCTION *********************************** */
	}
}
