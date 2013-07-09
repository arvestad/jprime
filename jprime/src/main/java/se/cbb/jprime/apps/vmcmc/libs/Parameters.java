package se.cbb.jprime.apps.vmcmc.libs;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

/**
 * Handles all regular application parameters for VMCMC. Used by MCMCMainApplication class for parameter parsing in the main function.
 * 
 * @author Raja Hashim Ali.
 */
public class Parameters {

	/** Required parameters: None */
	@Parameter(description = "None. VMCMC start window will appear, if no arguments are passed.")
	public List<String> files = new ArrayList<String>();
	
	/** Help. */
	@Parameter(names = {"-h", "--help"}, description = "Display help. To understand the options and input parameters, use help.")
	public Boolean help = false;
	
	/** Filename */
	@Parameter(names = {"-f","--filename"}, description = "Input file. View all the results and data with a Graphical User Interface for the file FILENAME. Check -n for same results on command line.\nDefault: No file")
	public String filename = null;
	
	/** Test and Simple Statistics only */
	@Parameter(names = {"-n","--nogui"}, description = "Test and simple statistics shown on command line only. VMCMC computed statistics and tests shown on stdout.")
	public Boolean nogui = false;
	
	/** Test and Simple Statistics only */
	@Parameter(names = {"-c","--confidencelevel"}, description = "Confidence Level value.")
	public String confidence = "95";
	
	/** Test Statistics only */
	@Parameter(names = {"-t","--testresult"}, description = "Test statistics shown on command line only. VMCMC computed tests shown on stdout. ")
	public Boolean test = false;
	
	/** Simple Statistics only */
	@Parameter(names = {"-s","--simplestats"}, description = "Statistics shown on command line only. VMCMC computed statistics for the MCMC chain shown on stdout. ")
	public Boolean stats = false;
	
	/** Geweke Convergence Test only */
	@Parameter(names = {"-g","--geweke"}, description = "Geweke convergence test and burn in estimator result on command line only. VMCMC computed geweke convergence and burn in estimator for the MCMC chain shown on stdout. ")
	public Boolean geweke = false;
	
	/** Estimated Sample Size Test only */
	@Parameter(names = {"-e","--ess"}, description = "Effective Sample Size test result on command line only. VMCMC computed estimated sample size estimator for the MCMC chain shown on stdout. ")
	public Boolean ess = false;
	
	/** Estimated Sample Size Test only */
	@Parameter(names = {"-r","--gelmanrubin"}, description = "Gelman-Rubin convergence test result on command line only. VMCMC computed estimated sample size estimator for the MCMC chain shown on stdout. ")
	public Boolean gr = false;
}
