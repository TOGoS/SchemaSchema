package togos.schemaschema;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	public static final String join( String separator, String[] things ) {
		if( things.length == 0 ) return "";
		if( things.length == 1 ) return things[0];
		StringBuilder sb = new StringBuilder();
		sb.append(things[0]);
		for( int i=1; i<things.length; ++i ) {
			sb.append(separator);
			sb.append(things[i]);
		}
		return sb.toString();
	}
	
	public static final String indent( String prefix, String text ) {
		return prefix + text.replace("\n", "\n"+prefix);
	}
	
	static final Pattern ALL_WHITESPACE     = Pattern.compile("^\\s*$");
	static final Pattern LEADING_WHITESPACE = Pattern.compile("^(\\s*)(.*)$");
	
	public static final String[] unindent( String[] lines ) {
		int firstTextLine = -1;
		int lastTextLine  = -1;
		
		for( int i=0; i<lines.length; ++i ) {
			if( !ALL_WHITESPACE.matcher(lines[i]).matches() ) {
				if( firstTextLine == -1 ) firstTextLine = i;
				lastTextLine = i;
			}
		}
		
		if( firstTextLine == -1 ) return new String[0];
		
		String longestConsistentWhitespace = null;
		
		for( int i=firstTextLine; i<=lastTextLine; ++i ) {
			Matcher m = LEADING_WHITESPACE.matcher(lines[i]);
			if( !m.matches() ) throw new RuntimeException("The leading whitespace regex should always match!  But somehow this line didn't: {"+lines[i]+'}');
			String lead = m.group(1);
			if( longestConsistentWhitespace == null ) {
				longestConsistentWhitespace = lead;
			} else {
				int j;
				for( j=0; j<Math.min(lead.length(), longestConsistentWhitespace.length()); ++j ) {
					if( lead.charAt(j) != longestConsistentWhitespace.charAt(j) ) {
						break;
					}
				}
				longestConsistentWhitespace = longestConsistentWhitespace.substring(0, j);
			}
		}
		
		String[] rez = new String[lastTextLine+1-firstTextLine];
		for( int i=firstTextLine, r=0; i<=lastTextLine; ++i,++r ) {
			rez[r] = lines[i].substring(longestConsistentWhitespace.length());
		}
		return rez;
	}
	
	public static final String unindent( String text ) {
		return join("\n",unindent(text.split("\n")));
	}
}
