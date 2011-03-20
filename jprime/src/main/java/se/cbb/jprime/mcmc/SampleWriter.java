package se.cbb.jprime.mcmc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;


/**
 * Ordinary MCMC sampler for sampling to a character output stream.
 * Can output to standard out or to a file. Buffers output, although the
 * buffer is flushed continuously in the standard out case.
 * <p/>
 * The user may change the default delimiter (tab) with <code>setDelim(...)</code>.
 * Also, it is possible, e.g. by invoking <code>setConcise("-")</code>, to let the parts of a
 * sample which has not changed from its previous state to be output as a dash:
 * <pre>
 * (a,(b,c))	4	0.345
 * -		3	0.213
 * -		4	0.743
 * ((a,b),c)	-	0.621
 * ...
 * </pre>
 * This may reduce file size when there are discrete parameters concentrated on
 * a small number of values.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleWriter implements Sampler {

	/** Default delimiter. */
	public static final String DEFAULT_DELIM = "\t";
	
	/** Default output stream buffer size. */
	public static final int DEFAULT_BUFFER_SIZE = 131072;
	
	/** The objects to sample from (processed in list order). */
	private List<Sampleable> sampleables;
	
	/** Output stream. */
	private BufferedWriter out;
	
	/** If true, flushes buffer immediately after sample. */
	private boolean flushAfterSampling;
	
	/** Delimiter. */
	private String delim = DEFAULT_DELIM;
	
	/** String substituting part of state unchanged. Not used if null. */
	private String conciseSymbol = null;
	
	/** Last output sample. Null if conciseSymbol == null. */
	private String[] lastSample = null;
	
	/**
	 * Constructor. Samples to standard out, flushing buffer after each line.
	 * @param sampleables the "sampleables".
	 */
	public SampleWriter(List<Sampleable> sampleables) {
		this.sampleables = sampleables;
		this.out = new BufferedWriter(new OutputStreamWriter(System.out));
		this.flushAfterSampling = true;
	}
	
	/**
	 * Constructor.
	 * @param sampleables the "sampleables".
	 * @param out stream to sample to.
	 */
	public SampleWriter(List<Sampleable> sampleables, BufferedWriter out) {
		this.sampleables = sampleables;
		this.out = out;
		this.flushAfterSampling = false;
	}
	
	/**
	 * Constructor. Uses the default encoding and a fairly high default buffer size.
	 * @param sampleables the "sampleables".
	 * @param f the file to write to.
	 * @throws IOException if output stream cannot be connected to f.
	 */
	public SampleWriter(List<Sampleable> sampleables, File f) throws IOException {
		this(sampleables, new BufferedWriter(new FileWriter(f), DEFAULT_BUFFER_SIZE));
	}
	
	/**
	 * Constructor. Uses the desired buffer size.
	 * @param sampleables the "sampleables".
	 * @param f the file to write to.
	 * @param bufferSz the buffer size.
	 * @throws IOException if output stream cannot be connected to f.
	 */
	public SampleWriter(List<Sampleable> sampleables, File f, int bufferSz) throws IOException {
		this(sampleables, new BufferedWriter(new FileWriter(f), bufferSz));
	}
	
	@Override
	public void setSampleables(List<Sampleable> sampleables) {
		this.sampleables = sampleables;
	}

	@Override
	public List<Sampleable> getSampleables() {
		return this.sampleables;
	}

	/**
	 * Sets the delimiter between parameters.
	 * @param delim the delimiter.
	 */
	public void setDelim(String delim) {
		if (delim == null) {
			throw new IllegalArgumentException("Cannot sample with null as delimiter.");
		}
		this.delim = delim;
	}
	
	/**
	 * Returns the delimiter output between parameters.
	 * @return the delimiter.
	 */
	public String getDelim() {
		return this.delim;
	}
	
	/**
	 * Turns on/off marking parameters that have not changed since last state
	 * with a symbol instead.
	 * @param symbol the string used to indicate no parameter change; null to always
	 *        output actual value.
	 */
	public void setConcise(String symbol) {
		this.conciseSymbol = symbol;
		this.lastSample = (symbol == null ? null : new String[this.sampleables.size()]);
	}
	
	/**
	 * Returns the current "concise symbol" -- see <code>setConcise(...)</code>.
	 * @return the symbol; null if actual value always output.
	 */
	public String getConcise() {
		return this.conciseSymbol;
	}
	
	@Override
	public void writeSampleHeader() throws IOException {
		int sz = this.sampleables.size();
		for (int i = 0; i < sz - 1; ++i) {
			this.out.write(this.sampleables.get(i).getSampleHeader());
			this.out.write(this.delim);
		}
		this.out.write(this.sampleables.get(sz - 1).getSampleHeader());
		this.out.newLine();
	}
	
	@Override
	public void writeSample() throws IOException {
		if (this.sampleables.size() == 0) { return; }
		
		// Retrieve all current parameters.
		String[] sample = new String[this.sampleables.size()];
		for (int i = 0; i < sample.length; ++i) {
			sample[i] = this.sampleables.get(i).getSampleValue();
		}
		
		// If desired, exchange unchanged parameters for symbol.
		if (this.conciseSymbol != null) {
			for (int i = 0; i < sample.length; ++i) {
				if (sample[i].equals(this.lastSample[i])) {
					this.lastSample[i] = this.conciseSymbol;
				} else {
					this.lastSample[i] = sample[i];
				}
				String[] tmp = this.lastSample;
				this.lastSample = sample;
				sample = tmp;
			}
		}
		
		for (int i = 0; i < sample.length - 1; ++i) {
			this.out.write(sample[i]);
			this.out.write(this.delim);
		}
		this.out.write(sample[sample.length - 1]);
		this.out.newLine();
		
		// Flush if desired.
		if (this.flushAfterSampling) {
			this.out.flush();
		}
	}

}
