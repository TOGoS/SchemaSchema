package togos.schemaschema.parser;

import java.util.List;

import togos.lang.ScriptError;
import togos.lang.SourceLocation;
import togos.schemaschema.SchemaObject;
import togos.schemaschema.parser.ast.Parameterized;

public interface Macro {
	SchemaObject apply( SchemaInterpreter interp, Parameterized[] arguments, List<SourceLocation> trace ) throws ScriptError;
}
