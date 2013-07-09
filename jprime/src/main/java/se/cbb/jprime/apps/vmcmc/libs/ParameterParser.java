package se.cbb.jprime.apps.vmcmc.libs;

import se.cbb.jprime.apps.vmcmc.libs.Parameters;
import se.cbb.jprime.misc.Triple;

public class ParameterParser {
	/* **************************************************************************** *
	 * 							CLASS PUBLIC FUNCTIONS								*
	 * **************************************************************************** */
	public static Triple<String, Integer, Double> getOptions(Parameters ps) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		String 					filename;
		int 					burnin;
		double 					confidence;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		filename		 		= ps.filename;
		burnin	 				= Integer.parseInt(ps.burnin);
		confidence		 		= Double.parseDouble(ps.confidence);
		
		/* ******************** FUNCTION BODY ************************************* */
		System.out.println("{\n\t\"File\": \"" + filename + "\",\n\t\"Burnin\": " + burnin + ",\n\t\"Confidence Level\": " + confidence + ",");
		return new Triple<String, Integer, Double>(filename, burnin, confidence);
	}
	/* **************************************************************************** *
	 * 							END OF CLASS										*
	 * **************************************************************************** */
}
