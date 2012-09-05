package se.cbb.jprime.apps.vmcmc.libs;

/**
 * MCMCInterface: Interface responsible for making sure that all classes using the
 * datacontainer provides the same functionality.
 */
public interface MCMCInterface {
	void 				setDataContainer			(MCMCDataContainer datacontainer);
	void 				setSeriesID					(int id);
	void 				setBurnIn					(double burnin);
	int 				getSeriesID					();
	MCMCDataContainer 	getDataContainer			();
}
