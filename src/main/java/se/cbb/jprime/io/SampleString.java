package se.cbb.jprime.io;

/**
 * Sample type for strings.
 * 
 * @author Joel Sjöstrand.
 */
public class SampleString implements SampleType {

	/** Sample type ID. */
	public static final String TYPE = "String";
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void appendType(StringBuilder sb) {
		sb.append(TYPE);
	}
}
