package se.cbb.jprime.apps.vmcmc.libs;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
enum SerieType {
	FLOAT,
	TREE,
	OTHER
}

/**
 *
 * SerieType: Enumeration of all types of parameters handled by datacontainer.
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
public class MCMCDataContainer {
	String fileName;	//Name of file
	
	ArrayList<SerieType> listSerietype = new ArrayList<SerieType>();	//List of parameter types
	
	int numSeries;	//Total number of series
	int numTreeSeries; 	//Number of tree series
	int numValueSeries;	//Number of numerical series
	
	int numLines;	//Number of lines in file
	
	ArrayList<List<Double>> listValueSeries = new ArrayList<List<Double>>();
	ArrayList<String> listValueNames = new ArrayList<String>();		//List of names for numerical series
	ArrayList<Integer> listNumValues = new ArrayList<Integer>();
	
	ArrayList<ArrayList<MCMCTree>> listTreeSeries = new ArrayList<ArrayList<MCMCTree>>();
	ArrayList<String> listTreeNames = new ArrayList<String>();		//List of names for tree series
	ArrayList<Integer> listNumTrees = new ArrayList<Integer>();

	public void addSerieType(SerieType type) {
		listSerietype.add(type);
		numSeries++;
	}
	
	public void addNewTreeSerie() {
		listTreeSeries.add(new ArrayList<MCMCTree>());
		listNumTrees.add(0);
		numTreeSeries++;
	}
	
	public void addTreeSerie(ArrayList<MCMCTree> listTrees) {
		listTreeSeries.add(listTrees);
		numTreeSeries++;
	}
	
	public void addTreeToSerie(MCMCTree tree, int index) {
		listTreeSeries.get(index).add(tree);
		listNumTrees.set(index, listNumTrees.get(index)+1);
	}
	
	public void addTreeName(String name) {
		listTreeNames.add(name);
	}
	
	public void addNewValueSerie() {
		listValueSeries.add(new ArrayList<Double>());
		listNumValues.add(0);
		numValueSeries++;
	}
	
	public void addValueSerie(List<Double> listValues) {
		listValueSeries.add(listValues);
		numValueSeries++;
	}
	
	public void addValueToSerie(Double value, int index) {
		listValueSeries.get(index).add(value);
		listNumValues.set(index, listNumValues.get(index)+1);
	}
	
	public void addValueName(String name) {
		listValueNames.add(name);
	}
	
	/*
	 * getSubDataContainer: Returns part of datacontainer. 
	 * WARNING: Incomplete
	 */
	public MCMCDataContainer getSubDataContainer(int first, int last) {
		MCMCDataContainer datacontainer = new MCMCDataContainer();
		
		if(first < 0 || last > numLines || last < first)
			return null;
		
		int length = last-first;
		
		datacontainer.setFileName(this.fileName);
		datacontainer.setSerieTypes(this.listSerietype);
		datacontainer.setNumLines(length);
		
		for(int i=0; i<numValueSeries; i++) {
			datacontainer.addValueName(listValueNames.get(i));
			datacontainer.addValueSerie(listValueSeries.get(i).subList(first, last));
		}
		
		return datacontainer;
	}
	
	public int getNumLines() {return numLines;}
	
	public String getFileName() {return fileName;}
	
	public ArrayList<SerieType> getSerieTypes() {return listSerietype;}
	public int getNumSeries() {return numSeries;}
	
	public ArrayList<List<Double>> getValueSeries() {return listValueSeries;}
	public List<Double> getValueSerie(int index) {return listValueSeries.get(index);}
	public int getNumValues(int index) {return listNumValues.get(index);}
	public int getNumValueSeries() {return numValueSeries;}
	public ArrayList<String> getValueNames() {return listValueNames;}
	
	public ArrayList<ArrayList<MCMCTree>> getTreeSeries() {return listTreeSeries;}
	public ArrayList<MCMCTree> getTreeSerie(int index) {return listTreeSeries.get(index);}
	public int getNumTrees(int index) {return listNumTrees.get(index);}
	public int getNumTreeSeries() {return numTreeSeries;}
	public ArrayList<String> getTreeNames() {return listTreeNames;}
	
	public void setNumLines(int numLines) {this.numLines = numLines;}
	public void setFileName(String fileName) {this.fileName = fileName;}
	public void setSerieTypes(ArrayList<SerieType> listSerietype) {this.listSerietype = listSerietype;}
	
	public void setNumSeries(int numSeries) {this.numSeries = numSeries;}
	
	public void setNumTreeSeries(int numTreeSeries) {this.numTreeSeries = numTreeSeries;}
	
}
