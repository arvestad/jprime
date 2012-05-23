package se.cbb.jprime.apps.vmcmc.libs;

import se.cbb.jprime.apps.vmcmc.libs.Parameters;
import se.cbb.jprime.misc.Triple;

public class ParameterParser {
	public static Triple<String, Integer, Double> Getoptions(Parameters ps) {
		String filename = ps.filename;
		int burnin = ps.burnin;
		double confidence = Double.parseDouble(ps.confidence);
			
		System.out.println("File: " + filename + "\nBurnin: " + burnin + "\nConfidence Level: " + confidence);
			
		return new Triple<String, Integer, Double>(filename, burnin, confidence);
	}
}
