package togos.schemaschema.parser.ast;

import togos.lang.SourceLocation;
import togos.schemaschema.parser.Tokenizer;

public class Word extends ASTNode
{
	public final String text;
	
	public Word( String text, SourceLocation loc ) {
		super(loc);
		this.text = text;
	}
	
	public String toString() {
		boolean needsEscapin = false;
		char[] chars = text.toCharArray();
		for( char c : chars ) {
			if( !Tokenizer.isWordChar(c) ) {
				needsEscapin = true;
				break;
			}
		}
		
		if( !needsEscapin ) return text;
		
		char[] escaped = new char[text.length()*2+2];
		int j = 0;
		escaped[j++] = '\'';
		for( char c : chars ) {
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
}
