package togos.schemaschema.parser.ast;

import togos.lang.SourceLocation;

public abstract class ASTNode
{
	public final SourceLocation sLoc;
	
	public ASTNode( SourceLocation sLoc ) {
		this.sLoc = sLoc;
	}
}
