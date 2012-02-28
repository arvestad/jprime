package se.cbb.jprime.apps.vmcmc.libs;

/**
 * MCMCInterface: Interface responsible for making sure that all classes using the
 * datacontainer provides the same functionality.
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
public interface MCMCInterface {
	void setDataContainer(MCMCDataContainer datacontainer);
	void setSeriesID(int id);
	void setBurnIn(double burnin);
	
	int getSeriesID();
	MCMCDataContainer getDataContainer();
}
