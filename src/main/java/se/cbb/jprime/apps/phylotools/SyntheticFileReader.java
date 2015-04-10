package se.cbb.jprime.apps.phylotools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class SyntheticFileReader {
	
	public String parseTreeFromReconciliationFile(String fileName) {
		try {
			String gfileName = "treeFromReconFile.tree";
		BufferedReader buf = new BufferedReader(new FileReader(fileName));
		BufferedWriter bw = new BufferedWriter(new FileWriter(gfileName));
		String line = "";
		while((line = buf.readLine()) != null){
			line = line.trim();
			if(line.charAt(0) == '#')
				continue;
			else {
				String[] token = line.split(";");
				
				String trueFile = "";
				if(token.length == 1)
					trueFile = token[0].trim();
				else
					trueFile = token[4].trim();
				bw.write(trueFile);
				bw.flush();
				bw.close();
				buf.close();
				
				return trueFile;
			}
		}
		}catch(Exception ex){
			System.err.println("Error in reading reconciliation file");
			System.err.println("Reason: " + ex.getMessage());
		}
		return null;
	}
}
