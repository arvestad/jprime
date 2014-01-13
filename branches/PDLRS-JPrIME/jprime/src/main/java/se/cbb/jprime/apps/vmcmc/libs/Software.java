package se.cbb.jprime.apps.vmcmc.libs;

import java.io.IOException;

public interface Software {
	static final String absolutePath = "./";
	
	public void displayHelp();
	public void setRelativePath(String resourcePath,String dataPath,String resultPath);
	public void setParameter();
	public String makeCommand();
	public boolean appendScriptFile(String scriptFileName, String command ) throws IOException;
	public boolean runCommand(String command) throws IOException;
}
