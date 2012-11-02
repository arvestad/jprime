package se.cbb.jprime.apps;

import java.util.Set;
import java.util.TreeMap;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Starter class for all apps. Uses reflection to search for main methods in the
 * <code>se.cbb.jprime.apps</code> package, and uses the corresponding class name
 * as a program name identifier. Apps are thus started as an ordinary executable:
 * <code>./jprime-X.Y.Z.jar &lt;application&gt; [options] &lt;arguments&gt;</code> etc.,
 * or alternatively: <code>java -jar jprime-X.Y.Z.jar &lt;application&gt; [options] &lt;arguments&gt;</code> etc.
 * 
 * @author Joel Sjöstrand.
 */
public class JPrIMEStarter {

	/**
	 * Starts a JPrIME application located in the <code>se.cbb.jprime.apps</code> folder
	 * (or sub-folder).
	 * @param args program name, followed by arguments (and necessarily in that order).
	 * @throws Exception.
	 */
	public static void main(String[] args) throws Exception {
		
		// Find all starter methods in apps package.
		Logger logger = (Logger) Reflections.log;
		Level oldLvl = logger.getLevel();
		logger.setLevel(Level.OFF);
		Reflections reflections = new Reflections(
			    new ConfigurationBuilder()
			        .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("se.cbb.jprime.apps")))
			        .setUrls(ClasspathHelper.forPackage("se.cbb.jprime.apps"))
			        .setScanners(new SubTypesScanner())
			        
			);
		Set<Class<? extends JPrIMEApp>> apps = reflections.getSubTypesOf(JPrIMEApp.class);
		logger.setLevel(oldLvl);
		
		// Create map linking name and app (we assume uniqueness).
		TreeMap<String, JPrIMEApp> map = new TreeMap<String, JPrIMEApp>();
		for (Class<? extends JPrIMEApp> c : apps) {
			try {
				// NOTE: Empty constructor assumed!!!!
				JPrIMEApp app = c.newInstance();
				map.put(app.getAppName(), app);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		if (args.length < 1 || args[0].equalsIgnoreCase("-h") || args[0].equalsIgnoreCase("--help")) {
			// No app specified.
			System.out.println(
					"================================================================================\n" +
					"JPrIME is a Java library for primarily phylogenetics by groups in computational\n" +
					"biology at Science for Life Laboratory (SciLifeLab) in Stockholm.\n\n" +
					"Releases, source code and tutorial: http://code.google.com/p/jprime\n\n" +
					"License: JPrIME is available under the New BSD License.\n"
					);
			System.out.println("Usage: jprime-x.y.z.jar <application> [options] <arguments>\n");
			System.out.println("List of available applications:");
			for (String k : map.keySet()) {
				System.out.println("    " + k);
			}
			System.out.println("You can usually obtain app-specific help thus: jprime-x.y.z.jar <application> -h");
			System.out.println("================================================================================\n");
		} else if (!map.containsKey(args[0])) {
			System.out.println("Unknown application. Use -h to show help.");
			System.out.println("Usage: jprime-x.y.z.jar <application> [options] <arguments>");
		} else {
			// Start app. Remove app name first, though.
			String[] appArgs = new String[args.length - 1];
			System.arraycopy(args, 1, appArgs, 0, appArgs.length);
			JPrIMEApp app = map.get(args[0]);
			app.main(appArgs);
		}
	}

}
