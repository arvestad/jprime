package se.cbb.jprime.apps.rwrappers;

import java.io.File;
import java.io.IOException;

/**
 * For convenience, collects methods related to executing R scripts.
 * <p/>
 * It may be a bit cumbersome to avoid printing info messages and
 * such. In this case, one solution is to let the script write to a temporary file 'f',
 * collect other output into a temporary sink file 's', then after execution possibly
 * echo the contents of 'f' to stdout (after which 'f' and 's' are marked for deletion or
 * similarly).
 * 
 * @author Joel Sjöstrand.
 */
public class RScriptExecuter {
	
	/**
	 * Executes an R script using "R CMD BATCH --slave --vanilla tmp.sink", where the sink is removed
	 * afterwards. Invoking applications should write their relevant output explicitly elsewhere.
	 * @param rFile file to be executed.
	 * @throws InterruptedException 
	 * @throws IOException.
	 */
	public static void execute(File rFile) throws IOException, InterruptedException {
		File tmpFile = File.createTempFile("ComputeKDEMode", ".sink");
		Process pr = Runtime.getRuntime().exec("R CMD BATCH --slave --vanilla " + rFile.getPath() + " " + tmpFile.getPath());
		pr.waitFor();
	}
}
