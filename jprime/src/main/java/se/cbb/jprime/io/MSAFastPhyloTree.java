package se.cbb.jprime.io;

import java.io.File;
import java.io.IOException;

/**
 * For getting a guest tree from a MSA by calling FastPhylo.
 * 
 * @author Vincent Llorens.
 */
public class MSAFastPhyloTree {
	
	/**
	 * Returns true if the FastPhylo binaries <code>fastdist</code>, <code>fastprot</code> and <code>fnj</code>
	 * are found in the path. If only one these binaries are found, returns false. If none of these binaries are
	 * found, returns false.
	 * @return true if <code>fastdist</code> and <code>fastprot</code> and <code>fnj</code> are in the path.
	 * @throws Exception if FastPhylo binaries are not found in the path.
	 */
    private static boolean isFastPhyloInPath() throws Exception {
        boolean res = true;
        String fastdist = "fastdist -V";
        String fastprot = "fastprot -V";
        String fnj = "fnj -V";
        Runtime rt = Runtime.getRuntime();
        try {
            rt.exec(fastdist);
            rt.exec(fastprot);
            rt.exec(fnj);
        } catch (IOException e) {
            res = false;
            throw new Exception("Error: FastPhylo is not in path.");
        }
        return res;
    }
    
    /**
     * Runs FastPhylo on the specified MSA file. 
     * @param inFile a multi-alignment of sequences file in Fasta format.
     * @param outFile the file that will contain the output of FastPhylo: a newick tree.
     * @param distProgram describes the program to use to compute the distance matrix from the MSA, either fastdist
     * (for DNA sequences) or fastprot (for protein sequences).
     * @param overwriteFiles true to overwrite the files during the process of Fast Phylo.
     * @return guest tree, names and branch lengths.
     */
    private static void runFastPhylo(String inFile, String outFile, String distProgram, boolean overwriteFiles) {
		Runtime rt = Runtime.getRuntime();
		try {
			// Apply distProgram on the MSA, it returns a distance matrix
			Process fdist = rt.exec(distProgram+" -I fasta "+inFile);
			ProcessStream.writeStreamToFile(fdist, inFile+".tmp", overwriteFiles);
			// Apply fnj on this distance matrix, it returns a tree
			Process fnj = rt.exec("fnj -I xml -O newick "+inFile+".tmp");
			ProcessStream.writeStreamToFile(fnj, outFile, overwriteFiles);
			// Delete the temporary file
			File ftmp = new File(inFile+".tmp");
			ftmp.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Return a Newick tree computed from a multialignment by Fast Phylo.
     * @param msaFile the mutialignment file, in Fasta format.
     * @param outTreeFile the name of the output file containing the newick tree.
     * @param substitutionModel the substitution model type used during the MCMC, to know if we have nucleotide or amino acid sequences.
     * @param overwrite true to overwrite an existing output file.
     * @return Newick tree computed by Fast Phylo.
     * @throws Exception.
     * @throws IOException.
     * @throws NewickIOException. 
     */
    public static NewickTree getTreeFromFastPhylo(String msaFile, String outTreeFile, String substitutionModel, boolean overwrite) throws Exception {
    	NewickTree GRaw = null;
    	String distProgram;
    	if (substitutionModel.equalsIgnoreCase("JC69")) {
    		// DNA
    		distProgram = "fastdist";
    	} else if (substitutionModel.equalsIgnoreCase("JTT") || substitutionModel.equalsIgnoreCase("UNIFORMAA")){
    		// Amino acids
    		distProgram = "fastprot";
    	} else {
    		// Codon
    		throw new IllegalArgumentException("FastPhylo can not handle codon sequences.");
    	}
	   	try {
	   		isFastPhyloInPath();
		   	runFastPhylo(msaFile, outTreeFile, distProgram, overwrite);
		   	File out = new File(outTreeFile);
		   	GRaw = NewickTreeReader.readTree(out, true);
		   	// Deleting temporary output file
		    out.delete();
	   	} catch (NewickIOException ne) {
	   		throw new NewickIOException(ne.getMessage(), ne);
    	} catch (IOException ioe) {
	   		throw new IOException(ioe.getMessage(), ioe);
	   	} catch (Exception e) {
    		throw new Exception(e.getMessage(), e);
    	}
	    return GRaw;
    }
    
    /**
     * Return a Newick tree computed from a multialignment by Fast Phylo.
     * @param msaFile the mutialignment file, in Fasta format.
     * @param outTreeFile the name of the output file containing the newick tree.
     * @param substitutionModel the substitution model type used during the MCMC, to know if we have nucleotide or amino acid sequences.
     * @return Newick tree computed by Fast Phylo.
     * @throws Exception.
     * @throws IOException.
     * @throws NewickIOException. 
     */
    public static NewickTree getTreeFromFastPhylo(String msaFile, String outTreeFile, String substitutionModel) throws Exception {
    	return getTreeFromFastPhylo(msaFile, outTreeFile, substitutionModel, false);
    }
    
    /**
     * Return a Newick tree computed from a multialignment by Fast Phylo.
     * @param msaFile the mutialignment file, in Fasta format.
     * @param substitutionModel the substitution model type used during the MCMC, to know if we have nucleotide or amino acid sequences.
     * @return Newick tree computed by Fast Phylo.
     * @throws Exception.
     * @throws IOException.
     * @throws NewickIOException. 
     */
    public static NewickTree getTreeFromFastPhylo(String msaFile, String substitutionModel) throws Exception {
    	return getTreeFromFastPhylo(msaFile, msaFile+"-out.newick", substitutionModel);
    }
    
}
