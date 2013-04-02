package togos.schemaschema.parser.ast;

import togos.lang.SourceLocation;

public class Parameterized extends ASTNode
{
	public final Phrase subject;
	public final Parameterized[] parameters;
	
	public Parameterized( Phrase subject, Parameterized[] parameters, SourceLocation loc ) {
		super(loc);
		this.subject = subject;
		this.parameters = parameters;
	}
	
	public String toString() {
		String s = subject.toString();
		if( parameters.length > 0 ) {
			s += "(";
			boolean frist = true;
			for( Parameterized p : parameters ) {
				if( !frist ) s += ", ";
				s += p.toString();
				frist = true;
			}
			s += ")";
		}
		return s;
	}
}
