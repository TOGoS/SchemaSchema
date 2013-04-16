package togos.schemaschema.parser.ast;

import togos.lang.SourceLocation;
import togos.schemaschema.parser.Token;
import togos.schemaschema.parser.Tokenizer;

public class Word extends ASTNode
{
	/**
	 * Single-quotes a string.
	 */
	public static String quote( char[] text ) {
		char[] escaped = new char[text.length*2+2];
		int j = 0;
		escaped[j++] = '\'';
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
			case '\'':
				escaped[j++] = '\\';
				escaped[j++] = '\'';
				break;
			case '\\':
				escaped[j++] = '\\';
				escaped[j++] = '\\';
				break;
			default:
				escaped[j++] = c;
			}
		}
		escaped[j++] = '\'';
		return new String( escaped, 0, j );
	}
	
	public static String quote( String text ) {
		return quote( text.toCharArray() );
	}
	
	/**
	 * Single quotes the given string if it contains any non-word characters.
	 */
	public static String quoteIfNecessary( String text ) {
		for( char c : text.toCharArray() ) {
			if( !Tokenizer.isWordChar(c) ) {
				return quote(text);
			}
		}
		return text;
	}
	
	public final String text;
	public final Token.Type quoting;	
	
	public Word( String text, Token.Type quoting, SourceLocation loc ) {
		super(loc);
		this.text = text;
		this.quoting = quoting;
	}
	
	public String toString() {
		return quoteIfNecessary(this.text);
	}
}
