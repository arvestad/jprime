package se.cbb.jprime.apps.phylotools;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

/**
 * JCommander parameters for <code>RobinsonFouldsDistance</code>.
 * 
 * @author Joel Sjöstrand.
 */
public class RobinsonFouldsDistanceParameters {

	/** Required parameters: Input file 1 and possibly 2. */
	@Parameter(description = "<File> [File2].")
	public List<String> infiles = new ArrayList<String>();
	
	/** Help. */
	@Parameter(names = {"-h", "--help"}, description = "Display help.")
	public Boolean help = false;
	
	/** Treat trees as rooted. */
	@Parameter(names = {"-r", "--rooted"}, description = "Treat trees as rooted.")
	public Boolean rooted = true;
	
}
