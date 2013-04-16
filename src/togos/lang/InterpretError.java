package togos.lang;

import togos.lang.SourceLocation;

public class InterpretError extends ScriptError
{
	private static final long serialVersionUID = 1L;
	
	public InterpretError( String message, SourceLocation sloc ) {
		super( message, sloc );
	}
	
	public InterpretError( Exception cause, SourceLocation sloc ) {
		super( cause, sloc );
	}
}
