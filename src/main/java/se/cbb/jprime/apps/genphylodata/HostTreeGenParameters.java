package se.cbb.jprime.apps.genphylodata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.beust.jcommander.Parameter;

/**
 * Contains user settings.
 * 
 * @author Joel Sjöstrand.
 */
public class HostTreeGenParameters {

	/** Required parameters: T, lambda, mu, outfile. */
	@Parameter(description = "<time interval> <birth rate> <death rate> <out prefix>")
	public List<String> args = new ArrayList<String>();
		
	/** Help. */
	@Parameter(names = {"-h", "--help"}, description = "Display help.")
	public Boolean help = false;
	
	/** Output. */
	@Parameter(names = {"-q", "--quiet"}, description = "Suppress creation of auxiliary files and write only pruned tree directly to stdout.")
	public Boolean doQuiet = false;
	
	/** PRNG seed. */
	@Parameter(names = {"-s", "--seed"}, description = "PRNG seed. Default: Random seed.")
	public String seed = null;

	/** Min leaves. */
	@Parameter(names = {"-min", "--min-leaves"}, description = "Minimum number of extant leaves required.")
	public Integer min = 2;
	
	/** Max leaves. */
	@Parameter(names = {"-max", "--max-leaves"}, description = "Maximum number of extant leaves allowed.")
	public Integer max = 64;
	
	/** Leaf size samples. */
	@Parameter(names = {"-sizes", "--leaf-sizes-file"}, description = "Samples the desired number of extant leaves uniformly from a single-column file. This is suitable for mimicking a known leaf size distribution. Default: No sampling.")
	public String leafSizes = null;
	
	/** Do meta. */
	@Parameter(names = {"-nox", "--no-auxiliary-tags"}, description = "Exclude auxiliary PrIME tags in output trees.")
	public Boolean excludeMeta = false;
	
	/** Leaf sampling. */
	@Parameter(names = {"-p", "--leaf-sampling-probability"}, description = "Governs the probability of observing a leaf. Lineages that fail to be observed will be pruned away similarly to lineages lost during the evolutionary process.")
	public String leafSamplingProb = "1.0";
	
	/** Start with bifurcation. */
	@Parameter(names = {"-bi", "--start-with-bifurcation"}, description = "Forces the process to start with a bifurcation event. Only a tree retaining this property after pruning will be output.")
	public Boolean bifurcationStart = false;
	
	/** Attempts. */
	@Parameter(names = {"-a", "--max-attempts"}, description = "Maximum number of attempts at creating random tree that meets requirements. If not met, no tree is output.")
	public Integer maxAttempts = 10000;
	
	/** Stem edge. */
	@Parameter(names = {"-stem", "--override-stem"}, description = "If set, overrides the stem edge of the generated tree by the specified value. If no stem edge is desired, this can be set to 0. Default: Sampled length.")
	public String stem = null;
	
	/** Vertex prefix. */
	@Parameter(names = {"-vp", "--vertex-prefix"}, description = "Vertex prefix.")
	public String vertexPrefix = "H";
	
	/**
	 * Returns output and info streams.
	 * @return streams.
	 */
	public BufferedWriter getOutputFile(String suffix) {
		try {
			String outprefix = args.get(3).trim();
			return new BufferedWriter(new FileWriter(outprefix + suffix));
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid output file prefix.", e);
		}
	}

	
	public double getT() {
		double v = Double.parseDouble(this.args.get(0));
		if (v <= 0.0) {
			throw new IllegalArgumentException("Invalid time interval.");
		}
		return v;
	}

	public double getBirthRate() {
		double v = Double.parseDouble(this.args.get(1));
		if (v < 0) {
			throw new IllegalArgumentException("Invalid rate.");
		}
		return v;
	}

	public double getDeathRate() {
		double v = Double.parseDouble(this.args.get(2));
		if (v < 0) {
			throw new IllegalArgumentException("Invalid rate.");
		}
		return v;
	}
	
	public Double getLeafSamplingProb() {
		double v = Double.parseDouble(this.leafSamplingProb);
		if (v < 0) {
			throw new IllegalArgumentException("Invalid leaf sampling prob.");
		}
		return v;
	}
	
	public ArrayList<Integer> getLeafSizes() throws FileNotFoundException {
		// Read file.
		if (this.leafSizes == null) { return null; }
		ArrayList<Integer> samples = new ArrayList<Integer>(8192);
		Scanner sc = new Scanner(new File(this.leafSizes));
		while (sc.hasNextLine()) {
			samples.add(Integer.parseInt(sc.nextLine()));
		}
		sc.close();
		return samples;
	}

}
