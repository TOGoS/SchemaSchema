package togos.schemaschema.parser.ast;

import togos.lang.BaseSourceLocation;
import togos.lang.SourceLocation;

public class Block extends ASTNode
{
	public static final Block EMPTY = new Block( new Command[0], BaseSourceLocation.NONE );
	
	public final Command[] commands;
	
	public Block( Command[] commands, SourceLocation loc ) {
		super(loc);
		this.commands = commands;
	}
	
	public String toString() {
		String s = "";
		for( Command c : commands ) {
			if( s.length() > 0 ) s += "\n"; 
			s += c.toString();
		}
		return s;
	}
}
