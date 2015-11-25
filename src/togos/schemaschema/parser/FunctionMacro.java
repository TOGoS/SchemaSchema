package togos.schemaschema.parser;

import java.util.List;

import togos.lang.ScriptError;
import togos.lang.SourceLocation;
import togos.schemaschema.ListUtil;
import togos.schemaschema.SchemaObject;
import togos.schemaschema.parser.ast.Parameterized;

public class FunctionMacro implements Macro {
	static SchemaObject apply(Function func, SchemaInterpreter interp, Parameterized[] arguments, List<SourceLocation> trace) throws ScriptError {
		SchemaObject[] argValues = new SchemaObject[arguments.length];
		for( int i=0; i<arguments.length; ++i ) {
			argValues[i] = interp.evaluate(null, arguments[i], ListUtil.appended(trace, arguments[i].sLoc));
		}
		return func.apply(interp, argValues, trace);
	}
	
	protected final Function func;
	public FunctionMacro( Function func ) {
		this.func = func;
	}
	@Override public SchemaObject apply(SchemaInterpreter interp, Parameterized[] arguments, List<SourceLocation> trace) throws ScriptError {
		return apply(func, interp, arguments, trace);
	}
}
