package se.cbb.jprime.apps.vmcmc.libs;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterDescription;

/**
 * For reformatting JCommander usage string.
 * 
 * @author Joel Sjöstrand.
 */
public class JCommanderUserWrapper {

	/**
	 * Obtains JCommander arguments in order of appearance rather than sorted
	 * by name (which is the case with a plain usage() call in JCommander 1.18).
	 * @param jc JCommander object.
	 * @param parameters class with JCommander parameters (and nothing else!).
	 * @param out string builder to write to.
	 * @return the usage.
	 */
	public static void getUnsortedUsage(JCommander jc, Object jcParams, StringBuilder out) {
		// Special treatment of main parameter.
		ParameterDescription mainParam = jc.getMainParameter();
		if (mainParam != null) {
			out.append("Required arguments\n");
			out.append("     ").append(mainParam.getDescription()).append('\n');
		}
		List<ParameterDescription> params = jc.getParameters();
		Field[] fields = jcParams.getClass().getFields();
		for (Field f : fields) {
			for (ParameterDescription p : params) {
				if (f.getName().equals(p.getField().getName())) {
					out.append(p.getNames()).append('\n');
					String def = (p.getDefault() == null ? "" : " Default: " + p.getDefault().toString() + '.');
					String desc = WordUtils.wrap(p.getDescription() + def, 120);
					desc = "     " + desc;
					desc = desc.replaceAll("\n", "\n     ") + '\n';
					out.append(desc);
					break;
				}
			}
		}
	}
	
}