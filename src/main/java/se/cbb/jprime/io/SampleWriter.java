package se.cbb.jprime.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;


/**
 * Ordinary MCMC sampler for sampling to a character output stream.
 * Can output to stdout or to a file. Buffers output, although the
 * buffer is flushed continuously in the stdout case.
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
 * a small number of values (e.g. trees).
 * 
 * @author Joel Sjöstrand.
 */
public class SampleWriter implements Sampler {

	/** Default delimiter. */
	public static final String DEFAULT_DELIM = "\t";
	
	/** Default output stream buffer size. */
	public static final int DEFAULT_BUFFER_SIZE = 131072;
	
	/** Output stream. */
	private BufferedWriter out;
	
	/** Governs how often automatic sampling is performed. */
	private int flushFactor;
	
	/** Sampling counter. */
	private int noOfUnflushedSamples;
	
	/** Delimiter. */
	private String delim = DEFAULT_DELIM;
	
	/** String substituting part of state unchanged. Not used if null. */
	private String conciseSymbol = null;
	
	/** Last output sample. Null if conciseSymbol == null. */
	private String[] lastSample = null;
	
	/**
	 * Constructor. Samples to standard out, flushing buffer after each line.
	 */
	public SampleWriter() {
		this.out = new BufferedWriter(new OutputStreamWriter(System.out));
		this.flushFactor = 1;
		this.noOfUnflushedSamples = 0;
	}
	
	/**
	 * Constructor.
	 * @param out stream to sample to.
	 * @param flushFactor governs how often automatic sampling is performed.
	 */
	public SampleWriter(BufferedWriter out, int flushFactor) {
		this.out = out;
		this.flushFactor = flushFactor;
		this.noOfUnflushedSamples = 0;
	}
	
	/**
	 * Constructor. Uses the default encoding and a fairly high default buffer size.
	 * @param f the file to write to.
	 * @param flushFactor governs how often automatic sampling is performed.
	 * @throws IOException if output stream cannot be connected to f.
	 */
	public SampleWriter(File f, int flushFactor) throws IOException {
		this(new BufferedWriter(new FileWriter(f), DEFAULT_BUFFER_SIZE), flushFactor);
	}
	
	/**
	 * Constructor. Uses the desired buffer size.
	 * @param f the file to write to.
	 * @param bufferSz the buffer size.
	 * @param flushFactor governs how often automatic sampling is performed.
	 * @throws IOException if output stream cannot be connected to f.
	 */
	public SampleWriter(File f, int bufferSz, int flushFactor) throws IOException {
		this(new BufferedWriter(new FileWriter(f), bufferSz), flushFactor);
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
		this.lastSample = null;
	}
	
	/**
	 * Returns the current "concise symbol" -- see <code>setConcise(...)</code>.
	 * @return the symbol; null if actual value always output.
	 */
	public String getConcise() {
		return this.conciseSymbol;
	}
	
	@Override
	public void writeSampleHeader(List<Sampleable> sampleables) throws IOException {
		int sz = sampleables.size();
		if (sz == 0) { return; }
		for (int i = 0; i < sz - 1; ++i) {
			this.out.write(sampleables.get(i).getSampleHeader());
			this.out.write(this.delim);
		}
		this.out.write(sampleables.get(sz - 1).getSampleHeader());
		this.out.newLine();
	}
	
	@Override
	public void writeSample(List<Sampleable> sampleables, Sampleable.SamplingMode mode) throws IOException {
		String[] sample = this.getValue(sampleables, this.conciseSymbol != null, mode);
		
		for (int i = 0; i < sample.length - 1; ++i) {
			this.out.write(sample[i]);
			this.out.write(this.delim);
		}
		this.out.write(sample[sample.length - 1]);
		this.out.newLine();
		
		// Flush if desired.
		this.noOfUnflushedSamples++;
		if (this.noOfUnflushedSamples % this.flushFactor == 0) {
			this.out.flush();
			this.noOfUnflushedSamples = 0;
		}
	}
	
	/**
	 * Flushes the underlying output stream.
	 * @throws IOException.
	 */
	public void flush() throws IOException {
		this.out.flush();
	}
	
	/**
	 * Closes the underlying output stream, flushing it first.
	 * @throws IOException.
	 */
	public void close() throws IOException {
		this.out.close();
	}
	
	/**
	 * Retrieves the sample values.
	 * @param sampleables the sampleable objects.
	 * @param doConcise true to shorten repeats.
	 * @param mode sampling mode.
	 * @return the sample.
	 */
	private String[] getValue(List<Sampleable> sampleables, boolean doConcise, Sampleable.SamplingMode mode) {
		if (sampleables.size() == 0) { return new String[0]; }
		
		// Retrieve all current parameters.
		String[] sample = new String[sampleables.size()];
		for (int i = 0; i < sample.length; ++i) {
			sample[i] = sampleables.get(i).getSampleValue(mode);
		}
		
		// If desired, exchange unchanged parameters for symbol.
		if (doConcise) {
			if (this.lastSample == null) {
				this.lastSample = sample;
			} else {
				for (int i = 0; i < sample.length; ++i) {
					if (sample[i].equals(this.lastSample[i])) {
						this.lastSample[i] = this.conciseSymbol;
					} else {
						this.lastSample[i] = sample[i];
					}
					String[] tmp = this.lastSample;
					this.lastSample = sample;    // lastSample now up-to-date and non-abbreviated.
					sample = tmp;                // sample now abbreviated.
				}
			}
		}
		return sample;
	}
	
	@Override
	public String getSampleHeader(List<Sampleable> sampleables) {
		int sz = sampleables.size();
		if (sz == 0) { return ""; }
		StringBuilder sb = new StringBuilder(sz * 32);
		for (int i = 0; i < sz - 1; ++i) {
			sb.append(sampleables.get(i).getSampleHeader());
			sb.append(this.delim);
		}
		sb.append(sampleables.get(sz - 1).getSampleHeader());
		return sb.toString();
	}

	@Override
	public String getSample(List<Sampleable> sampleables, Sampleable.SamplingMode mode) {
		String[] sample = this.getValue(sampleables, false, mode);
		StringBuilder sb = new StringBuilder(sample.length * 32);
		for (int i = 0; i < sample.length - 1; ++i) {
			sb.append(sample[i]);
			sb.append(this.delim);
		}
		sb.append(sample[sample.length - 1]);
		return sb.toString();
	}

	@Override
	public void writeString(String str) throws IOException {
		this.out.write(str);
		
		// We flush immediately.
		this.out.flush();
	}

}
