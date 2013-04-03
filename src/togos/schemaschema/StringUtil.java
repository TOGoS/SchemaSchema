package togos.schemaschema;

public final class StringUtil
{
	private StringUtil() { }
	
	public static final String join( String separator, Iterable<?> values ) {
		StringBuilder sb = new StringBuilder(1024);
		for( Object v : values ) {
			if( sb.length() > 0 ) sb.append( separator );
			sb.append( v );
		}
		return sb.toString();
	}
	
	public static final String indent( String prefix, String text ) {
		return prefix + text.replace("\n", "\n"+prefix);
	}
}
