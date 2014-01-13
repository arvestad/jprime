package se.cbb.jprime.apps.rwrappers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import com.beust.jcommander.JCommander;

/**
 * R wrapper application to non-parametrically estimate the mode of a sampled distribution
 * using kernel density estimation (KDE). k-variate distributions for k={1,2,3}
 * are supported.
 * <p/>
 * Computations are made using CRAN R with the np package, both of which must be available.
 * Input consists of an R-compatible file, e.g. tab-delimited, where the columns of interest
 * are specified in user input.
 * 
 * @author Joel Sjöstrand.
 */
public class ComputeKDEMode {
	
	public static void main(String[] args) throws InterruptedException {
		
		// Read options and parameters.
		ComputeKDEModeParameters params = new ComputeKDEModeParameters();
		new JCommander(params, args);
		if (args.length == 0 || params.showHelp) {
			params.usage();
			return;
		}
		
		// Compute the mode.
		try {
			System.out.println(computeMode(params));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Computes the mode and returns it as a string. If the parameters specifies
	 * generation of an R file instead, an empty string is returned.
	 * @param params the input file and options.
	 * @return the mode, or empty string in case a permanent R file is produced.
	 * @throws IOException.
	 * @throws InterruptedException.
	 */
	public static String computeMode(ComputeKDEModeParameters params) throws InterruptedException, IOException {
		File f;
		if (params.outFile == null) {
			// Temporary file to be executed.
			f = File.createTempFile("ComputeKDEMode", ".r");
		} else {
			// Permanent file no to be executed.
			f = new File(params.outFile);
		}
		BufferedWriter out = new BufferedWriter(new FileWriter(f));
		writeRFile(out, params);
		
		// Execute R-file if output not specified.
		StringBuilder output = new StringBuilder();
		if (params.outFile == null) {
			File res = File.createTempFile("ComputeKDEMode", ".out");
			out.write("write(file='" + res.getPath() + "', x=map, append=TRUE, sep='" + params.outDelim + "');\n");
			out.close();
			RScriptExecuter.execute(f);
			Scanner sc = new Scanner(res);
			while (sc.hasNextLine()) {
				output.append(sc.nextLine());
			}
			sc.close();
		} else {
			out.close();
		}
		return output.toString();
	}
	
	private static void writeRFile(BufferedWriter out, ComputeKDEModeParameters params) throws IOException {
		// Input file.
		out.write("dat <- read.table('" + params.getInfile() + "', header=TRUE);\n");
		// Sort data if desired.
		if (params.sortColumn != null) {
			int scol = Math.abs(params.sortColumn);
			SampleSorter ss = new SampleSorter("dat", scol, (params.sortColumn > 0), "dat");
			out.write(ss.toString());
		}
		int[] cols = params.getColumns();
		if (cols.length < 1 || cols.length > 3) {
			throw new IllegalArgumentException("Number of columns must be between 1 and 3.");
		}
		
		// Filter out only the relevant columns.
		out.write((new SampleFilter("dat", cols, null, "dat")).toString());
		
		// Acquire the subset of training points.
		out.write((new SampleFilter("dat", null, params.trainingPts, "tdat")).toString());
		
		// Acquire the subset of evaluation points.
		out.write((new SampleFilter("dat", null, params.evaluationPts, "edat")).toString());
		
		// Bandwidth object.
		// NOTE: BW estimation occurs prior to mirroring now. Which is most reasonable: before/after?
		//       The latter is intuitive, but may lead to over-smoothing...?
		out.write((new NPBandwidth("tdat", params.bandwidthMethod, params.kernel, "bw")).toString());
		
		// Boundary correction (mirroring) if desired.
		if (params.mirror) {
			out.write((new BoundaryMirror("tdat", cols.length, "tdat")).toString());
		}
		
		// Additional evaluation points along boundary if desired.
		if (params.axesPts != null) {
			out.write((new BoundaryAugmenter("edat", cols.length, params.axesPts, "edat")).toString());
		}
		
		// Estimate, then pick sample with highest density.
		out.write((new NPDensity("bw", "tdat", "edat", "dens")).toString());
		out.write("mapidx <- which.max(dens$dens);\n");
		if (cols.length == 1) {
			out.write("map <- edat[mapidx];\n");
		} else {
			out.write("map <- edat[mapidx, ];\n");
		}
	}
}
