package togos.schemaschema.parser.ast;

import togos.lang.SourceLocation;

public class Phrase extends ASTNode
{
	public final Word[] words;
	
	public Phrase( Word[] words, SourceLocation sLoc ) {
		super( sLoc );
		this.words = words;
	}
	
	public String toString() {
		String s = "";
		for( Word w : words ) {
			if( s.length() > 0 ) s += " ";
			s += w.toString();
		}
		return s;
	}
}
