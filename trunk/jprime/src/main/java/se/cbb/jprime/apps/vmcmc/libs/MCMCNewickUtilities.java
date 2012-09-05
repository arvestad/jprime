package se.cbb.jprime.apps.vmcmc.libs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class MCMCNewickUtilities implements Software {

	private String 	softwareName;
	private String 	resourcePath;
	private String 	resultPath;
	private String 	inputFileName;
	private String	outputFile;
	
	public MCMCNewickUtilities() {
		softwareName				= "nw_display";
		resultPath  			 	= absolutePath + "src/main/resources/vmcmc/Results/";
		resourcePath 				= absolutePath + "src/main/resources/vmcmc/Resources/";
	}
	
	@Override
	public boolean runCommand(String command) {
		try {
			Runtime rt 						= Runtime.getRuntime();
			Process pr 						= rt.exec(command);
			BufferedReader stdInput 		= new BufferedReader(new InputStreamReader(pr.getInputStream()));
		    BufferedReader stdError 		= new BufferedReader(new InputStreamReader(pr.getErrorStream()));

            String line 					= null;
            while ( (line = stdInput.readLine()) != null)
                System.out.println(line);
            
            while ( (line = stdError.readLine()) != null)
                System.out.println(line);
			
			pr.waitFor();
            
			return true;
		} catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
	}


	@Override
	public void displayHelp() {
		// TODO Auto-generated method stub
		String command = resourcePath + softwareName + " -help";
		runCommand(command);
	}


	@Override
	public void setRelativePath(String resourcePath, String dataPath, String resultPath) {
		this.resourcePath 				= resourcePath;
		this.resultPath 				= resultPath;
	}


	@Override
	public void setParameter() {
		// TODO Auto-generated method stub
		
	}
	
	public void setParameter(String inputFileName, String outputFileName) {
		// TODO Auto-generated method stub
		this.inputFileName 	= inputFileName;
		this.outputFile 	= outputFileName;
	}


	@Override
	public String makeCommand() {
		String command = resourcePath + softwareName + " " + resultPath + inputFileName + " > " + resultPath + outputFile;
		try {
			appendScriptFile("NewickUtilitiesScript.sh", command);
		} catch (IOException e) {
			System.out.println("Script File Modification failed. " + e.getMessage());
		}
		return resultPath + "NewickUtilitiesScript.sh";
	}


	@Override
	public boolean appendScriptFile(String scriptFileName, String command) throws IOException {
		BufferedWriter out 	= new BufferedWriter(new FileWriter(resultPath + scriptFileName));

		out.write(command);
		out.flush();
		out.close();
		
		runCommand("chmod 777 " + resultPath + scriptFileName);
		return true;
	}
	
	public void createAndWriteString(String fileName, String string) throws Exception {
		BufferedWriter out 	= new BufferedWriter(new FileWriter(resultPath + fileName));

		out.write(string);
		
		out.flush();
		out.close();
	}
	
	public String stringReader(String fileName) throws Exception {
		BufferedReader in 		= new BufferedReader(new FileReader(resultPath + fileName));
		String line;
		String result			= "";
		
		while ((line = in.readLine()) != null) {
			result = result + line;
			result = result + "\n";
		}

		in.close();
		return result;
	}
	
	public boolean FileDeleter(String fileName){
		File in 		= new File(resultPath + fileName);
	    boolean success = in.delete();

	    if (!success){
	    	System.out.println("Deletion failed.");
	    	System.exit(0);
	    }
		return success;
	}
}
