package togos.schemaschema.parser.ast;

import togos.lang.SourceLocation;
import togos.schemaschema.parser.Token;
import togos.schemaschema.parser.Tokenizer;

public class Word extends ASTNode
{
	/**
	 * Single-quotes a string.
	 */
	public static String quote( char[] text, char delimiter ) {
		char[] escaped = new char[text.length*2+2];
		int j = 0;
		escaped[j++] = delimiter;
		for( char c : text ) {
			switch( c ) {
			case '\r':
				escaped[j++] = '\\';
				escaped[j++] = 'r';
				break;
			case '\n':
				escaped[j++] = '\\';
				escaped[j++] = 'n';
				break;
			case '\t':
				escaped[j++] = '\\';
				escaped[j++] = 't';
				break;
			case '\'': case '"':
				if( c == delimiter ) {
					escaped[j++] = '\\';
					escaped[j++] = c;
				} else {
					escaped[j++] = c;
				}
				break;
			case '\\':
				escaped[j++] = '\\';
				escaped[j++] = '\\';
				break;
			default:
				escaped[j++] = c;
			}
		}
		escaped[j++] = delimiter;
		return new String( escaped, 0, j );
	}
	
	public static String quote( String text, char delimiter ) {
		return quote( text.toCharArray(), delimiter );
	}
	
	public static String quote( String text ) {
		return quote( text, '\'' );
	}
	
	/**
	 * Single quotes the given string if it contains any non-word characters.
	 */
	public static String quoteIfNecessary( String text, char delimiter ) {
		for( char c : text.toCharArray() ) {
			if( !Tokenizer.isWordChar(c) || c == delimiter ) {
				return quote(text, delimiter);
			}
		}
		return text;
	}
	
	public static String quoteIfNecessary( String text ) {
		return quoteIfNecessary(text, '\'');
	}
	
	public static char quoteChar(Token.Type tokenType) {
		return tokenType == Token.Type.DOUBLE_QUOTED_STRING ? '"' : '\'';
	}
	
	public final String text;
	public final Token.Type quoting;	
	
	public Word( String text, Token.Type quoting, SourceLocation loc ) {
		super(loc);
		this.text = text;
		this.quoting = quoting;
	}
	
	public String toString() {
		return quoteIfNecessary(this.text, quoteChar(quoting));
	}
}
