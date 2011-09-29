package se.cbb.jprime.apps.gsrf;

import com.beust.jcommander.JCommander;

/**
 * TBD.
 * 
 * @author Joel Sjöstrand.
 */
public class GSRf {
	
	/**
	 * GSRf starter.
	 * @param args.
	 */
	public static void main(String[] args) {
		try {
			// Parse options.
			Parameters params = new Parameters();
			JCommander jc = new JCommander(params, args);
			if (args.length == 0 || params.help) {
				jc.usage();
				return;
			}
		} catch (Exception e) {
			System.err.print(e);
			System.err.print("\nUse option -h or --help to show usage.");
		}
	}
	
	
}
