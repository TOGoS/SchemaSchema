package togos.schemaschema.parser.ast;

import togos.lang.SourceLocation;

public class Parameterized extends ASTNode
{
	public static final Parameterized[] EMPTY_PARAMETER_LIST = new Parameterized[0];
	
	public final Phrase subject;
	public final Parameterized[] parameters;
	
	public Parameterized( Phrase subject, Parameterized[] parameters, SourceLocation loc ) {
		super(loc);
		this.subject = subject;
		this.parameters = parameters;
	}
	
	public Parameterized( Phrase subject, SourceLocation loc ) {
		this( subject, EMPTY_PARAMETER_LIST, loc );
	}
	
	public String toString() {
		String s = subject.toString();
		if( parameters.length > 0 ) {
			s += "(";
			boolean frist = true;
			for( Parameterized p : parameters ) {
				if( !frist ) s += ", ";
				s += p.toString();
				frist = false;
			}
			s += ")";
		}
		return s;
	}
}
