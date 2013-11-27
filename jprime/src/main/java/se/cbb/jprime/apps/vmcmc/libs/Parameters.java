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
	@Parameter(description = "None or filename. VMCMC start window will appear, if no arguments are passed and VMCMC will display results and data in GUI if arguments filname is given.")
	public List<String> files = new ArrayList<String>();
	
	/** Help. */
	@Parameter(names = {"-h", "--help"}, description = "Display help. To understand the options and input parameters, use help.")
	public Boolean help = false;
	
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
	@Parameter(names = {"-e","--ess"}, description = "Effective Sample Size test result on command line only. VMCMC computed estimated sample size burn-in and convergenceestimator for the MCMC chain shown on stdout. ")
	public Boolean ess = false;
	
	/** Gelman-Rubin convergence test only */
	@Parameter(names = {"-r","--gelmanrubin"}, description = "Gelman-Rubin convergence test result on command line only. VMCMC computed gelman rubin burn-in and convergence estimator for the MCMC chain shown on stdout. ")
	public Boolean gr = false;
	
	/** Display Posterior distribution of trees only */
	@Parameter(names = {"-p","--posterior"}, description = "Display Posterior distribution of trees. ")
	public Boolean posterior = false;
	
	/** Estimated Sample Size Test only */
	@Parameter(names = {"-b","--burnin"}, description = "Burn-in values to reject for command line MAP trees. ")
	public String burnin = "-1";
	
	/** Estimated Sample Size Test only */
	@Parameter(names = {"-m","--maxaposterioritree"}, description = "Calculate and display MAP tree. ")
	public boolean maptree = false;
	
	/** Estimated Sample Size Test only */
	@Parameter(names = {"-ct","--convergencetest"}, description = "Run the overall convergence tests only. ")
	public boolean convergencetest = false;
}
