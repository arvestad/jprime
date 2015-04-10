package se.cbb.jprime.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class provides various methods to deal with I/O from process (eg. stdout).
 * 
 * @author Vincent Llorens.
 */
public class ProcessStream {
	
    /**
     * Write the output of the stdout of p into the file outFile.
     * @param p process to capture the output from.
     * @param outFile file to write the output to.
     * @param overwrite tells whether to overwrite files if they already exist, default to false.
     * @throws IOException if the file outFile exists and overwrite is set to false.
     */
    public static void writeStreamToFile(Process p, String outFile, boolean overwrite) throws IOException {
    	File f = new File(outFile);
    	if(!f.exists() || overwrite) {
	    	FileWriter fwriter = null;
	    	InputStream cmdStdOut = p.getInputStream();
	    	BufferedReader stdOut = new BufferedReader(new InputStreamReader(cmdStdOut));
	    	String line;
			try {
				fwriter = new FileWriter(f);
				BufferedWriter out = new BufferedWriter(fwriter);
				while((line = stdOut.readLine()) != null) {
					out.write(line+"\n");
				}
		   		out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	} else {
    		throw new IOException("File "+outFile+" exists and overwrite is set to false.");
    	}
    }
    
    /**
     * Write the output of the stdout of p into the file outFile. If the file already exists it throws an error.
     * @param p process to capture the output from.
     * @param outFile file to write the output to.
     * @throws IOException if the file outFile exists and overwrite is set to false
     */
    public static void writeStreamToFile(Process p, String outFile) throws IOException {
    	writeStreamToFile(p, outFile, false);
    }

}
