package se.cbb.jprime.apps.realise;

import java.util.ArrayList;
import java.util.List;

import se.cbb.jprime.seqevo.SubstitutionMatrixHandlerFactory;

import com.beust.jcommander.Parameter;

/**
 * Handles all regular application parameters.
 * 
 * @author Joel Sjöstrand.
 * @author Vincent Llorens.
 * @author Owais Mahmudi.
 */
public class Parameters {

	/** MCMC file. */
	@Parameter(names = {"-mi", "--mcmcinfile"}, description = "<MCMC File>")
	public String inmcmcfile = null;
	
	/** Help. */
	@Parameter(names = {"-h", "--help"}, description = "Display help.")
	public Boolean help = false;
	
	/** Output mcmc location. */
	@Parameter(names = {"-mo", "--mcmcoutfile"}, description = "Output file for MAP Gene tree analysis.")
	public String outmcmcfile = null;
	
//	/** Output location. */
//	@Parameter(names = {"-o", "--outfile"}, description = "Output file.")
//	public String outfile = null;
	
//	/** MAP gene trees location. */
//	@Parameter(names = {"-mapgtf", "--mapgenetrees"}, description = "MAP gene trees file.")
//	public String mapgenetreefile = null;
	
	/** File for keeping stats of transfers. */
	@Parameter(names = {"-tstats", "--transferstats"}, description = "Output file for keeping statistics of transfers.")
	public String transferstatsfile = null;
	
	/** Info output location. */
	@Parameter(names = {"-info", "--infofile"}, description = "Info output file. Default: <outfile>.info when -o has been specified, " +
			"stdout when -o has not been specified, suppressed if -info NONE is specified.")
	public String infofile = null;
	
	/** Input realisation file. */
	@Parameter(names = {"-ri", "--realinfile"}, description = "Input file for MAP Realisation analysis.")
	public String inrealfile = null;
	
//	/** Output realisation file. */
//	@Parameter(names = {"-ro", "--realoutfile"}, description = "Output file for MAP Realisation analysis.")
//	public String outrealfile = null;
	
	@Parameter(names = {"-s", "--synthetic"}, description = "Performs the synthetic analysis.")
	public Boolean synthetic = false;
	
	@Parameter(names = {"-b", "--biological"}, description = "Performs the biological analysis.")
	public Boolean biological = false;
	
	/** Proportion of samples to discard as burn-in. */
	@Parameter(names = {"-gtsetb", "--guesttreesetburninprop"}, description = "Proportion of samples to discard as burn-in in the guest tree set, e.g. 0.25 for 25%..")
	public String guestTreeSetBurninProp = "0.25";
	
	/** Required parameters: True gene tree with transfers, and Realisation file */
	@Parameter(description = "<True tree> <Realisation file>.")
	public List<String> files = new ArrayList<String>();
		
	/** Discretisation timestep. */
	@Parameter(names = {"-dts", "--discretisationtimestep"}, description = "Discretisation timestep upper bound. E.g. 0.02 yields" +
			" timesteps of max size 0.02 on each host edge.")
	public String discTimestep = "0.02";
	
	/** Min. no of discretisation intervals. */
	@Parameter(names = {"-dmin", "--discretisationmin"}, description = "Min. no. of discretisation intervals on each host edge.")
	public Integer discMin = 3;
	
	/** Max. no of discretisation intervals. */
	@Parameter(names = {"-dmax", "--discretisationmax"}, description = "Max. no. of discretisation intervals on each host edge.")
	public Integer discMax = 10;
	
	/** No of discretisation intervals for stem. */
	@Parameter(names = {"-dstem", "--discretisationstem"}, description = "No. of discretisation intervals on the host edge predating the root." +
			" Default: Simple rule-of-thumb.")
	public Integer discStem = null;

//	/** Debug flag. */
//	@Parameter(names = {"-dbg", "--debug"}, description = "Output debugging info.")
//	public Boolean debug = false;
	
}
