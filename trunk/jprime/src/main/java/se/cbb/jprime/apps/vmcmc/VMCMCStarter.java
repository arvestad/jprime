package se.cbb.jprime.apps.vmcmc;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

import se.cbb.jprime.apps.JPrIMEApp;
import se.cbb.jprime.apps.vmcmc.libs.ParameterParser;
import se.cbb.jprime.apps.vmcmc.libs.Parameters;
import se.cbb.jprime.misc.Triple;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterDescription;

public class VMCMCStarter implements JPrIMEApp {
	
	@Override
	public String getAppName() {
		return "VMCMC";
	}

	/** Definition: 			Main function for VMCMC.										
	<p>Usage: 				Initialize the application from command line.						
 	<p>Function:			Gets the inputs from command line, parses them and calls the appropriate constructor of MCMCWindow. 				
 	<p>Classes:				Parameters, JCommander, JCommanderUserWrapper, Triple.
 	<p>Internal Functions: 	MCMCApplication(),  		
 	@return 				(A new graphical window)/(command line) statistical and/or convergence test analysis.					
	 */
	public void main(String[] args) {
		if (args.length == 0)
			new MCMCApplication();
		else {
			Parameters params = new Parameters();
			JCommander vmcmc = new JCommander(params, args);

			if (params.help) {
				StringBuilder sb = new StringBuilder(65536);
				sb.append("Usage: java vmcmc [options] ").append('\n');
				
				ParameterDescription mainParam = vmcmc.getMainParameter();
				if (mainParam != null) {
					sb.append("Required arguments\n");
					sb.append("     ").append(mainParam.getDescription()).append('\n');
				}
				List<ParameterDescription> params1 = vmcmc.getParameters();
				Field[] fields = params.getClass().getFields();
				for (Field f : fields) {
					for (ParameterDescription p : params1) {
						if (f.getName().equals(p.getField().getName())) {
							sb.append(p.getNames()).append('\n');
							String def = (p.getDefault() == null ? "" : " Default: " + p.getDefault().toString() + '.');
							String desc = WordUtils.wrap(p.getDescription() + def, 120);
							desc = "     " + desc;
							desc = desc.replaceAll("\n", "\n     ") + '\n';
							sb.append(desc);
							break;
						}
					}
				}
				System.out.println(sb.toString());
			}
			else if (args.length == 1)
				new MCMCApplication(args[0]);
			else if (params.filename == null)
				System.out.println("File Name not provided. Use -f for inputting filename or see -h for valid options.");
			else if ((params.nogui == false) && (params.test == false) && (params.stats == false) && (params.ess == false) && (params.geweke == false) && (params.gr == false))
				new MCMCApplication(params.filename);
			else {
				Triple<String, Integer, Double> paramData = ParameterParser.getOptions(params);
				try {
					if (params.nogui == true) {
						new MCMCApplication(7, paramData.first, paramData.second, paramData.third);
					} else if (params.test == true) {
						new MCMCApplication(2, paramData.first, paramData.second, paramData.third);
					} else if (params.stats == true) {
						new MCMCApplication(3, paramData.first, paramData.second, paramData.third);
					} else if (params.geweke == true) {
						new MCMCApplication(4, paramData.first, paramData.second, paramData.third);
					} else if (params.ess == true) {
						new MCMCApplication(5, paramData.first, paramData.second, paramData.third);
					} else if (params.gr == true) {
						new MCMCApplication(6, paramData.first, paramData.second, paramData.third);
					}
					System.out.println("\n\t]");
					System.out.println("}");
				} catch (Exception e) {
					System.out.println("Error : " + e.getMessage());
					System.exit(-1);
				}
			}
		}
	}
	
}
