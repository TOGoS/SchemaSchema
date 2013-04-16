package togos.schemaschema.parser.ast;

import togos.lang.SourceLocation;

public class Command extends ASTNode
{
	public final Parameterized subject;
	public final Parameterized[] modifiers;
	public final Block body;
	
	public Command( Parameterized subject, Parameterized[] modifiers, Block body, SourceLocation loc ) {
		super(loc);
		this.subject = subject;
		this.modifiers = modifiers;
		this.body = body;
	}
	
	public String toString() {
		String s = subject.toString();
		for( Parameterized p : modifiers ) {
			s += " : " + p.toString();
		}
		if( body.commands.length == 0 ) {
		} else if( body.commands.length == 1 ) {
			s += " = " + body.commands[0].toString();
		} else {
			s += " {\n\t" + body.toString().replace("\n", "\n\t") + "\n}";
		}
		return s;
	}
	
	public Parameterized[] getSubjectAndModifiers() {
		Parameterized[] sam = new Parameterized[modifiers.length+1];
		sam[0] = subject;
		for( int i=0; i<modifiers.length; ++i ) sam[i+1] = modifiers[i];
		return sam;
	}
}
