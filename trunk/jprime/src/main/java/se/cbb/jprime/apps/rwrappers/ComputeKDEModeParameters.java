package se.cbb.jprime.apps.rwrappers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;

public class ComputeKDEModeParameters {

	@Parameter(description = "Input file and pertinent parameters.")
	private List<String> arguments = Lists.newArrayList();

	@Parameter(names = { "-h", "--help" }, description = "Show help.")
	public Boolean showHelp = false;
	
	@Parameter(names = { "-b", "--bandwidth-method" }, description = "KDE bandwidth method.")
	public String bandwidthMethod = "normal-reference";

	@Parameter(names = {"-k", "--kernel"}, description = "Kernel type.")
	public String kernel = "gaussian";
	
	@Parameter(names = {"-t", "--training-pts"}, description = "Training points limit.")
	public Integer trainingPts = null;
	
	@Parameter(names = {"-e", "--evaluation-pts"}, description = "Evaluation points limit.")
	public Integer evaluationPts = null;

	@Parameter(names = {"-s", "--sort"}, description = "Sort samples.")
	public Integer sortColumn = null;
	
	@Parameter(names = {"-m", "--mirror"}, description = "Mirror samples around origin.")
	public Boolean mirror = false;
	
	@Parameter(names = {"-a", "--axes-pts"}, description = "Augment evaluation points along positive axes.")
	public Integer axesPts = null;
	
	@Parameter(names = {"-d", "--delimiter"}, description = "Output delimiter.")
	public String outDelim = ",";
	
	@Parameter(names = {"-o", "--output"}, description = "Output file.")
	public String outFile = null;
	
	public String getInfile() throws FileNotFoundException {
		String in = this.arguments.get(0);
		if (!(new File(in).exists())) {
			throw new FileNotFoundException("Could not find input file.");
		}
		return in;
	}
	
	public int[] getColumns() {
		String[] scols = arguments.get(1).split(",");
		int[] icols = new int[scols.length];
		for (int i = 0; i < icols.length; ++i) {
			icols[i] = Integer.parseInt(scols[i]);
		}
		return icols;
	}
	
	public void usage() {
		System.out.println(
				"======================================================================================================\n" +
				"Takes as input a sample file and outputs the mode of the marginal distribution w.r.t. 1, 2 or 3\n" +
				"of the parameters. The mode is estimated using kernel density estimation (KDE) by employing CRAN R and\n" +
				"its 'np' package (which must be installed). Input consists of a tab-/space-/comma-separated file\n" +
				"with a header, one parameter per column, and the column(s) of interest as a comma-separated string.\n" +
				"The sample points are used both for training and evaluation. NOTE: Columns missing header names may be\n" +
				"treated differently by R. Therefore always test the column numbering explicitly in advance.\n\n" +
				"Usage:\n" +
				"  ComputeKDEMode [options] <infile> <params>\n\n" +
				"Arguments:\n" +
				"  <infile>               Sample file, e.g. 'mysamples.coda'.\n" +
				"  <params>               Columns of interest, indexed from 1, e.g. '1,4,5'.\n\n" +
				"Options:\n" +
				"  -b <bwmethod>          Bandwidth estimation method. Valid values are 'cv.ml' for cross-validation\n" +
				"                         maximum likelihood, 'cv.ls' for cross-validation least-squares and\n" +
				"                         'normal-reference' for plain rule-of-thumb based on the IQR. The latter is\n" +
				"                         default. The two former are recommended but significantly more demanding.\n" +
				"  -k <kernel>            Kernel type. Valid values are 'gaussian', 'epanechnikov', 'rectangular'\n" +
				"                         'triangular', 'biweight', 'cosine' and 'optcosine'. Defaults to 'gaussian'.\n" +
				"  -t <noOfPts>           Limit KDE training points to a specified number.\n" +
				"  -e <noOfPts>           Limit KDE evaluation points to a specified number.\n" +
				"  -s <column>            Sort samples according to a certain column (indexed from 1). Ascending\n" +
				"                         sorting is assumed unless column number is prepended with minus sign.\n" +
				"                         Example: with likelihood in first column, one can use -s -1 to\n" +
				"                         sort samples in decreasing order. Only useful in conjunction with -t or -e.\n" +
				"  -m                     Mirror samples around the origin. Assumes that the samples are bounded to\n" +
				"                         the positive domain. Use this to account for the sparser density close to the\n" +
				"                         boundary.\n" +
				"  -a <noOfPts>           Augment evaluation points with points along each axis from the origin to the\n" +
				"                         corresponding 3rd marginal quartile. Assumes samples are bounded to the positive\n" +
				"                         domain. The specified number of points is added equidistantly along each axis.\n" +
				"  -d <delim>             Delimiter between output parameters. Defaults to comma.\n" +
				"  -o <outfile>           Write CRAN R file to output rather than executing R commands.\n" +
				"\n" +
				"Copyright: Joel Sjostrand, 2010.\n" +
				"======================================================================================================\n"
				);
	}
}
